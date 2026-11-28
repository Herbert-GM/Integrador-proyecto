[CmdletBinding()]
param([ValidateRange(1024,65535)][int]$Puerto=8090, [string]$JavaHome=$env:JAVA_HOME,
    [switch]$ConCuenta, [string]$PgBin='C:\Program Files\PostgreSQL\18\bin')
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
if (Test-Path -LiteralPath (Join-Path $PSScriptRoot 'app.jar')) { $proyecto=$PSScriptRoot }
$jar = Join-Path $proyecto 'target/ensename-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jar)) { $jar = Join-Path $PSScriptRoot 'app.jar' }
if (-not (Test-Path -LiteralPath $jar)) { throw 'Primero prepara la entrega con mvnw.cmd verify o Preparar-Despliegue.ps1.' }
$java = if ($JavaHome) { Join-Path $JavaHome 'bin/java.exe' } else { (Get-Command java -ErrorAction Stop).Source }
$opciones=@('--spring.profiles.active=demo')
if ($ConCuenta) {
    & (Join-Path $PSScriptRoot 'Iniciar-PostgresPruebas.ps1') -PgBin $PgBin -DirectorioProyecto $proyecto
    . (Join-Path $PSScriptRoot 'Postgres-Mantenimiento.ps1')
    $variables=Get-PgAislado $PgBin $proyecto
    $existe=Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-tA','-v','ON_ERROR_STOP=1','-c',"SELECT count(*) FROM pg_database WHERE datname='ensename_exposicion'") $variables
    if($existe -eq '0'){Invoke-PgMantenimiento (Join-Path $PgBin 'createdb.exe') @('-w','ensename_exposicion') $variables | Out-Null}
    $variables.PGDATABASE='ensename_exposicion'
    $tabla=Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-tA','-v','ON_ERROR_STOP=1','-c',"SELECT to_regclass('dbo.usuario')") $variables
    if(-not $tabla){Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-v','ON_ERROR_STOP=1','-f',(Join-Path $proyecto 'deploy/postgres/001-esquema.sql')) $variables | Out-Null}
    $opciones=@('--spring.profiles.active=postgres,exposicion',"--ensename.exposicion.directorio=$(Join-Path $proyecto '.local/postgres-test')")
    Write-Output 'Cuenta de ensayo exclusivamente local: orientador@demo.invalid / Demostracion 2026!'
}
Write-Output "Demostración local: http://127.0.0.1:$Puerto — perfiles ficticios y WhatsApp desactivado. Ctrl+C para salir."
& $java -jar $jar @opciones '--server.address=127.0.0.1' "--server.port=$Puerto"
if ($LASTEXITCODE -ne 0) { throw 'La demostración local terminó con error.' }
