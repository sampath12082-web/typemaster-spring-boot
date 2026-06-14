@echo off
echo ================================================
echo  Starting TypeMaster (Backend + Frontend)
echo ================================================
echo.
echo  Backend  -> http://localhost:8080
echo  Frontend -> http://localhost:5173
echo.
start "TypeMaster Backend"  cmd /c "%~dp0start-backend.bat"
timeout /t 5 >nul
start "TypeMaster Frontend" cmd /c "%~dp0start-frontend.bat"
echo Both services launched in separate windows.
