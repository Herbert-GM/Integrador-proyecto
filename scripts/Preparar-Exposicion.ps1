[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
Push-Location $proyecto
try {
    $estado = & git status --porcelain
    if ($LASTEXITCODE -ne 0 -or $estado) { throw 'Guarda y revisa los cambios en Git antes de empaquetar la exposición.' }
    $version = & git rev-parse HEAD
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo identificar el commit.' }
    $jar = Join-Path $proyecto 'target/ensename-0.1.0-SNAPSHOT.jar'
    if (-not (Test-Path -LiteralPath $jar)) { throw 'Ejecuta mvnw.cmd verify antes de preparar la exposición.' }
    $destino = Join-Path $proyecto ('target/exposicion-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ') + '-' + [guid]::NewGuid().ToString('N').Substring(0,8))
    New-Item -ItemType Directory -Path $destino | Out-Null
    Copy-Item -LiteralPath $jar -Destination (Join-Path $destino 'app.jar')
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Iniciar-DemoExposicion.ps1') -Destination $destino
    foreach($script in @('Iniciar-PostgresPruebas.ps1','Postgres-Mantenimiento.ps1')){Copy-Item -LiteralPath (Join-Path $PSScriptRoot $script) -Destination $destino}
    $esquema=Join-Path $destino 'deploy/postgres'
    New-Item -ItemType Directory -Path $esquema -Force | Out-Null
    Copy-Item -LiteralPath 'deploy/postgres/001-esquema.sql' -Destination $esquema
    Copy-Item -LiteralPath 'docs/exposicion.md' -Destination (Join-Path $destino 'LEEME.md')
    $archivos = Get-ChildItem -LiteralPath $destino -File -Recurse | ForEach-Object {
        [pscustomobject]@{ archivo=[IO.Path]::GetRelativePath($destino,$_.FullName); sha256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash }
    }
    [pscustomobject]@{ commit=$version.Trim(); creado_utc=[DateTimeOffset]::UtcNow.ToString('o'); archivos=@($archivos) } |
        ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $destino 'manifest.json') -Encoding utf8
    $zip = $destino + '.zip'
    Compress-Archive -Path (Join-Path $destino '*') -DestinationPath $zip
    Write-Output "Paquete de exposición: $zip"
    Write-Output ('SHA256 ZIP: ' + (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash)
    Write-Output 'Incluye demo y guía. No incluye respaldos, secretos ni cuentas reales. Requiere Java 21 instalado.'
} finally { Pop-Location }
