$ErrorActionPreference = 'Stop'
$destino = Join-Path (Split-Path $PSScriptRoot -Parent) '.local\native'
New-Item -ItemType Directory -Force -Path $destino | Out-Null
$archivo = Join-Path $destino 'mssql-jdbc_auth-12.10.2.x64.dll'
if (-not (Test-Path -LiteralPath $archivo)) {
    Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/com/microsoft/sqlserver/mssql-jdbc_auth/12.10.2.x64/mssql-jdbc_auth-12.10.2.x64.dll' -OutFile $archivo
}
if ((Get-Item -LiteralPath $archivo).Length -eq 0) { throw 'El controlador descargado está vacío.' }
Write-Output 'Controlador oficial preparado en .local/native, fuera de Git. Compatible con Java x64.'
