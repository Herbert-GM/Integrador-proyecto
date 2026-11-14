param(
    [string]$Servidor = '.\SQLEXPRESS',
    [string]$Base = 'Ensename'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Data
$cadena = [System.Data.SqlClient.SqlConnectionStringBuilder]::new()
$cadena['Data Source'] = $Servidor
$cadena['Initial Catalog'] = $Base
$cadena['Integrated Security'] = $true
$cadena['Encrypt'] = $true
# Solo para comprobar SQL Express local con certificado autofirmado.
$cadena['TrustServerCertificate'] = $true
$cadena['Connect Timeout'] = 5
$conexion = [System.Data.SqlClient.SqlConnection]::new($cadena.ConnectionString)
try {
    $conexion.Open()
    $consulta = $conexion.CreateCommand()
    $consulta.CommandText = 'SELECT @@SERVERNAME servidor, DB_NAME() base, SUSER_SNAME() cuenta'
    $lector = $consulta.ExecuteReader()
    if ($lector.Read()) {
        [pscustomobject]@{ Servidor = $lector['servidor']; Base = $lector['base']; Cuenta = $lector['cuenta']; Acceso = 'Correcto' }
    }
    $lector.Dispose()
    $consulta.Dispose()
} catch {
    Write-Error "No se pudo abrir la base '$Base' en '$Servidor' mediante la cuenta de Windows actual. Comprueba el servidor y pide al administrador acceso limitado a esa base. Detalle: $($_.Exception.Message)"
    exit 1
} finally {
    $conexion.Dispose()
}
