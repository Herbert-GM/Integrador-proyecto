[CmdletBinding()]
param(
    [uri]$BaseUrl = 'https://ensename.onrender.com',
    [switch]$IncluirBaseDeDatos,
    [switch]$Guardar,
    [ValidateRange(1, 180)][int]$TimeoutSegundos = 90
)
$ErrorActionPreference = 'Stop'
if (-not $BaseUrl.IsAbsoluteUri -or $BaseUrl.UserInfo -or $BaseUrl.Query -or $BaseUrl.Fragment -or
    $BaseUrl.AbsolutePath -ne '/' -or
    ($BaseUrl.Scheme -ne 'https' -and -not ($BaseUrl.Scheme -eq 'http' -and $BaseUrl.IsLoopback))) {
    throw 'Usa una URL base HTTPS sin credenciales, ruta, consulta ni fragmento. HTTP solo se admite en localhost.'
}
$rutas = @('/vivo')
if ($IncluirBaseDeDatos) { $rutas += '/salud' }
$resultados = foreach ($ruta in $rutas) {
    $cronometro = [Diagnostics.Stopwatch]::StartNew()
    $estado = 0
    $disponible = $false
    $requestId = $null
    try {
        $respuesta = Invoke-WebRequest -Uri ([uri]::new($BaseUrl, $ruta)) -Method Get -UseBasicParsing -TimeoutSec $TimeoutSegundos -MaximumRedirection 0
        $estado = [int]$respuesta.StatusCode
        $contenido = $respuesta.Content | ConvertFrom-Json
        $disponible = $estado -eq 200 -and $contenido.estado -eq 'disponible'
        $idRecibido = [string]($respuesta.Headers['X-Request-ID'] | Select-Object -First 1)
        if ($idRecibido -match '^[0-9a-f-]{36}$') { $requestId = $idRecibido }
    } catch {
        if ($null -ne $_.Exception.Response) { $estado = [int]$_.Exception.Response.StatusCode }
        # No volcar la respuesta, cabeceras ni el mensaje de excepción.
    } finally { $cronometro.Stop() }
    [pscustomobject]@{
        fecha_utc = [DateTimeOffset]::UtcNow.ToString('o')
        ruta = $ruta
        http = $estado
        disponible = $disponible
        duracion_ms = $cronometro.ElapsedMilliseconds
        request_id = $requestId
    }
}
$resultados | Format-Table -AutoSize | Out-Host
if ($Guardar) {
    $destino = Join-Path $PSScriptRoot '../target/monitoreo'
    New-Item -ItemType Directory -Path $destino -Force | Out-Null
    foreach ($resultado in $resultados) {
        $resultado | ConvertTo-Json -Compress | Add-Content -LiteralPath (Join-Path $destino 'comprobaciones.jsonl') -Encoding utf8
    }
}
if (@($resultados | Where-Object { -not $_.disponible }).Count -gt 0) { exit 1 }
exit 0
