#!/bin/sh
# Maven Wrapper for Linux/Mac
MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    mkdir -p "$(dirname "$MAVEN_WRAPPER_JAR")"
    curl -o "$MAVEN_WRAPPER_JAR" "$DOWNLOAD_URL"
fi

exec java -jar "$MAVEN_WRAPPER_JAR" "$@"
