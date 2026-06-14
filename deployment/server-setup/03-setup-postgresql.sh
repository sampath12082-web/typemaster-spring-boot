#!/bin/bash
# =============================================================
# Script 03 — Install and Configure PostgreSQL 16
# Run as: sudo bash 03-setup-postgresql.sh
# =============================================================
set -euo pipefail

echo "=== [1/4] Installing PostgreSQL 16 ==="
# Add PostgreSQL official repo
curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
    | gpg --dearmor -o /usr/share/keyrings/postgresql.gpg
echo "deb [signed-by=/usr/share/keyrings/postgresql.gpg] \
    https://apt.postgresql.org/pub/repos/apt \
    $(lsb_release -cs)-pgdg main" \
    | tee /etc/apt/sources.list.d/pgdg.list

apt-get update -y
apt-get install -y postgresql-16 postgresql-client-16

echo "=== [2/4] Starting and enabling PostgreSQL ==="
systemctl start postgresql
systemctl enable postgresql
systemctl status postgresql --no-pager

echo ""
echo "=== [3/4] Creating database, user, and schema ==="
# Run the init SQL as postgres superuser
sudo -u postgres psql -f "$(dirname "$0")/../config/init-db.sql"

echo ""
echo "=== [4/4] Configuring PostgreSQL to listen on localhost only ==="
PG_CONF=$(sudo -u postgres psql -t -c "SHOW config_file;" | tr -d ' ')
PG_HBA=$(sudo -u postgres psql -t -c "SHOW hba_file;" | tr -d ' ')

# Ensure listen_addresses = 'localhost' (default, but be explicit)
sed -i "s/#listen_addresses = 'localhost'/listen_addresses = 'localhost'/" "$PG_CONF"
sed -i "s/listen_addresses = '\*'/listen_addresses = 'localhost'/" "$PG_CONF"

systemctl reload postgresql

echo ""
echo "=== PostgreSQL setup complete ==="
echo "Database: typemaster"
echo "User: typemaster"
echo "Password: (set in /etc/typemaster/typemaster.env)"
echo ""
echo "=== Script 03 complete ==="
