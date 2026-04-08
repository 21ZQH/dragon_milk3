@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "SOURCE_WEBAPP=%SCRIPT_DIR%webapp"
set "TOMCAT_HOME=C:\Program Files (x86)\apache-tomcat-9.0.112"
set "TARGET_DIR=%TOMCAT_HOME%\webapps\SE"
set "BACKUP_DIR=%TEMP%\SE_file_backup"
set "TOMCAT_STARTUP=%TOMCAT_HOME%\bin\startup.bat"

echo [1/5] Backing up runtime data...
if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
if exist "%TARGET_DIR%\WEB-INF\file" (
    robocopy "%TARGET_DIR%\WEB-INF\file" "%BACKUP_DIR%" /E >nul
    if errorlevel 8 goto :copy_failed
)

echo [2/5] Deploying project to %TARGET_DIR%...
if exist "%TARGET_DIR%" rmdir /s /q "%TARGET_DIR%"
mkdir "%TARGET_DIR%"
robocopy "%SOURCE_WEBAPP%" "%TARGET_DIR%" /E >nul
if errorlevel 8 goto :copy_failed

echo [3/5] Restoring runtime data...
if not exist "%TARGET_DIR%\WEB-INF\file" mkdir "%TARGET_DIR%\WEB-INF\file"
if exist "%BACKUP_DIR%" (
    robocopy "%BACKUP_DIR%" "%TARGET_DIR%\WEB-INF\file" /E >nul
    if errorlevel 8 goto :copy_failed
)
if not exist "%TARGET_DIR%\WEB-INF\file\users.txt" type nul > "%TARGET_DIR%\WEB-INF\file\users.txt"
if not exist "%TARGET_DIR%\WEB-INF\file\courses.txt" type nul > "%TARGET_DIR%\WEB-INF\file\courses.txt"
if exist "%SOURCE_WEBAPP%\WEB-INF\file\candidates.txt" (
    copy /Y "%SOURCE_WEBAPP%\WEB-INF\file\candidates.txt" "%TARGET_DIR%\WEB-INF\file\candidates.txt" >nul
)

echo [4/5] Compiling Java sources...
pushd "%TARGET_DIR%\WEB-INF"
call "..\command2.bat"
if errorlevel 1 goto :compile_failed
popd

echo [5/5] Starting Tomcat...
if not exist "%TOMCAT_STARTUP%" goto :tomcat_start_failed
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"
call "%TOMCAT_STARTUP%"
if errorlevel 1 goto :tomcat_start_failed
echo Tomcat startup script executed.

echo Deployment finished.
echo Open: http://localhost:8081/SE/start.html
exit /b 0

:copy_failed
echo Deployment copy failed.
exit /b 1

:compile_failed
popd
echo Compilation failed.
exit /b 1

:tomcat_start_failed
echo Tomcat start failed. Please check %TOMCAT_STARTUP%.
exit /b 1
