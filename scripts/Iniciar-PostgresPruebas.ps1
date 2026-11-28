param([string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin', [string]$DirectorioProyecto = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$proyecto = [IO.Path]::GetFullPath($DirectorioProyecto)
$datos = Join-Path $proyecto '.local\postgres-test'
$psql = Join-Path $PgBin 'psql.exe'
if (-not (Test-Path -LiteralPath $psql)) { throw 'Indica la carpeta bin de PostgreSQL con -PgBin.' }
if (-not (Test-Path -LiteralPath (Join-Path $datos 'PG_VERSION'))) {
    & (Join-Path $PgBin 'initdb.exe') -D $datos -U ensename_test -A trust -E UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar la instancia aislada.' }
}
& (Join-Path $PgBin 'pg_ctl.exe') -D $datos status | Out-Null
if ($LASTEXITCODE -ne 0) {
    & (Join-Path $PgBin 'pg_ctl.exe') -D $datos -l (Join-Path $proyecto '.local\postgres-test.log') -o '-h 127.0.0.1 -p 55432' -w start
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar PostgreSQL de pruebas en 127.0.0.1:55432.' }
}
$real = & $psql -h 127.0.0.1 -p 55432 -U ensename_test -d postgres -tA -c 'SHOW data_directory'
if ($LASTEXITCODE -ne 0 -or [IO.Path]::GetFullPath($real.Trim()) -ne [IO.Path]::GetFullPath($datos)) {
    throw 'El puerto no corresponde a la instancia aislada del proyecto. No se modificó su esquema.'
}
$existe = & $psql -h 127.0.0.1 -p 55432 -U ensename_test -d postgres -tA -c "SELECT 1 FROM pg_database WHERE datname='ensename'"
if ($LASTEXITCODE -ne 0) { throw 'No se pudo comprobar la base de pruebas.' }
if (-not $existe) {
    & (Join-Path $PgBin 'createdb.exe') -h 127.0.0.1 -p 55432 -U ensename_test ensename
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear la base de pruebas.' }
}
$tabla = & $psql -h 127.0.0.1 -p 55432 -U ensename_test -d ensename -tA -c "SELECT to_regclass('dbo.usuario')"
if ($LASTEXITCODE -ne 0) { throw 'No se pudo comprobar el esquema.' }
if (-not $tabla.Trim()) {
    & $psql -h 127.0.0.1 -p 55432 -U ensename_test -d ensename -v ON_ERROR_STOP=1 -f (Join-Path $proyecto 'deploy\postgres\001-esquema.sql')
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar el esquema de pruebas.' }
}
Write-Output 'PostgreSQL de pruebas disponible en 127.0.0.1:55432. Solo para desarrollo local, nunca para producción.'
