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

Steps are filled in by the tasks that follow.
