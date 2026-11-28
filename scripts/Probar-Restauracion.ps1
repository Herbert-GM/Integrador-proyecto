#Requires -Version 7.0
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$Respaldo,
    [Parameter(Mandatory)][ValidatePattern('^ensename_restauracion_[a-z0-9_]{1,35}$')][string]$BaseNueva,
    [string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin'
)
$ErrorActionPreference = 'Stop'
if (-not $IsWindows) { throw 'Este respaldo requiere la misma cuenta de Windows que lo cifró.' }
$proyecto = Split-Path $PSScriptRoot -Parent
. (Join-Path $PSScriptRoot 'Postgres-Mantenimiento.ps1')
$variables = Get-PgAislado $PgBin $proyecto
$archivo = (Resolve-Path -LiteralPath $Respaldo).Path
if (-not $archivo.EndsWith('.dump.dpapi') -or -not (Test-Path -LiteralPath ($archivo + '.sha256'))) { throw 'Se requiere respaldo .dump.dpapi y su archivo SHA256.' }
$esperado = (Get-Content -LiteralPath ($archivo + '.sha256') -Raw).Trim()
if ($esperado -notmatch '^[A-Fa-f0-9]{64}$' -or (Get-FileHash -LiteralPath $archivo -Algorithm SHA256).Hash -ne $esperado) { throw 'La huella del respaldo no coincide. No se creó una base.' }
$existe = Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-tA','-v','ON_ERROR_STOP=1','-c',"SELECT count(*) FROM pg_database WHERE datname='$BaseNueva'") $variables
if ($existe -ne '0') { throw 'La base de destino ya existe. No se sobrescribe ni se borra ninguna base.' }
$temporal = Join-Path (Split-Path $archivo -Parent) ('restauracion-' + [guid]::NewGuid().ToString('N') + '.dump')
$datos = $null
try {
    $datos = [Security.Cryptography.ProtectedData]::Unprotect([IO.File]::ReadAllBytes($archivo), $null, [Security.Cryptography.DataProtectionScope]::CurrentUser)
    [IO.File]::WriteAllBytes($temporal, $datos)
    Invoke-PgMantenimiento (Join-Path $PgBin 'pg_restore.exe') @('--list',$temporal) @{} | Out-Null
    Invoke-PgMantenimiento (Join-Path $PgBin 'createdb.exe') @('-w',$BaseNueva) $variables | Out-Null
    $variables.PGDATABASE = $BaseNueva
    Invoke-PgMantenimiento (Join-Path $PgBin 'pg_restore.exe') @('-w','--single-transaction','--exit-on-error','--no-owner','--no-privileges','--dbname',$BaseNueva,$temporal) $variables | Out-Null
    $tablas = Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-tA','-v','ON_ERROR_STOP=1','-c',"SELECT count(*) FROM information_schema.tables WHERE table_schema='dbo' AND table_type='BASE TABLE'") $variables
    if ($tablas -ne '5') { throw 'La restauración no contiene las cinco tablas esperadas. Conservar destino para diagnóstico.' }
    $conteos = Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-tA','-v','ON_ERROR_STOP=1','-c','SELECT (SELECT count(*) FROM dbo.usuario), (SELECT count(*) FROM dbo.orientador), (SELECT count(*) FROM dbo.especialidad), (SELECT count(*) FROM dbo.orientadorespecialidad), (SELECT count(*) FROM dbo.suscripcion)') $variables
    Write-Output "Restauración aislada correcta: $BaseNueva; cinco tablas. Conteos (usuario|orientador|especialidad|relación|suscripción): $conteos"
    Write-Output 'No se modificó la base publicada ni se reemplazó una base local existente.'
} finally {
    if ($null -ne $datos) { [Array]::Clear($datos, 0, $datos.Length) }
    $variables.Clear()
    if (Test-Path -LiteralPath $temporal) { Remove-Item -LiteralPath $temporal -Force }
}
