@echo off
title TypeMaster Backend
echo ================================================
echo  TypeMaster Backend  ^|  http://localhost:8080
echo ================================================
cd /d "%~dp0..\backend"
C:\Apache\maven\bin\mvn.cmd spring-boot:run
pause
