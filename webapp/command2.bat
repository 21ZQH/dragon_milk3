@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
set "WEB_INF_DIR=%SCRIPT_DIR%WEB-INF"
if not exist "%WEB_INF_DIR%\src" (
    set "WEB_INF_DIR=%SCRIPT_DIR%"
)

set "SRC_DIR=%WEB_INF_DIR%\src"
set "CLASSES_DIR=%WEB_INF_DIR%\classes"
set "LIB_DIR=%WEB_INF_DIR%\lib"
set "API_JAR="
set "PDFBOX_CP="

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

call :find_pdfbox_jars
if not defined PDFBOX_CP (
    echo Could not find Apache PDFBox JARs.
    echo Run "mvn -DskipTests compile" from the project root first.
    exit /b 1
)
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
call :copy_pdfbox_jars

set "CP=%API_JAR%;%PDFBOX_CP%;%CLASSES_DIR%;."

pushd "%SRC_DIR%" || exit /b 1

javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" model\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" store\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" service\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" service\ai\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" service\ai\impl\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%CLASSES_DIR%" service\impl\*.java || goto :compile_failed
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

:find_pdfbox_jars
set "PDFBOX_VERSION=3.0.3"
set "COMMONS_LOGGING_VERSION=1.3.3"
set "PDFBOX_JAR=%USERPROFILE%\.m2\repository\org\apache\pdfbox\pdfbox\%PDFBOX_VERSION%\pdfbox-%PDFBOX_VERSION%.jar"
set "FONTBOX_JAR=%USERPROFILE%\.m2\repository\org\apache\pdfbox\fontbox\%PDFBOX_VERSION%\fontbox-%PDFBOX_VERSION%.jar"
set "PDFBOX_IO_JAR=%USERPROFILE%\.m2\repository\org\apache\pdfbox\pdfbox-io\%PDFBOX_VERSION%\pdfbox-io-%PDFBOX_VERSION%.jar"
set "COMMONS_LOGGING_JAR=%USERPROFILE%\.m2\repository\commons-logging\commons-logging\%COMMONS_LOGGING_VERSION%\commons-logging-%COMMONS_LOGGING_VERSION%.jar"
if exist "%PDFBOX_JAR%" if exist "%FONTBOX_JAR%" if exist "%PDFBOX_IO_JAR%" if exist "%COMMONS_LOGGING_JAR%" (
    set "PDFBOX_CP=%PDFBOX_JAR%;%FONTBOX_JAR%;%PDFBOX_IO_JAR%;%COMMONS_LOGGING_JAR%"
)
exit /b 0

:copy_pdfbox_jars
copy /Y "%PDFBOX_JAR%" "%LIB_DIR%\" >nul
copy /Y "%FONTBOX_JAR%" "%LIB_DIR%\" >nul
copy /Y "%PDFBOX_IO_JAR%" "%LIB_DIR%\" >nul
copy /Y "%COMMONS_LOGGING_JAR%" "%LIB_DIR%\" >nul
exit /b 0

:compile_failed
popd
echo Compilation failed.
exit /b 1
