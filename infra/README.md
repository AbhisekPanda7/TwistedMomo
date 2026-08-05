# TwistedMomos Infrastructure Runbook

Production runs on a single Hostinger KVM VPS managed by [Dokploy](https://dokploy.com).

**The defining property of this setup: no inbound port is open on the public
interface.** Public web traffic arrives through a Cloudflare Tunnel that the box
dials outward; administrative access arrives over Tailscale, also outbound. A port
scan of the public IP finds nothing.

Provisioning is manual. Every step below is run by hand over SSH and is written
down here so the box can be rebuilt from this file alone. The rationale for each
control is in [SECURITY.md](./SECURITY.md).

## Topology

```
Browser ──https──> Cloudflare edge (WAF, DDoS, TLS, rate limiting)
                        ▲
                        │  outbound-initiated tunnel — nothing listens on the VPS
                        │
              ┌─────────┴──────────────────────────────┐
              │ Hostinger VPS — Ubuntu 24.04, 8 GB     │
              │                                        │
              │  cloudflared ──┬─ frontend :80        │
              │                ├─ backend  :8080      │
              │                └─ mysql (no ports)    │
              │  loki / promtail / grafana             │
              │  Dokploy panel :3000 ── tailnet only   │
              │                                        │
              │  ufw: deny all inbound on eth0         │
              │       allow in on tailscale0           │
              │  sshd: ListenAddress <tailscale-ip>    │
              └────────────────────────────────────────┘
                        ▲
                        │  WireGuard mesh, also outbound-initiated
                   Tailscale (SSH, Dokploy panel, Grafana)
```

CI never touches the box directly: GitHub Actions pushes an image to GHCR and calls
a Dokploy webhook over HTTPS, and Dokploy pulls.

## Provisioning order

Follow these in sequence. Do not skip the verification command at the end of each
section — several of these steps fail silently.

1. [Create the VPS](#1-create-the-vps)
2. [First login and system updates](#2-first-login-and-system-updates)
3. [Create the deploy user](#3-create-the-deploy-user)
4. [Install and join Tailscale](#4-install-and-join-tailscale)
5. [Bind sshd to Tailscale only](#5-bind-sshd-to-tailscale-only)
6. [Configure the firewall](#6-configure-the-firewall)
7. [Install Docker](#7-install-docker)
8. [Install Dokploy](#8-install-dokploy)
9. [Create the Cloudflare Tunnel](#9-create-the-cloudflare-tunnel)
10. [Deploy the application stack](#10-deploy-the-application-stack)
11. [Deploy the observability stack](#11-deploy-the-observability-stack)
12. [Configure CI/CD](#12-configure-cicd)
13. [Configure backups](#13-configure-backups)
14. [Verify](#14-verify)

---

## 1. Create the VPS

Hostinger panel → VPS → Ubuntu 24.04 LTS, 8 GB plan. Note the public IP and the
root password.

Also note the domain bundled with the plan — it is needed in step 9.

## 2. First login and system updates

```bash
ssh root@<vps-public-ip>
```

This is the **only** time SSH is used over the public IP. After step 5 it stops
working, by design.

```bash
apt update && apt upgrade -y
apt install -y curl ca-certificates ufw fail2ban unattended-upgrades
```

Enable automatic security patching so known-CVE drift does not accumulate on a box
nobody logs into for weeks:

```bash
dpkg-reconfigure -f noninteractive unattended-upgrades
systemctl status unattended-upgrades --no-pager | head -5
```

Verify: the service reports `active (running)`.

**If SSH stops answering after this step**, the upgrade restarted the SSH units and
`ssh.socket` did not come back — an observed failure on a fresh Hostinger Ubuntu
24.04 image, not a hypothetical. The symptom is a connection *timeout* while the
Hostinger browser console still logs in fine. Recover from that console:

```bash
systemctl status ssh --no-pager | head -6   # "inactive (dead)" confirms it
ss -tlnp | grep :22                          # no output confirms nothing is listening
systemctl enable --now ssh.socket
ss -tlnp | grep :22                          # LISTEN 0 4096 0.0.0.0:22
```

An `apt upgrade` that pulls a kernel also leaves `*** System restart required ***`.
Reboot once SSH is confirmed working, then check it comes back on its own — that is
the test that `enable` actually persisted.

## 3. Create the deploy user

Running everything as root means any container escape or careless command is
immediately fatal. Create an unprivileged user that day-to-day work happens as:

```bash
adduser --disabled-password --gecos "" admin-deploy
usermod -aG sudo admin-deploy
```

`--disabled-password` sets no password: this account is reached only through
Tailscale SSH, which handles authentication itself. There is no password to guess
and no key file to rotate.

That has a consequence worth stating plainly: **`sudo` can never succeed for this
account**, because there is no password for it to accept. Grant passwordless sudo
instead — still as root:

```bash
echo 'admin-deploy ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/admin-deploy
chmod 0440 /etc/sudoers.d/admin-deploy
visudo -c
```

`visudo -c` must report `parsed OK` for every file it lists. A malformed sudoers
file breaks `sudo` for every account at once, so do not skip it.

Anyone holding a shell as `admin-deploy` therefore holds root without a second
prompt. That is the accepted trade: reaching that shell already means passing the
Tailscale ACL with its 12-hour re-authentication, so a password here would guard a
door that already takes two keys — and the nightly backup in step 13 runs
unattended and cannot answer a prompt at all.

Verify:

```bash
id admin-deploy
# uid=1000(admin-deploy) gid=1000(admin-deploy) groups=1000(admin-deploy),27(sudo)
```

From your laptop, once step 4 has put this host on the tailnet:

```bash
ssh admin-deploy@twistedmomos-prod 'sudo whoami'
# root
```

## 4. Install and join Tailscale

Before running this, create a **reusable, pre-authorized** auth key tagged
`tag:prod` at <https://login.tailscale.com/admin/settings/keys>.

```bash
curl -fsSL https://tailscale.com/install.sh | sh

tailscale up \
  --authkey='tskey-auth-REPLACE-ME' \
  --hostname=twistedmomos-prod \
  --ssh
```

`--ssh` hands SSH authentication and authorization to Tailscale itself. Access is
then governed by the tailnet ACL rather than `authorized_keys` files that have to
be distributed and rotated by hand, and adding or removing a developer never
touches the server. See [SECURITY.md](./SECURITY.md#granting-another-developer-access).

Record the Tailscale IP — step 5 needs it:

```bash
tailscale ip -4
# 100.x.y.z
```

Verify MagicDNS resolves the host from your laptop (which must also be on the
tailnet):

```bash
tailscale status | grep twistedmomos-prod
ssh admin-deploy@twistedmomos-prod 'echo tailscale ssh works'
```

Do not proceed until that last command succeeds. Step 5 removes public SSH, and if
Tailscale SSH is not working first, you will be locked out.

## 5. Bind sshd to Tailscale only

This is the *belt*; the firewall in step 6 is the *braces*. With both in place,
`sshd` is not merely blocked from the public interface — it is not listening on it
at all, so even a flushed firewall leaves nothing exposed.

**First, disable socket activation.** Ubuntu 24.04 ships SSH socket-activated:
`ssh.socket` owns the listening address and spawns `ssh.service` per connection.
While that is in effect, `ListenAddress` in `sshd_config` is **silently ignored** —
the bind address comes from the socket unit instead. Confirm which mode is active:

```bash
ss -tlnp | grep :22
# users:(("systemd",pid=1,...))  -> socket-activated, do the disable below
# users:(("sshd",...))           -> classic service, skip to the ListenAddress block
```

```bash
systemctl disable --now ssh.socket
systemctl enable ssh.service
```

**Second, remove the boot-time bind race.** Binding to the Tailscale address makes
sshd depend on an interface that does not exist early in boot. `tailscaled` reports
started before `tailscale0` has an address, so ordering the unit after it is *not*
enough — sshd loses the race and dies with `Bind to port 22 on 100.x.y.z failed:
Cannot assign requested address`. Allowing a non-local bind removes the race
outright rather than retrying around it:

```bash
echo 'net.ipv4.ip_nonlocal_bind=1' > /etc/sysctl.d/99-nonlocal-bind.conf
sysctl --system | grep -i nonlocal
```

Add ordering and a retry as the second line of defence. Note the empty
`RestartPreventExitStatus=`: the vendor unit ships `RestartPreventExitStatus=255`,
and sshd exits **255** on bind failure — so without clearing it, `Restart=` is
silently inert and no retry ever happens.

```bash
mkdir -p /etc/systemd/system/ssh.service.d
```

```bash
printf '[Unit]\nAfter=tailscaled.service network-online.target\nWants=network-online.target\n\n[Service]\nRestartPreventExitStatus=\nRestart=on-failure\nRestartSec=5s\n' > /etc/systemd/system/ssh.service.d/10-after-tailscale.conf
```

```bash
systemctl daemon-reload
```

Then bind sshd to the tailnet:

```bash
TS_IP=$(tailscale ip -4)
printf '\nListenAddress %s\n' "$TS_IP" >> /etc/ssh/sshd_config

sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin no/'          /etc/ssh/sshd_config
sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config

sshd -t && systemctl restart ssh.service
```

`sshd -t` validates the configuration before the restart. **Do not skip it** — a
syntax error plus a restart means sshd fails to come back and you are locked out.

**Third, verify the effective config rather than the file.** `/etc/ssh/sshd_config`
begins with `Include /etc/ssh/sshd_config.d/*.conf`, and in OpenSSH **the first
match wins** — so for included files a *lower* number beats a higher one, the
opposite of most `.d` conventions. Cloud images ship
`50-cloud-init.conf` containing `PasswordAuthentication yes`, which silently beats
the line the `sed` above just wrote:

```bash
sshd -T | grep -iE 'passwordauthentication|kbdinteractive|permitrootlogin'
```

All three must read `no`. If `passwordauthentication` reads `yes`, find the winner
and fix it at the source:

```bash
grep -rn PasswordAuthentication /etc/ssh/sshd_config /etc/ssh/sshd_config.d/
sed -i 's/^PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config.d/50-cloud-init.conf
sshd -t && systemctl restart ssh.service
```

Stop cloud-init reinstating it on the next boot:

```bash
echo 'ssh_pwauth: false' > /etc/cloud/cloud.cfg.d/99-disable-pwauth.cfg
```

Keep the Hostinger browser console open while running this. It is an out-of-band
connection to the VM that works when sshd is stopped and the firewall is closed,
which is what makes this step recoverable rather than a one-way door.

**Verify across a reboot, not just a restart.** Every failure mode above appears
only at boot; `systemctl restart` succeeds regardless and proves nothing:

```bash
reboot
# wait ~90s, then from your laptop:
ssh admin-deploy@twistedmomos-prod 'systemctl is-active ssh.service; sudo ss -tlnp | grep :22'
```

Want `active` and `LISTEN ... 100.x.y.z:22` — and never `0.0.0.0:22`.

Note that Tailscale SSH is served by `tailscaled`, not by `sshd`, so it keeps
working even while `ssh.service` is failed. That is why these failures cost
redundancy rather than access — and why they are easy to miss.

## 6. Configure the firewall

```bash
ufw --force reset
ufw default deny incoming
ufw default allow outgoing

# The only inbound traffic accepted anywhere on this machine.
ufw allow in on tailscale0 comment 'tailnet'

# Braces for the Dokploy panel, which is also bound to the Tailscale interface.
# The panel holds every environment variable in plaintext.
ufw deny 3000 comment 'dokploy-panel-public'

ufw --force enable
```

No rule opens 80 or 443. That is not an omission — public traffic arrives through
the Cloudflare Tunnel in step 9, which the box dials outward.

Verify:

```bash
ufw status verbose
# Default: deny (incoming), allow (outgoing), disabled (routed)
# Anywhere on tailscale0    ALLOW IN    Anywhere    # tailnet
```

**`ufw` alone does not hold once Docker is installed.** Docker inserts its own
`DOCKER-USER` iptables chain, which is evaluated *before* ufw's rules, so any
container that publishes a port reaches the internet regardless of what `ufw status`
claims. Dokploy's Traefik publishes 80 and 443, and the panel publishes 3000 —
observed open from off-network immediately after step 8, with ufw active and
`deny 3000` in place.

Close them in the chain Docker actually consults, after step 8 has installed Docker:

```bash
iptables -I DOCKER-USER -i eth0 -p tcp --dport 3000 -j DROP
iptables -I DOCKER-USER -i eth0 -p tcp --dport 80 -j DROP
iptables -I DOCKER-USER -i eth0 -p tcp --dport 443 -j DROP
```

These are not persistent on their own:

```bash
apt install -y iptables-persistent   # answer yes to saving current rules
netfilter-persistent save
```

Verify from a machine **not** on the tailnet — this is the check that matters, and
it must be run from off-network because everything looks closed from inside:

```bash
nc -vz -w 5 <vps-public-ip> 22
nc -vz -w 5 <vps-public-ip> 80
nc -vz -w 5 <vps-public-ip> 443
nc -vz -w 5 <vps-public-ip> 3000
```

All four must time out. Re-run after any reboot and after any Dokploy upgrade —
a republished port silently reopens the hole.

Once the Cloudflare Tunnel exists (step 9), Traefik does not need 80 and 443
published at all: `cloudflared` reaches it over the internal Docker network. The
DROP rules hold the line until then; unpublishing the ports in Dokploy's Traefik
configuration is the cleaner end state.

## 7. Install Docker

```bash
curl -fsSL https://get.docker.com | sh
usermod -aG docker admin-deploy
```

Verify:

```bash
docker --version
sudo -u admin-deploy docker ps
```

The second command must succeed without `sudo` inside it — that confirms the
`admin-deploy` user's group membership took effect. If it fails, log out and back in.

## 8. Install Dokploy

Dokploy's installer sets up Docker Swarm, a Traefik reverse proxy and its own
Postgres, then serves a panel on port 3000. Run it as root.

```bash
curl -sSL https://dokploy.com/install.sh | sh
```

Verify. Swarm services and plain containers are listed separately, and Traefik is
**not** a swarm service — checking only `docker service ls` makes it look like the
install failed:

```bash
docker service ls
# dokploy, dokploy-postgres

docker ps --format '{{.Names}}\t{{.Status}}'
# dokploy-traefik, plus the dokploy and dokploy-postgres tasks
```

Observed on v0.29.13: there is no `dokploy-redis`. Do not go hunting for it.

**Claim the admin account immediately — before anything else.** The first visitor
to the panel becomes the administrator, and at this point in the runbook port 3000
is still reachable from the internet (see step 6: Docker bypasses ufw, and the DROP
rules have not been added yet). Whoever loads it first owns every secret this box
will ever hold.

```
http://<tailscale-ip>:3000
```

1. Create the admin user with a strong, unique password.
2. Enable 2FA under Settings → Profile.

Then apply the `DOCKER-USER` DROP rules from step 6 and confirm the panel is *not*
reachable publicly, from a machine **not** on the tailnet:

```bash
curl --max-time 10 http://<vps-public-ip>:3000
# must time out
```

If the panel was already claimed when you arrived, treat the install as
compromised: wipe Dokploy and reinstall rather than reasoning about what an
attacker did or did not do.

## 9. Create the Cloudflare Tunnel

The tunnel is what lets the box serve public traffic with no inbound port. The
connector dials out; Cloudflare pushes requests down that connection.

**In the Cloudflare dashboard:**

1. Add `twistedmomos.tech` to Cloudflare (free plan) and change the nameservers at
   Hostinger to the two Cloudflare provides. Wait for the zone to report Active.

   Cloudflare imports the registrar's existing records, which for a fresh
   Hostinger domain means an `A` record for the apex pointing at a parking IP and
   a `CNAME` for `www`. Delete the apex `A` record — step 5 below replaces it.
2. Zero Trust → Networks → Tunnels → **Create a tunnel** → type **Cloudflared**.
3. Name it `twistedmomos-prod`.
4. Copy the token from the install command shown. This is the value of
   `CLOUDFLARE_TUNNEL_TOKEN`. **Do not run the install command** — the connector
   runs as a container in the application stack, not as a host service.
5. Public Hostnames → **Add a public hostname**, twice — one per service:

   | Subdomain | Domain | Type | URL |
   |---|---|---|---|
   | *(blank)* | `twistedmomos.tech` | HTTP | `frontend:80` |
   | `api` | `twistedmomos.tech` | HTTP | `backend:8080` |

   Leaving the subdomain blank serves the apex. Add a third for `www` if you want
   it to resolve.

   The connector resolves `frontend` and `backend` by service name on the Docker
   network. Traffic between Cloudflare and the connector is encrypted by the
   tunnel itself, so plain HTTP on this leg is correct — there is no certificate
   to obtain or renew on the origin.

**No DNS record needs creating by hand.** The tunnel creates a proxied CNAME for
each hostname automatically.

## 10. Deploy the application stack

Both images must exist in GHCR before this will deploy — nothing is built on the
host. If neither workflow has run yet, publish them first: Actions → *backend
image* → **Run workflow**, and the same for *frontend image*.

1. Dokploy → Create Project → `twistedmomos`.
2. Inside it → Create Service → **Compose**.
3. Source. Provider **Git** with URL
   `https://github.com/AbhisekPanda7/TwistedMomo.git`, branch `main`, compose path
   `infra/app/compose.yml`. The repository is public, so no credentials are
   needed — and unlike the GitHub App provider, this needs no approval from the
   repository owner, which matters when you are a collaborator rather than the
   owner.
4. Environment tab → paste `infra/app/.env.example` and replace every `change-me`.
   Generate secrets with:
   ```bash
   openssl rand -base64 48
   ```
   `SPRING_DATASOURCE_PASSWORD` must be **identical** to `MYSQL_PASSWORD`. They
   are two names for one credential, and a mismatch surfaces as a Flyway
   connection failure at startup rather than as anything mentioning passwords.
5. Set `CLOUDFLARE_TUNNEL_TOKEN` to the token from step 9.
6. Deploy. Watch the logs until Flyway reports the migrations applied and Boot
   logs `Started BackendApplication`.

**If a deploy fails and you then change anything about a network in this file,
tear the stack down before redeploying.** Docker does not recreate an existing
network when its definition changes, so the old one silently survives and the
next deploy is applied against it. The symptom is not a network error — it is
`java.net.UnknownHostException: mysql` buried at the bottom of a Spring bean
failure, because the backend never joined the network MySQL is on:

```bash
docker rm -f $(docker ps -aq --filter name=twistedmomos-app)
docker network rm $(docker network ls -q --filter name=twistedmomos-app)
docker network ls | grep twistedmomos    # must print nothing
```

Named volumes (`mysql-data`, `backend-uploads`) are untouched by this, so no data
is lost.

Verify the tunnel connected:

```bash
ssh admin-deploy@twistedmomos-prod
docker logs $(docker ps --filter name=cloudflared --format '{{.Names}}' | head -1) 2>&1 | tail -20
# "Registered tunnel connection" — usually four, one per Cloudflare colo
```

Verify both hostnames answer publicly:

```bash
curl -fsS https://api.twistedmomos.tech/actuator/health
# {"status":"UP"}

curl -sS -o /dev/null -w '%{http_code}\n' https://twistedmomos.tech/
# 200

curl -sS -o /dev/null -w '%{http_code}\n' https://twistedmomos.tech/admin/orders
# 200 — nginx serves index.html for unknown paths so React Router can route them.
# A 404 here means the SPA fallback is missing from the frontend image.
```

Then load `https://twistedmomos.tech` in a browser and confirm the app reaches the
API — the base URL is compiled into the bundle, so a wrong one shows as requests
to the wrong host in the network tab rather than as a build failure.

## 11. Deploy the observability stack

A separate Dokploy service from the application, so logging can be restarted
without touching the API.

1. Dokploy → same project → Create Service → **Compose**.
2. Source: provider **Git**, URL
   `https://github.com/AbhisekPanda7/TwistedMomo.git`, branch `main`, compose path
   `infra/observability/compose.yml`.
3. Environment tab → paste `infra/observability/.env.example`, replacing
   `change-me-long-random` with `openssl rand -base64 24`, and setting
   `GRAFANA_BIND_IP` to this host's Tailscale address (`tailscale ip -4`).
4. Deploy.

**Changing `loki-config.yml` or `promtail-config.yml` needs a container restart,
not just a deploy.** Both are bind-mounted, so their contents change without the
container spec changing — `docker compose up -d` reports the container `Running`,
Dokploy reports `Deployed ✅`, and the process carries on serving the file it
started with. Promtail's `watchConfig` is not a safety net either: an editor that
saves by rename leaves the watcher on the old inode.

After deploying a config change:

```bash
docker restart <stack>-promtail-1
docker exec <stack>-promtail-1 head -40 /etc/promtail/promtail-config.yml
```

Cost an afternoon on 2026-08-05: a corrected Promtail pipeline was merged,
deployed and reported healthy while Loki kept storing the broken lines. Green
output, nothing applied — the same shape as a Dokploy webhook answering 200 with
`Branch Not Match`.

**Reaching Grafana.** It binds to the Tailscale interface, so any device on the
tailnet reaches it by MagicDNS name — no tunnel, no port forward:

```
http://twistedmomos-prod:3001
```

Confirm it is bound to that interface and not to `0.0.0.0`:

```bash
ss -tlnp | grep 3001
# LISTEN 0 4096 100.x.y.z:3001
```

`0.0.0.0:3001` there means `GRAFANA_BIND_IP` was unset and the panel is exposed to
the internet — Docker publishes ports through its own iptables chain, ahead of
ufw, so the firewall will not save you. Fix the variable and redeploy rather than
adding a DROP rule on top.

Verify from off-network that it is not reachable publicly:

```bash
nc -vz -w 5 <vps-public-ip> 3001    # must time out
```

Add the data source: Connections → Add data source → **Loki** → URL
`http://loki:3100` → Save & test.

## Finding a request by its trace ID

A user reporting a failure quotes the `Ref:` shown in the UI — that is the trace
ID. The same value appears in the `X-Trace-Id` response header and in the
`traceId` field of the error envelope.

In Grafana → Explore → Loki:

```logql
{container=~"twistedmomos.*"} | traceId = `4bf92f3577b34da6a3ce929d0e0e4736`
```

That returns every log line for that one request, across every class that logged
during it.

All errors in the last hour:

```logql
{container=~"twistedmomos.*", level="ERROR"}
```

Everything a single class logged:

```logql
{container=~"twistedmomos.*"} | logger =~ `.*OrderServiceImpl`
```

## 12. Configure CI/CD

The production host never builds. GitHub Actions runs the tests, builds the image,
pushes it to GHCR, and calls a Dokploy webhook. The runner never connects to the
VPS, which is why closing public SSH does not break deployments.

**In Dokploy:**

1. Open the `twistedmomos` compose service → Deployments tab.
2. Copy the webhook URL.
3. If the GHCR package is private, add registry credentials under
   Settings → Registry: your GitHub username, and a PAT with `read:packages`.

**In GitHub:**

1. Repo → Settings → Secrets and variables → Actions.
2. Add `DOKPLOY_WEBHOOK_URL` with the URL from above. Treat it as a secret —
   anyone holding it can trigger a deploy.

`GITHUB_TOKEN` is provided automatically; the workflow requests `packages: write`
for it and needs no PAT for pushing.

**Flow**

```
push to main (backend/**)
  → tests run (Mockito, no database)
  → image built, pushed as :latest and :<commit-sha>
  → webhook → Dokploy pulls :latest and redeploys
```

**Rolling back.** Set `BACKEND_IMAGE` to a previous `:<commit-sha>` in the Dokploy
environment and redeploy. This is why both tags are pushed — a rollback is a
configuration change, not a rebuild.

## 13. Configure backups

Copy the script to the box and schedule it:

```bash
scp infra/bootstrap/backup.sh admin-deploy@twistedmomos-prod:/tmp/
ssh admin-deploy@twistedmomos-prod
sudo mv /tmp/backup.sh /usr/local/bin/backup.sh
sudo chmod +x /usr/local/bin/backup.sh

sudo crontab -e
# 30 2 * * * /usr/local/bin/backup.sh >> /var/log/twistedmomos-backup.log 2>&1
```

Run it once by hand and confirm the output before trusting the schedule:

```bash
sudo /usr/local/bin/backup.sh
ls -lh /opt/twistedmomos/backups/
```

**What is backed up.** The database and the uploads volume. Logs are excluded —
they expire after 30 days by design — and Grafana dashboards are trivial to
recreate.

**Dokploy's own database is not covered by this script.** It holds every
environment variable in plaintext, so losing it means rebuilding all service
configuration by hand. Enable Dokploy's own backup feature for it, and store the
result somewhere that reflects the fact that it contains every secret.

**Getting copies off the box.** A backup on the same disk as the database is not
a backup. Pull them to a machine on the tailnet:

```bash
rsync -avz admin-deploy@twistedmomos-prod:/opt/twistedmomos/backups/ ./backups/
```

Or configure an S3-compatible destination in Dokploy (Backblaze B2's free tier is
ample at this size).

**Restoring the database.**

```bash
ssh admin-deploy@twistedmomos-prod
CONTAINER=$(docker ps --filter name=mysql --format '{{.Names}}' | head -1)
gunzip -c /opt/twistedmomos/backups/twisted_momos-YYYYMMDD-HHMMSS.sql.gz \
  | docker exec -i "$CONTAINER" sh -c 'exec mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"'
```

Flyway will find the schema already at the recorded version and apply nothing.

**Restoring uploads.**

```bash
VOLUME=$(docker volume ls --format '{{.Name}}' | grep backend-uploads | head -1)
docker run --rm -v "$VOLUME":/data -v /opt/twistedmomos/backups:/backup alpine:3.21 \
  tar xzf /backup/uploads-YYYYMMDD-HHMMSS.tar.gz -C /data
```

**Test a restore quarterly.** A backup that has never been restored is a guess.
