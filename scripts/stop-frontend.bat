@echo off
echo Stopping TypeMaster Frontend (port 5173)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173 " ^| findstr "LISTENING"') do (
    echo   Killing PID %%p
    taskkill /PID %%p /F >nul 2>&1
)
echo Frontend stopped.
timeout /t 2 >nul
