# PowerShell script to locate UMHAI-related files in the HAPI FHIR project
# Run this from the project root: C:\FHIR-Sandbox\hapi-fhir-jpaserver-starter

Write-Host "=== Searching for UMHAI-related Java files ===" -ForegroundColor Cyan
Get-ChildItem -Path . -Recurse -Include "*Umhai*.java" -ErrorAction SilentlyContinue |
    Select-Object FullName |
    Format-Table -AutoSize

Write-Host "`n=== Searching for umhai-data.json (or similar data files) ===" -ForegroundColor Cyan
Get-ChildItem -Path . -Recurse -Include "*umhai*.json" -ErrorAction SilentlyContinue |
    Select-Object FullName, Length, LastWriteTime |
    Format-Table -AutoSize

Write-Host "`n=== Searching source code for file-based read/write patterns (ObjectMapper, FileWriter, Files.write, etc.) ===" -ForegroundColor Cyan
Get-ChildItem -Path .\src -Recurse -Include "*.java" -ErrorAction SilentlyContinue |
    Select-String -Pattern "ObjectMapper|FileWriter|FileReader|Files\.write|Files\.read|new File\(" |
    Select-Object Path, LineNumber, Line |
    Format-Table -AutoSize -Wrap

Write-Host "`n=== Searching for JPA/Repository/EntityManager related config ===" -ForegroundColor Cyan
Get-ChildItem -Path .\src -Recurse -Include "*.java" -ErrorAction SilentlyContinue |
    Select-String -Pattern "packagesToScan|EntityManagerFactory|@EnableJpaRepositories|JpaRepository" |
    Select-Object Path, LineNumber, Line |
    Format-Table -AutoSize -Wrap

Write-Host "`n=== Done. Please review paths above. ===" -ForegroundColor Green
