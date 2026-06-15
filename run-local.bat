@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
cd /d "%~dp0"
"C:\Users\SAMPAT KUMAR ASEALU\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6\bin\mvn.cmd" clean spring-boot:run -Dspring-boot.run.profiles=local
