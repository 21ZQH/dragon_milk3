@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
set "WEB_INF_DIR=%SCRIPT_DIR%WEB-INF"
if not exist "%WEB_INF_DIR%\src" (
    set "WEB_INF_DIR=%SCRIPT_DIR%"
)

set "SRC_DIR=%WEB_INF_DIR%\src"
set "CLASSES_DIR=%WEB_INF_DIR%\classes"
set "API_JAR="

if not exist "%SRC_DIR%" (
    echo Source directory not found: "%SRC_DIR%"
    exit /b 1
)

call :find_api_jar
if not defined API_JAR (
    echo Could not find a servlet API JAR.
    echo Set TOMCAT_HOME or CATALINA_HOME, or install jakarta.servlet-api in Maven local repository.
    exit /b 1
)

if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

set "CP=%API_JAR%;%CLASSES_DIR%;."

pushd "%SRC_DIR%" || exit /b 1

javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" model\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" store\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" listener\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" controller\*.java || goto :compile_failed

popd
echo Compilation completed.
exit /b 0

:find_api_jar
for %%V in (6.1.0 6.0.0 5.0.0) do (
    if not defined API_JAR if exist "%USERPROFILE%\.m2\repository\jakarta\servlet\jakarta.servlet-api\%%V\jakarta.servlet-api-%%V.jar" (
        set "API_JAR=%USERPROFILE%\.m2\repository\jakarta\servlet\jakarta.servlet-api\%%V\jakarta.servlet-api-%%V.jar"
    )
)
if not defined API_JAR if defined TOMCAT_HOME call :set_api_from_tomcat "%TOMCAT_HOME%"
if not defined API_JAR if defined CATALINA_HOME call :set_api_from_tomcat "%CATALINA_HOME%"
if not defined API_JAR if defined CATALINA_BASE call :set_api_from_tomcat "%CATALINA_BASE%"
if not defined API_JAR if exist "G:\Tomcat\lib\servlet-api.jar" set "API_JAR=G:\Tomcat\lib\servlet-api.jar"
if not defined API_JAR if exist "C:\Program Files (x86)\apache-tomcat-9.0.112\lib\servlet-api.jar" set "API_JAR=C:\Program Files (x86)\apache-tomcat-9.0.112\lib\servlet-api.jar"
exit /b 0

:set_api_from_tomcat
if exist "%~1\lib\servlet-api.jar" (
    set "API_JAR=%~1\lib\servlet-api.jar"
    exit /b 0
)
if exist "%~1\lib\jakarta.servlet-api.jar" (
    set "API_JAR=%~1\lib\jakarta.servlet-api.jar"
)
exit /b 0

:compile_failed
popd
echo Compilation failed.
exit /b 1
