@echo off
echo ================================================
echo  Stopping TypeMaster (Backend + Frontend)
echo ================================================
call "%~dp0stop-backend.bat"
call "%~dp0stop-frontend.bat"
echo All services stopped.
pause
