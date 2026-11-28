# Funciones compartidas. Los secretos se pasan solo al entorno del proceso hijo.
function Invoke-PgMantenimiento {
    param([string]$Ejecutable, [string[]]$Argumentos, [hashtable]$Variables)
    $inicio = [Diagnostics.ProcessStartInfo]::new()
    $inicio.FileName = $Ejecutable
    $inicio.UseShellExecute = $false
    $inicio.CreateNoWindow = $true
    $inicio.RedirectStandardOutput = $true
    $inicio.RedirectStandardError = $true
    foreach ($argumento in $Argumentos) { $inicio.ArgumentList.Add($argumento) }
    # No heredar ajustes de otra base ni archivos de configuración de libpq.
    foreach ($nombre in @($inicio.Environment.Keys)) {
        if ($nombre -like 'PG*') { $inicio.Environment.Remove($nombre) | Out-Null }
    }
    foreach ($nombre in $Variables.Keys) { $inicio.Environment[$nombre] = [string]$Variables[$nombre] }
    $proceso = [Diagnostics.Process]::new()
    $proceso.StartInfo = $inicio
    try {
        $proceso.Start() | Out-Null
        $salida = $proceso.StandardOutput.ReadToEndAsync()
        $errores = $proceso.StandardError.ReadToEndAsync()
        if (-not $proceso.WaitForExit(120000)) {
            $proceso.Kill($true)
            throw 'La operación PostgreSQL superó dos minutos.'
        }
        $texto = $salida.GetAwaiter().GetResult()
        $errores.GetAwaiter().GetResult() | Out-Null
        if ($proceso.ExitCode -ne 0) { throw 'La operación PostgreSQL falló. Revisa conexión, permisos, versión y certificado; no se muestran detalles privados.' }
        return $texto.Trim()
    } finally { $inicio.Environment.Clear(); $proceso.Dispose() }
}

function Get-PgAislado {
    param([string]$PgBin, [string]$Proyecto)
    $variables = @{ PGHOST='127.0.0.1'; PGPORT='55432'; PGUSER='ensename_test'; PGDATABASE='postgres'; PGSSLMODE='disable'; PGCONNECT_TIMEOUT='10' }
    $real = Invoke-PgMantenimiento (Join-Path $PgBin 'psql.exe') @('-w','-X','-tA','-v','ON_ERROR_STOP=1','-c','SHOW data_directory') $variables
    $esperado = [IO.Path]::GetFullPath((Join-Path $Proyecto '.local/postgres-test'))
    if ([IO.Path]::GetFullPath($real) -ne $esperado) { throw 'El servidor no corresponde a la instancia aislada del proyecto.' }
    return $variables
}
