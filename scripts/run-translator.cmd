@echo off
setlocal
if "%~1"=="" (
  echo Usage: scripts\run-translator.cmd ^<file.test^>
  exit /b 1
)
set FILE=%~1
call mvn -q -DskipTests compile || exit /b 1
call mvn -q -DincludeScope=compile dependency:build-classpath -Dmdep.outputFile=cp.txt || exit /b 1
for /f "usebackq tokens=*" %%i in ("cp.txt") do set CP=%%i
set CP=%CP%;target\classes
java -cp "%CP%" translator.Translator "%FILE%"
endlocal

