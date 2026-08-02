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
              │  cloudflared ──→ traefik (plain HTTP)  │
              │                    ├─ backend :8080    │
              │                    └─ mysql (no ports) │
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

## 3. Create the deploy user

Running everything as root means any container escape or careless command is
immediately fatal. Create an unprivileged user that day-to-day work happens as:

```bash
adduser --disabled-password --gecos "" deploy
usermod -aG sudo deploy
```

`--disabled-password` sets no password: this account is reached only through
Tailscale SSH, which handles authentication itself. There is no password to guess
and no key file to rotate.

Verify:

```bash
id deploy
# uid=1000(deploy) gid=1000(deploy) groups=1000(deploy),27(sudo)
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
ssh deploy@twistedmomos-prod 'echo tailscale ssh works'
```

Do not proceed until that last command succeeds. Step 5 removes public SSH, and if
Tailscale SSH is not working first, you will be locked out.

## 5. Bind sshd to Tailscale only

This is the *belt*; the firewall in step 6 is the *braces*. With both in place,
`sshd` is not merely blocked from the public interface — it is not listening on it
at all, so even a flushed firewall leaves nothing exposed.

```bash
TS_IP=$(tailscale ip -4)
printf '\n# Bind to the Tailscale interface only — never the public one.\nListenAddress %s\n' "$TS_IP" >> /etc/ssh/sshd_config

sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin no/'          /etc/ssh/sshd_config
sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config

sshd -t && systemctl restart ssh
```

`sshd -t` validates the configuration before the restart. **Do not skip it** — a
syntax error plus a restart means sshd fails to come back and you are locked out.

Verify it is listening on the Tailscale IP and nowhere else:

```bash
ss -tlnp | grep :22
# LISTEN 0 128 100.x.y.z:22 0.0.0.0:*
```

If `0.0.0.0:22` appears, the `ListenAddress` line did not take effect. Fix it
before continuing.

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

## 7. Install Docker

```bash
curl -fsSL https://get.docker.com | sh
usermod -aG docker deploy
```

Verify:

```bash
docker --version
sudo -u deploy docker ps
```

The second command must succeed without `sudo` inside it — that confirms the
`deploy` user's group membership took effect. If it fails, log out and back in.

## 8. Install Dokploy

Dokploy's installer sets up Docker Swarm, a Traefik reverse proxy, and its own
Postgres and Redis, then serves a panel on port 3000.

```bash
curl -sSL https://dokploy.com/install.sh | sh
```

Verify the services are running:

```bash
docker service ls
# dokploy, dokploy-traefik, dokploy-postgres, dokploy-redis
```

**Claim the admin account immediately.** The first visitor to the panel becomes the
administrator, and the panel is reachable by anyone on your tailnet:

```
http://<tailscale-ip>:3000
```

1. Create the admin user with a strong, unique password.
2. Enable 2FA under Settings → Profile.

Confirm the panel is *not* reachable publicly. From a machine that is **not** on
the tailnet:

```bash
curl --max-time 10 http://<vps-public-ip>:3000
# must time out
```

## 9. Create the Cloudflare Tunnel

The tunnel is what lets the box serve public traffic with no inbound port. The
connector dials out; Cloudflare pushes requests down that connection.

**In the Cloudflare dashboard:**

1. Add the Hostinger-bundled domain to Cloudflare (free plan) and change the
   nameservers at Hostinger to the two Cloudflare provides. Wait for the zone to
   report Active.
2. Zero Trust → Networks → Tunnels → **Create a tunnel** → type **Cloudflared**.
3. Name it `twistedmomos-prod`.
4. Copy the token from the install command shown. This is the value of
   `CLOUDFLARE_TUNNEL_TOKEN`. **Do not run the install command** — the connector
   runs as a container in the application stack, not as a host service.
5. Public Hostnames → **Add a public hostname**:
   - Subdomain `api`, domain your bundled domain
   - Service type **HTTP**, URL `backend:8080`

   The connector resolves `backend` on the Docker network. Traffic between
   Cloudflare and the connector is encrypted by the tunnel itself, so plain HTTP
   on this leg is correct — there is no certificate to obtain or renew on the
   origin.

**No DNS record needs creating by hand.** The tunnel creates a proxied CNAME for
the hostname automatically.

## 10. Deploy the application stack

1. Dokploy → Create Project → `twistedmomos`.
2. Inside it → Create Service → **Compose**.
3. Provider GitHub, repo `AbhisekPanda7/TwistedMomo`, branch `main`, compose path
   `infra/app/compose.yml`.
4. Environment tab → paste `infra/app/.env.example` and replace every `change-me`.
   Generate secrets with:
   ```bash
   openssl rand -base64 48
   ```
5. Set `CLOUDFLARE_TUNNEL_TOKEN` to the token from step 9.
6. Deploy. Watch the logs until Flyway reports the migrations applied and Boot
   logs `Started BackendApplication`.

Verify the tunnel connected:

```bash
ssh deploy@twistedmomos-prod
docker logs $(docker ps --filter name=cloudflared --format '{{.Names}}' | head -1) 2>&1 | tail -20
# "Registered tunnel connection" — usually four, one per Cloudflare colo
```

Verify the API answers publicly:

```bash
curl -fsS https://api.<your-domain>/actuator/health
# {"status":"UP"}
```

## 11. Deploy the observability stack

1. Dokploy → same project → Create Service → **Compose**.
2. Compose path `infra/observability/compose.yml`.
3. Environment tab → paste `infra/observability/.env.example`, replacing
   `change-me-long-random` with `openssl rand -base64 24`.
4. Deploy.

**Reaching Grafana.** It binds to loopback, so it is reached through an SSH tunnel
over Tailscale:

```bash
ssh -L 3001:127.0.0.1:3001 deploy@twistedmomos-prod
# then open http://localhost:3001
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
