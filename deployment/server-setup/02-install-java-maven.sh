#!/bin/bash
# =============================================================
# Script 02 — Install Java 21 (Temurin) + Maven 3.9
# Run as: sudo bash 02-install-java-maven.sh
# =============================================================
set -euo pipefail

echo "=== [1/3] Installing Java 21 (Eclipse Temurin) ==="
# Add Adoptium GPG key and repo
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] \
    https://packages.adoptium.net/artifactory/deb \
    $(lsb_release -cs) main" \
    | tee /etc/apt/sources.list.d/adoptium.list

apt-get update -y
apt-get install -y temurin-21-jdk

echo "Java version:"
java -version

echo ""
echo "=== [2/3] Installing Maven 3.9 ==="
MAVEN_VERSION="3.9.6"
MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

wget -q "$MAVEN_URL" -O /tmp/maven.tar.gz
tar -xzf /tmp/maven.tar.gz -C /opt
ln -sfn "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven
rm /tmp/maven.tar.gz

# Add to PATH system-wide
cat > /etc/profile.d/maven.sh << 'EOF'
export JAVA_HOME=/usr/lib/jvm/temurin-21-amd64
export M2_HOME=/opt/maven
export PATH=${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}
EOF

# Also add for ARM architecture (path differs)
JAVA_PATH=$(readlink -f $(which java) | sed 's|/bin/java||')
sed -i "s|/usr/lib/jvm/temurin-21-amd64|${JAVA_PATH}|" /etc/profile.d/maven.sh

source /etc/profile.d/maven.sh
echo "Maven version:"
mvn -version

echo ""
echo "=== [3/3] Verifying installations ==="
java -version
mvn -version

echo ""
echo "=== Script 02 complete ==="
