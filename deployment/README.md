# TypeMaster — Oracle Cloud Always Free Deployment Guide

## Architecture Overview

```
Internet
   │
   ▼
Oracle Cloud VM (ARM Ampere A1 — 2 OCPU, 12 GB RAM)
   ├── Nginx  (port 80/443)  ← SSL termination, reverse proxy
   │     ├── /api/*  → Spring Boot :8080
   │     └── /*      → React static files
   ├── Spring Boot (port 8080, internal only)
   └── PostgreSQL  (port 5432, internal only)
```

Everything runs on a single VM. Zero external services required. Zero cost — forever.

---

## Prerequisites

- Oracle Cloud account (free): https://cloud.oracle.com/free
- A domain name (free options: https://freedns.afraid.org or https://www.duckdns.org)
- A local machine with: Java 21, Maven, Node 18+, Git, SSH client

---

## Step 1 — Create Oracle Cloud Account

1. Go to https://cloud.oracle.com/free and sign up.
2. Use a real credit card for verification — you will NOT be charged.
3. Choose your home region carefully (cannot be changed). Pick the region closest to your users.
4. Wait for account activation (usually instant, sometimes up to 24 hours).

---

## Step 2 — Provision the VM

### 2a. Create a Compute Instance

1. Log in → Compute → Instances → **Create Instance**
2. Name: `typemaster-vm`
3. Image: **Ubuntu 22.04** (Canonical)
4. Shape: Change to **VM.Standard.A1.Flex** (ARM Ampere)
   - OCPUs: **2**
   - Memory: **12 GB**
   - This is within the always-free 4 OCPU / 24 GB allocation
5. Networking: Create new VCN or use existing. Ensure **Assign public IPv4** is checked.
6. SSH Keys: Upload your public key (`~/.ssh/id_rsa.pub`) or download the generated key.
7. Boot volume: **50 GB** (free up to 200 GB total)
8. Click **Create**.

### 2b. Note your public IP

Once the instance is RUNNING, note the **Public IP address**. You'll use this throughout.

### 2c. Open firewall ports in Oracle Cloud

Go to: Networking → Virtual Cloud Networks → your VCN → Security Lists → Default Security List

Add **Ingress Rules**:

| Stateless | Source CIDR | Protocol | Port | Description |
|-----------|-------------|----------|------|-------------|
| No | 0.0.0.0/0 | TCP | 80 | HTTP |
| No | 0.0.0.0/0 | TCP | 443 | HTTPS |

Port 22 (SSH) should already be open.

---

## Step 3 — Point a Domain to Your VM

### Free domain options

**Option A — DuckDNS** (easiest, e.g., `typemaster.duckdns.org`):
1. Go to https://www.duckdns.org → sign in with Google
2. Add a subdomain, point it to your VM's public IP
3. Your app will be at: `https://typemaster.duckdns.org`

**Option B — Afraid FreeDNS** (more options, e.g., `typemaster.mooo.com`):
1. Go to https://freedns.afraid.org → register free
2. Add an A record pointing your subdomain to the VM IP

Once done, verify DNS is resolving:
```bash
nslookup typemaster.duckdns.org
# Should return your VM's public IP
```

---

## Step 4 — SSH Into the VM

```bash
ssh -i ~/.ssh/your-key.pem ubuntu@<YOUR_VM_PUBLIC_IP>
```

Once connected, run all scripts from `server-setup/` in order.

---

## Step 5 — Run Server Setup Scripts

Copy the `deployment-oracle/` folder to your VM:
```bash
scp -i ~/.ssh/your-key.pem -r deployment-oracle/ ubuntu@<YOUR_VM_PUBLIC_IP>:~/
```

Then SSH in and run each script in order:

```bash
ssh -i ~/.ssh/your-key.pem ubuntu@<YOUR_VM_PUBLIC_IP>

chmod +x ~/deployment-oracle/server-setup/*.sh

# Run in order:
sudo ~/deployment-oracle/server-setup/01-vm-setup.sh
sudo ~/deployment-oracle/server-setup/02-install-java-maven.sh
sudo ~/deployment-oracle/server-setup/03-setup-postgresql.sh
# (At this point: build the app locally — see Step 6)
sudo ~/deployment-oracle/server-setup/04-deploy-backend.sh
sudo ~/deployment-oracle/server-setup/05-nginx-ssl.sh
```

---

## Step 6 — Build and Upload the Application

Run this from your **local machine** (inside the `typing-tutor/` project root):

```bash
# Edit scripts/build-and-upload.sh to set VM_IP and SSH_KEY
nano deployment-oracle/scripts/build-and-upload.sh

# Then run it
bash deployment-oracle/scripts/build-and-upload.sh
```

This script:
1. Builds the Spring Boot JAR (`mvn clean package`)
2. Builds the React frontend (`npm run build`)
3. Uploads the JAR to the VM
4. Uploads the frontend `dist/` to the VM
5. Restarts the backend service

---

## Step 7 — Configure SSL (HTTPS)

After `05-nginx-ssl.sh` has run and Nginx is serving HTTP traffic:

```bash
sudo certbot --nginx -d your-domain.duckdns.org
```

Certbot will:
1. Verify you own the domain (via HTTP challenge)
2. Obtain a free Let's Encrypt certificate
3. Auto-update your Nginx config for HTTPS
4. Set up auto-renewal (cron job)

---

## Step 8 — Set Environment Variables

Edit `/etc/typemaster/typemaster.env` on the VM:

```bash
sudo nano /etc/typemaster/typemaster.env
```

Fill in:
```
DATABASE_URL=jdbc:postgresql://localhost:5432/typemaster
DATABASE_USERNAME=typemaster
DATABASE_PASSWORD=<choose a strong password>
JWT_SECRET=<base64 encoded 32-byte random string>
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=yourtypemaster@gmail.com
MAIL_PASSWORD=<Gmail App Password — Google Account > Security > App Passwords, requires 2FA>
FRONTEND_URL=https://your-domain.duckdns.org
```

Then restart:
```bash
sudo systemctl restart typemaster
```

---

## Step 9 — Verify Deployment

```bash
# Check backend is running
sudo systemctl status typemaster
curl http://localhost:8080/api/auth/me  # should return 401 (app is running)

# Check Nginx is running
sudo systemctl status nginx
curl http://your-domain.duckdns.org/api/auth/me  # proxied through nginx

# Check PostgreSQL
sudo systemctl status postgresql
```

Visit `https://your-domain.duckdns.org` in your browser. The TypeMaster app should load.

---

## Maintenance

### View backend logs
```bash
sudo journalctl -u typemaster -f
```

### Restart services
```bash
sudo systemctl restart typemaster
sudo systemctl restart nginx
sudo systemctl restart postgresql
```

### Redeploy after code changes
```bash
# From your local machine:
bash deployment-oracle/scripts/build-and-upload.sh
```

### SSL certificate renewal
Certbot auto-renews. To manually renew:
```bash
sudo certbot renew
```

---

## Cost Summary

| Resource | Service | Cost |
|----------|---------|------|
| VM (2 OCPU, 12 GB RAM) | Oracle Cloud Ampere A1 | $0 forever |
| Block storage (50 GB) | Oracle Cloud | $0 forever |
| PostgreSQL | Self-hosted on VM | $0 |
| SSL certificate | Let's Encrypt | $0 |
| Domain name | DuckDNS / FreeDNS | $0 |
| Email (~500/day) | Gmail SMTP (yourtypemaster@gmail.com) | $0 |
| **Total** | | **$0/month** |

---

## Folder Structure

```
deployment-oracle/
├── README.md                       ← This file
├── backend-config/
│   ├── application-prod.properties ← Copy to backend/src/main/resources/
│   ├── pom-additions.xml           ← Dependencies to add to pom.xml
│   └── SecurityConfig-cors-patch.java  ← CORS config to add to SecurityConfig.java
├── server-setup/
│   ├── 01-vm-setup.sh              ← System packages + firewall
│   ├── 02-install-java-maven.sh    ← Java 21 + Maven
│   ├── 03-setup-postgresql.sh      ← PostgreSQL 16 setup
│   ├── 04-deploy-backend.sh        ← systemd service creation
│   └── 05-nginx-ssl.sh             ← Nginx + Certbot
├── config/
│   ├── nginx.conf                  ← Nginx virtual host
│   ├── typemaster.service          ← systemd unit file
│   └── init-db.sql                 ← PostgreSQL DB/user creation
├── frontend-config/
│   └── .env.production             ← Copy to frontend/.env.production
└── scripts/
    └── build-and-upload.sh         ← Local script: build + deploy to VM
```
