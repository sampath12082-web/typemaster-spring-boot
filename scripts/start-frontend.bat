@echo off
title TypeMaster Frontend
echo ================================================
echo  TypeMaster Frontend  ^|  http://localhost:5173
echo ================================================
cd /d "%~dp0..\frontend"
npm run dev
pause
