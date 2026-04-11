@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
set "SOURCE_WEBAPP=%SCRIPT_DIR%webapp"
set "BACKUP_DIR=%TEMP%\SE_file_backup"
set "TARGET_DIR="
set "TOMCAT_HOME="
set "TOMCAT_STARTUP="

if not exist "%SOURCE_WEBAPP%\WEB-INF" (
    echo Source webapp directory not found: "%SOURCE_WEBAPP%"
    exit /b 1
)

call :resolve_tomcat_home
if not defined TOMCAT_HOME (
    echo Could not locate Tomcat.
    echo Set TOMCAT_HOME or CATALINA_HOME to your Tomcat 11 installation directory.
    exit /b 1
)

set "TARGET_DIR=%TOMCAT_HOME%\webapps\SE"
set "TOMCAT_STARTUP=%TOMCAT_HOME%\bin\startup.bat"

echo [1/5] Using Tomcat at: %TOMCAT_HOME%
echo [2/5] Backing up runtime data...
if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
if exist "%TARGET_DIR%\WEB-INF\file" (
    robocopy "%TARGET_DIR%\WEB-INF\file" "%BACKUP_DIR%" /E >nul
    if errorlevel 8 goto :copy_failed
)

echo [3/5] Deploying project to %TARGET_DIR%...
if exist "%TARGET_DIR%" rmdir /s /q "%TARGET_DIR%"
mkdir "%TARGET_DIR%" >nul
robocopy "%SOURCE_WEBAPP%" "%TARGET_DIR%" /E >nul
if errorlevel 8 goto :copy_failed

echo [4/5] Restoring runtime data...
if not exist "%TARGET_DIR%\WEB-INF\file" mkdir "%TARGET_DIR%\WEB-INF\file"
if exist "%BACKUP_DIR%" (
    robocopy "%BACKUP_DIR%" "%TARGET_DIR%\WEB-INF\file" /E >nul
    if errorlevel 8 goto :copy_failed
)
if not exist "%TARGET_DIR%\WEB-INF\file\users.txt" type nul > "%TARGET_DIR%\WEB-INF\file\users.txt"
if not exist "%TARGET_DIR%\WEB-INF\file\courses.txt" type nul > "%TARGET_DIR%\WEB-INF\file\courses.txt"
if not exist "%TARGET_DIR%\WEB-INF\file\deadline.txt" type nul > "%TARGET_DIR%\WEB-INF\file\deadline.txt"
if not exist "%TARGET_DIR%\WEB-INF\file\mo-deadline.txt" type nul > "%TARGET_DIR%\WEB-INF\file\mo-deadline.txt"
if exist "%SOURCE_WEBAPP%\WEB-INF\file\candidates.txt" (
    copy /Y "%SOURCE_WEBAPP%\WEB-INF\file\candidates.txt" "%TARGET_DIR%\WEB-INF\file\candidates.txt" >nul
)

echo [5/5] Compiling and starting Tomcat...
pushd "%TARGET_DIR%\WEB-INF"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"
call "..\command2.bat"
if errorlevel 1 goto :compile_failed
popd

if not exist "%TOMCAT_STARTUP%" goto :tomcat_start_failed
call "%TOMCAT_STARTUP%"
if errorlevel 1 goto :tomcat_start_failed

echo Deployment finished.
echo Open: http://localhost:8081/SE/start.html
exit /b 0

:resolve_tomcat_home
if defined TOMCAT_HOME if exist "%TOMCAT_HOME%\bin\startup.bat" goto :resolved
if defined CATALINA_HOME if exist "%CATALINA_HOME%\bin\startup.bat" (
    set "TOMCAT_HOME=%CATALINA_HOME%"
    goto :resolved
)
if exist "G:\Tomcat\bin\startup.bat" (
    set "TOMCAT_HOME=G:\Tomcat"
    goto :resolved
)
for %%D in ("C:\Program Files\Apache Software Foundation\Tomcat 11.*") do (
    if exist "%%~fD\bin\startup.bat" (
        set "TOMCAT_HOME=%%~fD"
        goto :resolved
    )
)
for %%D in ("C:\Program Files\apache-tomcat-11.*") do (
    if exist "%%~fD\bin\startup.bat" (
        set "TOMCAT_HOME=%%~fD"
        goto :resolved
    )
)
if exist "C:\Program Files (x86)\apache-tomcat-9.0.112\bin\startup.bat" (
    set "TOMCAT_HOME=C:\Program Files (x86)\apache-tomcat-9.0.112"
)
:resolved
exit /b 0

:copy_failed
echo Deployment copy failed.
exit /b 1

:compile_failed
popd
echo Compilation failed.
exit /b 1

:tomcat_start_failed
echo Tomcat start failed. Please check "%TOMCAT_STARTUP%".
exit /b 1
