@echo off
setlocal EnableDelayedExpansion

set "TOMCAT_HOME=C:\Program Files (x86)\apache-tomcat-9.0.112"
set "BASE=%~dp0WEB-INF"
set "CP=%TOMCAT_HOME%\lib\servlet-api.jar;%BASE%\classes;."

if not exist "%BASE%\classes" mkdir "%BASE%\classes"

pushd "%BASE%\src" || exit /b 1

javac -encoding UTF-8 -classpath "%CP%" -d "%BASE%\classes" model\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%BASE%\classes" store\*.java || goto :compile_failed
javac -encoding UTF-8 -classpath "%CP%" -d "%BASE%\classes" controller\*.java || goto :compile_failed

popd

echo Compilation completed.

endlocal
exit /b 0

:compile_failed
popd
exit /b 1