param([int]$Puerto = 8080, [string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
if (-not $JavaHome) {
    $jdk = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jdk) { $JavaHome = $jdk.FullName }
}
if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome 'bin\java.exe'))) {
    throw 'Configura JAVA_HOME con un JDK 21 o usa -JavaHome con su carpeta.'
}
$nativo = Join-Path $proyecto '.local\native'
$dll = Join-Path $nativo 'mssql-jdbc_auth-12.10.2.x64.dll'
if (-not (Test-Path -LiteralPath $dll)) {
    throw 'Falta el controlador de Windows. Ejecuta primero scripts\Preparar-Local.ps1.'
}
$env:JAVA_HOME = $JavaHome
$env:PATH = "$nativo;$env:PATH"
Push-Location $proyecto
try {
    & '.\mvnw.cmd' spring-boot:run '-Dspring-boot.run.profiles=sqlserver' "-Dspring-boot.run.arguments=--server.port=$Puerto"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar ENSEÑA-ME. Revisa el mensaje anterior.' }
} finally { Pop-Location }
