#!/bin/bash
# =============================================================
# Script 01 — VM System Setup
# Run as: sudo bash 01-vm-setup.sh
# Ubuntu 22.04 on Oracle Cloud ARM Ampere A1
# =============================================================
set -euo pipefail

echo "=== [1/4] Updating system packages ==="
apt-get update -y
apt-get upgrade -y
apt-get install -y \
    curl wget git unzip \
    software-properties-common \
    apt-transport-https \
    ca-certificates \
    gnupg \
    ufw \
    fail2ban

echo "=== [2/4] Configuring UFW firewall ==="
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh
ufw allow 80/tcp
ufw allow 443/tcp
# Internal PostgreSQL and Spring Boot ports stay blocked from outside
ufw --force enable
ufw status

echo "=== [3/4] Hardening SSH ==="
# Disable password auth (key-only login)
sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sed -i 's/PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
systemctl reload sshd

echo "=== [4/4] Creating app user and directories ==="
# Create a dedicated non-root user to run the app
id -u typemaster &>/dev/null || useradd -m -s /bin/bash typemaster

# App directories
mkdir -p /opt/typemaster/app
mkdir -p /opt/typemaster/frontend
mkdir -p /etc/typemaster
mkdir -p /var/log/typemaster

chown -R typemaster:typemaster /opt/typemaster
chown -R typemaster:typemaster /var/log/typemaster
chmod 750 /etc/typemaster

echo ""
echo "=== Script 01 complete. Reboot recommended: sudo reboot ==="
