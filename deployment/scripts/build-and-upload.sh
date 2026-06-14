#!/bin/bash
# =============================================================
# Local Build + Upload Script
# Run from your LOCAL machine inside the typing-tutor/ project root
#
# Usage: bash deployment-oracle/scripts/build-and-upload.sh
#
# Prerequisites:
#   - Java 21 + Maven installed locally
#   - Node 18+ installed locally
#   - SSH key configured for the VM
# =============================================================
set -euo pipefail

# ============================================================
# CONFIGURE THESE VALUES
# ============================================================
VM_IP="<YOUR_ORACLE_VM_PUBLIC_IP>"
SSH_KEY="$HOME/.ssh/id_rsa"       # Path to your SSH private key
SSH_USER="ubuntu"
DOMAIN="your-domain.duckdns.org"
# ============================================================

echo "=== TypeMaster Build & Deploy ==="
echo "Target: $SSH_USER@$VM_IP"
echo ""

if [ "$VM_IP" = "<YOUR_ORACLE_VM_PUBLIC_IP>" ]; then
    echo "ERROR: Edit this script and set VM_IP before running."
    exit 1
fi

SSH_CMD="ssh -i $SSH_KEY -o StrictHostKeyChecking=accept-new $SSH_USER@$VM_IP"
SCP_CMD="scp -i $SSH_KEY -o StrictHostKeyChecking=accept-new"

# ============================================================
# Step 1: Copy production env to frontend
# ============================================================
echo "--- [1/5] Setting up frontend production env ---"
cp deployment-oracle/frontend-config/.env.production frontend/.env.production
# Make sure the domain is correct
sed -i "s/your-domain.duckdns.org/$DOMAIN/" frontend/.env.production

# ============================================================
# Step 2: Build frontend
# ============================================================
echo "--- [2/5] Building React frontend ---"
cd frontend
npm install --silent
npm run build
cd ..
echo "Frontend built: frontend/dist/"

# ============================================================
# Step 3: Build backend
# ============================================================
echo "--- [3/5] Building Spring Boot JAR ---"
cd backend
./mvnw clean package -DskipTests -q
JAR_FILE=$(ls target/*.jar | grep -v original | head -1)
echo "JAR built: $JAR_FILE"
cd ..

# ============================================================
# Step 4: Upload to VM
# ============================================================
echo "--- [4/5] Uploading to VM ---"

# Upload JAR
$SCP_CMD backend/$JAR_FILE $SSH_USER@$VM_IP:/opt/typemaster/app/app.jar.new
$SSH_CMD "sudo mv /opt/typemaster/app/app.jar.new /opt/typemaster/app/app.jar && sudo chown typemaster:typemaster /opt/typemaster/app/app.jar"
echo "  JAR uploaded"

# Upload frontend build
$SCP_CMD -r frontend/dist/* $SSH_USER@$VM_IP:/tmp/frontend-dist/
$SSH_CMD "sudo rm -rf /opt/typemaster/frontend/* && sudo cp -r /tmp/frontend-dist/* /opt/typemaster/frontend/ && sudo chown -R typemaster:typemaster /opt/typemaster/frontend/ && rm -rf /tmp/frontend-dist"
echo "  Frontend uploaded"

# ============================================================
# Step 5: Restart backend service
# ============================================================
echo "--- [5/5] Restarting backend service ---"
$SSH_CMD "sudo systemctl restart typemaster"
sleep 5
$SSH_CMD "sudo systemctl status typemaster --no-pager"

echo ""
echo "=== Deployment complete ==="
echo "Visit: https://$DOMAIN"
echo ""
echo "To watch logs:"
echo "  ssh -i $SSH_KEY $SSH_USER@$VM_IP 'sudo journalctl -u typemaster -f'"
