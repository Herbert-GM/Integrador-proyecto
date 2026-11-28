[CmdletBinding()]
param([ValidateRange(1024,65535)][int]$Puerto=8090, [string]$JavaHome=$env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
$jar = Join-Path $proyecto 'target/ensename-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jar)) { $jar = Join-Path $PSScriptRoot 'app.jar' }
if (-not (Test-Path -LiteralPath $jar)) { throw 'Primero prepara la entrega con mvnw.cmd verify o Preparar-Despliegue.ps1.' }
$java = if ($JavaHome) { Join-Path $JavaHome 'bin/java.exe' } else { (Get-Command java -ErrorAction Stop).Source }
Write-Output "Demostración sin Internet: http://127.0.0.1:$Puerto — perfiles ficticios y contacto desactivado. Ctrl+C para salir."
& $java -jar $jar '--spring.profiles.active=demo' '--server.address=127.0.0.1' "--server.port=$Puerto"
if ($LASTEXITCODE -ne 0) { throw 'La demostración local terminó con error.' }
