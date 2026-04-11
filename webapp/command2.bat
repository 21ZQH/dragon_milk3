@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "WEB_INF_DIR=%SCRIPT_DIR%WEB-INF"
if not exist "%WEB_INF_DIR%\src" (
    set "WEB_INF_DIR=%SCRIPT_DIR%"
)

set "SRC_DIR=%WEB_INF_DIR%\src"
set "CLASSES_DIR=%WEB_INF_DIR%\classes"
set "SERVLET_API_JAR="
set "JAKARTA_API_JAR="

if not exist "%SRC_DIR%" (
    echo Source directory not found: "%SRC_DIR%"
    exit /b 1
)

if exist "%USERPROFILE%\.m2\repository\jakarta\servlet\jakarta.servlet-api\6.0.0\jakarta.servlet-api-6.0.0.jar" (
    set "JAKARTA_API_JAR=%USERPROFILE%\.m2\repository\jakarta\servlet\jakarta.servlet-api\6.0.0\jakarta.servlet-api-6.0.0.jar"
)
if not defined JAKARTA_API_JAR if exist "%USERPROFILE%\.m2\repository\jakarta\servlet\jakarta.servlet-api\5.0.0\jakarta.servlet-api-5.0.0.jar" (
    set "JAKARTA_API_JAR=%USERPROFILE%\.m2\repository\jakarta\servlet\jakarta.servlet-api\5.0.0\jakarta.servlet-api-5.0.0.jar"
)

if defined CATALINA_HOME if exist "%CATALINA_HOME%\lib\servlet-api.jar" (
    set "SERVLET_API_JAR=%CATALINA_HOME%\lib\servlet-api.jar"
)
if not defined SERVLET_API_JAR if exist "G:\Tomcat\lib\servlet-api.jar" (
    set "SERVLET_API_JAR=G:\Tomcat\lib\servlet-api.jar"
)
if not defined SERVLET_API_JAR if exist "C:\Program Files (x86)\apache-tomcat-9.0.112\lib\servlet-api.jar" (
    set "SERVLET_API_JAR=C:\Program Files (x86)\apache-tomcat-9.0.112\lib\servlet-api.jar"
)

if not defined SERVLET_API_JAR if not defined JAKARTA_API_JAR (
    echo Could not find jakarta.servlet-api.jar or servlet-api.jar.
    echo Please install dependency in Maven local repository or set CATALINA_HOME.
    exit /b 1
)

if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

if defined SERVLET_API_JAR (
    set "CP=%SERVLET_API_JAR%;%CLASSES_DIR%;."
) else (
    set "CP=%JAKARTA_API_JAR%;%CLASSES_DIR%;."
)

pushd "%SRC_DIR%" || exit /b 1

javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" model\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" store\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" listener\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" controller\*.java || goto :compile_failed

popd
echo Compilation completed.
exit /b 0

:compile_failed
popd
echo Compilation failed.
exit /b 1
