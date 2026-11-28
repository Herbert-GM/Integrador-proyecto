#Requires -Version 7.0
[CmdletBinding()]
param(
    [string]$Servidor,
    [string]$Base = 'neondb',
    [string]$Usuario,
    [Security.SecureString]$Clave,
    [string]$CertificadoRaiz = 'system',
    [switch]$LocalDePrueba,
    [string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin'
)
$ErrorActionPreference = 'Stop'
if (-not $IsWindows) { throw 'Este respaldo utiliza cifrado DPAPI del usuario de Windows.' }
$proyecto = Split-Path $PSScriptRoot -Parent
. (Join-Path $PSScriptRoot 'Postgres-Mantenimiento.ps1')
$variables = $null
if ($LocalDePrueba) {
    $variables = Get-PgAislado $PgBin $proyecto
    if ($Base -eq 'neondb') { $Base = 'ensename' }
} else {
    if ($Servidor -notmatch '^[a-zA-Z0-9.-]+$' -or $Servidor -match '-pooler\.' -or -not $Usuario) {
        throw 'Indica -Servidor directo (sin -pooler) y -Usuario autorizado para respaldo.'
    }
    if (-not $Clave) { $Clave = Read-Host 'Contraseña del usuario de respaldo (no se guardará)' -AsSecureString }
    $variables = @{ PGHOST=$Servidor; PGPORT='5432'; PGUSER=$Usuario; PGSSLMODE='verify-full'; PGSSLROOTCERT=$CertificadoRaiz; PGCONNECT_TIMEOUT='20' }
}
if ($Base -notmatch '^[a-zA-Z][a-zA-Z0-9_]{0,62}$') { throw 'Nombre de base no válido.' }
$variables.PGDATABASE = $Base
$destino = Join-Path $proyecto '.local/respaldos'
New-Item -ItemType Directory -Force -Path $destino | Out-Null
$nombre = 'ensename-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssZ') + '-' + [guid]::NewGuid().ToString('N')
$temporal = Join-Path $destino ($nombre + '.dump')
$cifrado = $temporal + '.dpapi'
$puntero = [IntPtr]::Zero
$datos = $null
try {
    if ($Clave) {
        $puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Clave)
        $variables.PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero)
    }
    Invoke-PgMantenimiento (Join-Path $PgBin 'pg_dump.exe') @('-w','--format=custom','--schema=dbo','--no-owner','--no-privileges','--file',$temporal) $variables | Out-Null
    Invoke-PgMantenimiento (Join-Path $PgBin 'pg_restore.exe') @('--list',$temporal) @{} | Out-Null
    $datos = [IO.File]::ReadAllBytes($temporal)
    $protegidos = [Security.Cryptography.ProtectedData]::Protect($datos, $null, [Security.Cryptography.DataProtectionScope]::CurrentUser)
    [IO.File]::WriteAllBytes($cifrado, $protegidos)
    $hash = (Get-FileHash -LiteralPath $cifrado -Algorithm SHA256).Hash
    $hash | Set-Content -LiteralPath ($cifrado + '.sha256') -Encoding ascii
    Write-Output "Respaldo cifrado: $cifrado"
    Write-Output 'Solo puede descifrarse con esta cuenta de Windows. El archivo no contiene roles ni contraseñas de conexión; sí contiene los datos y hashes de las cuentas.'
} finally {
    if ($null -ne $datos) { [Array]::Clear($datos, 0, $datos.Length) }
    if ($puntero -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero) }
    if ($null -ne $variables) { $variables.Clear() }
    if (Test-Path -LiteralPath $temporal) { Remove-Item -LiteralPath $temporal -Force }
}
