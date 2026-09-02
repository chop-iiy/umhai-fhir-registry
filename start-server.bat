@echo off
TITLE UMHAI Registry HAPI FHIR Server Launcher
echo ===================================================
echo [UMHAI] Initializing Server Environment Variables...
echo ===================================================

SET "JAVA_HOME=C:\Program Files\Java\jdk-17"
SET "PATH=%JAVA_HOME%\bin;%PATH%"

echo [UMHAI] Verifying Active Java Target Version...
java -version
echo.
echo ===================================================
echo [UMHAI] Launching HAPI FHIR JPA Registry Engine...
echo ===================================================
cd /d "C:\FHIR-Sandbox\hapi-fhir-jpaserver-starter"
mvn spring-boot:run
pause

