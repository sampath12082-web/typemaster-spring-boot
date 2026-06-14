@echo off
echo Stopping TypeMaster Backend (port 8080)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo   Killing PID %%p
    taskkill /PID %%p /F >nul 2>&1
)
echo Backend stopped.
timeout /t 2 >nul
