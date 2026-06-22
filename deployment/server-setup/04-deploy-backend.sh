#!/bin/bash
# =============================================================
# Script 04 — Deploy Spring Boot as systemd Service
# Run as: sudo bash 04-deploy-backend.sh
# Prerequisites: Java 21 installed, JAR uploaded to /opt/typemaster/app/
# =============================================================
set -euo pipefail

APP_DIR="/opt/typemaster/app"
SERVICE_FILE="/etc/systemd/system/typemaster.service"
ENV_FILE="/etc/typemaster/typemaster.env"

echo "=== [1/4] Creating environment file ==="
if [ ! -f "$ENV_FILE" ]; then
    # Generate a random JWT secret (base64, 44 chars)
    JWT_SECRET=$(openssl rand -base64 32)
    # Generate a random DB password
    DB_PASSWORD=$(openssl rand -base64 16 | tr -dc 'a-zA-Z0-9' | head -c 20)

    cat > "$ENV_FILE" << EOF
# TypeMaster Production Environment Variables
# EDIT THIS FILE before starting the service

DATABASE_URL=jdbc:postgresql://localhost:5432/typemaster
DATABASE_USERNAME=typemaster
DATABASE_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET}

# Gmail SMTP — yourtypemaster@gmail.com is the standard sender for OTPs and support.
# MAIL_PASSWORD must be a Gmail "App Password" (Google Account > Security > App Passwords;
# requires 2FA enabled on the account), NOT the regular account password.
# If OTP volume grows or Gmail's sending limits/throttling become a problem, switch to a
# dedicated transactional provider (Brevo, SendGrid, etc.) with yourtypemaster@gmail.com
# configured as the verified "From" sender there instead.
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=yourtypemaster@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# Your domain (set after DNS is configured)
FRONTEND_URL=https://your-domain.duckdns.org
EOF

    chmod 640 "$ENV_FILE"
    chown root:typemaster "$ENV_FILE"
    echo "Created $ENV_FILE"
    echo ""
    echo ">>> IMPORTANT: Edit $ENV_FILE with your real values before continuing <<<"
    echo "    Especially: MAIL_USERNAME, MAIL_PASSWORD, FRONTEND_URL"
    echo ""
    echo "Also update PostgreSQL with the generated password:"
    echo "  sudo -u postgres psql -c \"ALTER USER typemaster PASSWORD '${DB_PASSWORD}';\""
else
    echo "$ENV_FILE already exists — skipping creation"
fi

echo ""
echo "=== [2/4] Installing systemd service ==="
cp "$(dirname "$0")/../config/typemaster.service" "$SERVICE_FILE"
systemctl daemon-reload

echo ""
echo "=== [3/4] Setting permissions ==="
chown -R typemaster:typemaster "$APP_DIR"
chmod 755 "$APP_DIR"

echo ""
echo "=== [4/4] Enabling service (start after JAR is deployed) ==="
systemctl enable typemaster

echo ""
echo "=== Script 04 complete ==="
echo ""
echo "Next steps:"
echo "  1. Edit $ENV_FILE with real values"
echo "  2. Upload the JAR: scp target/typing-tutor-*.jar ubuntu@VM_IP:/opt/typemaster/app/app.jar"
echo "  3. Start the service: sudo systemctl start typemaster"
echo "  4. Check logs: sudo journalctl -u typemaster -f"
