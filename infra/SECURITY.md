# Security Model

## Principle

Every sensitive surface is protected by **two independent controls**, so a single
misconfiguration, flushed firewall, or leaked credential does not expose it. This
is a deliberate trade: more steps to provision, far fewer ways to be wrong.

The central decision is that **nothing listens on the public interface**. Both the
public path (Cloudflare Tunnel) and the administrative path (Tailscale) are
*outbound-initiated*. There is no port to scan, expose by accident, or forget to
close.

## What each control defends against

| Surface | Belt | Braces | Defeats |
|---|---|---|---|
| SSH reachability | `sshd` binds to the Tailscale IP only | `ufw` denies public inbound | Internet-wide SSH scanning; a flushed firewall still leaves sshd unreachable publicly |
| SSH authentication | Tailscale ACL decides who connects | `PasswordAuthentication no`, `PermitRootLogin no` | Credential stuffing, brute force, a leaked password |
| Session lifetime | ACL `check` mode re-authenticates periodically | Non-root `admin-deploy` user only | A stolen laptop granting permanent root |
| Public web | Tunnel is outbound-only | `ufw` denies public inbound | Direct-to-origin requests bypassing the WAF |
| Origin identity | Origin is never contacted directly | Cloudflare proxy hides the IP | IP leaks via DNS history or certificate logs |
| Database | No `ports:` mapping at all | Isolated overlay network | Reaching MySQL from the host or another stack |
| Dokploy panel | Bound to the Tailscale interface | `ufw deny 3000` | Exposure of every environment variable in plaintext |
| Grafana | Bound to the Tailscale interface | Admin password, no anonymous access, no sign-up | Anonymous log access; a port scan finding the panel at all |
| Patching | `unattended-upgrades` (security only) | Lynis audit, recorded | Known-CVE drift |

## Why not IP whitelisting for SSH

Restricting port 22 to a home IP works until it does not: travelling, a changed
residential IP, or a home network outage each lock you out of your own server, and
the rule has to be edited from wherever you happen to be. Routing back through a
home VPN to fix that adds a second hop and noticeable input lag.

Tailscale removes the problem rather than working around it. The box dials out, so
there is no inbound rule to maintain, and access follows the person rather than
the network they happen to be on.

## Accepted risks

- **Dokploy stores environment variables as plaintext** in its internal Postgres.
  Anyone with panel or database access reads every secret across every project.
  Mitigated by keeping the panel off the public internet, a strong unique admin
  password, and 2FA. **Not solved.** Treat panel access as equivalent to holding
  every secret.
- **Single host, no replication.** Recovery is restore-from-backup. See
  [README.md](./README.md#13-configure-backups).
- **Cloudflare is in the critical path.** A tunnel outage takes the site down. This
  fails *closed*, which is the correct direction — a reachable but unprotected
  origin would be worse.
- **The tunnel token is a bearer credential.** Anyone holding it can run a
  connector for your tunnel. It lives in Dokploy's environment, which is covered
  by the first risk above.

## Granting another developer access

SSH access is granted in the Tailscale ACL, not on the server. Nothing on the box
changes — no key is copied, no firewall rule is edited, and revocation is a line
deleted from this file.

### 1. Invite them to the tailnet

Tailscale admin console → **Users → Invite external users** → their email address.
The free plan covers 3 users and 100 devices.

### 2. Scope their access in the ACL

Edit the tailnet policy file. This grants SSH to the production host **and nothing
else** — not your laptop, not the Dokploy panel, not Grafana:

```jsonc
{
  "tagOwners": {
    "tag:prod": ["sambit.behera581@gmail.com"]
  },

  "grants": [
    // Owner: full access to tagged production hosts.
    {
      "src": ["sambit.behera581@gmail.com"],
      "dst": ["tag:prod"],
      "ip":  ["*"]
    },

    // Other developers: SSH only. Port 3000 (Dokploy) and 3001 (Grafana) are
    // deliberately absent — the panel shows every secret in plaintext.
    {
      "src": ["dev@example.com"],
      "dst": ["tag:prod"],
      "ip":  ["22"]
    }
  ],

  "ssh": [
    {
      // "check" forces a browser re-authentication periodically, so a stolen
      // laptop does not grant indefinite production access.
      "action":      "check",
      "checkPeriod": "12h",
      "src":         ["autogroup:member"],
      "dst":         ["tag:prod"],
      // They land as the unprivileged admin-deploy user. Never root.
      "users":       ["admin-deploy"]
    }
  ]
}
```

### 3. Verify the scoping worked

Have them confirm SSH works and the panel does not:

```bash
ssh admin-deploy@twistedmomos-prod          # succeeds, after a browser re-auth prompt
curl --max-time 5 http://twistedmomos-prod:3000   # must time out
```

If the second command returns anything, the ACL grant is too broad — fix it before
continuing.

### 4. Revoking

Delete their `grants` entry, or remove the user from the tailnet entirely. Access
stops at the next check interval, with no server-side change and no key to hunt down.

## Access boundaries

Deploy rights and shell access are separate concerns. Grant the narrowest one that
does the job:

| They need to | Grant |
|---|---|
| Deploy code | GitHub repository write access — pushing to `main` triggers the pipeline |
| Read logs | A Grafana user account of their own |
| Get a shell | Tailscale ACL, port 22, `admin-deploy` user |
| Read every secret | **Owner only.** Do not share the Dokploy panel. |
