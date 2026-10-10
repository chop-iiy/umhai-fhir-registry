@echo off
echo ===================================================
echo [UMHAI] Initializing Server Environment Variables...
echo ===================================================

SET "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
SET "PATH=%JAVA_HOME%\bin;C:\Software\apache-maven-3.9.16\bin;%PATH%"

echo [UMHAI] Verifying Active Java Target Version...
java -version

echo.
echo ===================================================
echo [UMHAI] Launching HAPI FHIR JPA Registry Engine...
echo ===================================================

cd /d "%~dp0"
mvn spring-boot:run