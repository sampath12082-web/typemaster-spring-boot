#!/bin/bash
# =============================================================
# Script 05 — Install Nginx + Let's Encrypt SSL
# Run as: sudo bash 05-nginx-ssl.sh <your-domain>
# Example: sudo bash 05-nginx-ssl.sh typemaster.duckdns.org
# =============================================================
set -euo pipefail

DOMAIN="${1:-}"
if [ -z "$DOMAIN" ]; then
    echo "Usage: sudo bash 05-nginx-ssl.sh <your-domain>"
    echo "Example: sudo bash 05-nginx-ssl.sh typemaster.duckdns.org"
    exit 1
fi

echo "=== [1/4] Installing Nginx ==="
apt-get update -y
apt-get install -y nginx

echo "=== [2/4] Configuring Nginx for $DOMAIN ==="
NGINX_CONF_SRC="$(dirname "$0")/../config/nginx.conf"
NGINX_SITE="/etc/nginx/sites-available/typemaster"

# Replace placeholder domain in config
sed "s/YOUR_DOMAIN/${DOMAIN}/g" "$NGINX_CONF_SRC" > "$NGINX_SITE"

# Enable site
ln -sf "$NGINX_SITE" /etc/nginx/sites-enabled/typemaster
rm -f /etc/nginx/sites-enabled/default

# Test config
nginx -t
systemctl restart nginx
systemctl enable nginx
echo "Nginx running on port 80"

echo ""
echo "=== [3/4] Installing Certbot (Let's Encrypt) ==="
apt-get install -y certbot python3-certbot-nginx

echo ""
echo "=== [4/4] Obtaining SSL certificate ==="
echo "Domain: $DOMAIN"
echo ""
# Dry run first to check everything is correct
echo "Running dry run first..."
certbot certonly --nginx -d "$DOMAIN" --dry-run --non-interactive --agree-tos \
    --email admin@${DOMAIN} 2>/dev/null \
    || echo "(Dry run failed — DNS may not be propagated yet. Wait a few minutes and re-run.)"

echo ""
echo "If dry run passed, run the real cert request:"
echo "  sudo certbot --nginx -d $DOMAIN"
echo ""
echo "After cert is issued, Nginx will auto-redirect HTTP to HTTPS."
echo ""
echo "=== Script 05 complete ==="
echo ""
echo "Auto-renewal is set up automatically by certbot."
echo "Test renewal: sudo certbot renew --dry-run"
