param([string]$JavaHome = $env:JAVA_HOME, [ValidateSet('postgres','sqlserver')][string]$Motor = 'postgres')
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
if ($JavaHome) { $env:JAVA_HOME = $JavaHome }
Push-Location $proyecto
try {
    & '.\mvnw.cmd' -B -ntp verify
    if ($LASTEXITCODE -ne 0) { throw 'La verificación falló. No se preparó un paquete nuevo.' }
    $destino = Join-Path $proyecto $(if ($Motor -eq 'postgres') { 'target\despliegue-postgres' } else { 'target\despliegue' })
    New-Item -ItemType Directory -Force -Path $destino | Out-Null
    Copy-Item -LiteralPath 'target\ensename-0.1.0-SNAPSHOT.jar' -Destination (Join-Path $destino 'app.jar') -Force
    if ($Motor -eq 'postgres') {
        Copy-Item -LiteralPath 'deploy\postgres\001-esquema.sql' -Destination $destino -Force
        Copy-Item -LiteralPath 'deploy\postgres\002-permisos.sql' -Destination $destino -Force
        Copy-Item -LiteralPath 'deploy\postgres\configuracion.env.example' -Destination $destino -Force
        Copy-Item -LiteralPath 'docs\render-neon.md' -Destination $destino -Force
    } else {
        Copy-Item -LiteralPath 'deploy\azure\001-esquema.sql' -Destination $destino -Force
        Copy-Item -LiteralPath 'scripts\especialidades.sql' -Destination (Join-Path $destino '002-especialidades.sql') -Force
        Copy-Item -LiteralPath 'deploy\azure\configuracion.env.example' -Destination $destino -Force
        Copy-Item -LiteralPath 'docs\despliegue.md' -Destination $destino -Force
    }
    $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $destino 'app.jar')).Hash
    "$hash  app.jar" | Set-Content -LiteralPath (Join-Path $destino 'SHA256SUMS.txt') -Encoding ascii
    Compress-Archive -Path (Join-Path $destino '*') -DestinationPath (Join-Path $proyecto "target\ensename-despliegue-$Motor.zip") -Force
    Write-Output "Paquete comprobado: $destino"
} finally { Pop-Location }
