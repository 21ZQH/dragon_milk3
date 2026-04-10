@echo off
setlocal

call "%~dp0..\command2.bat"
exit /b %errorlevel%
