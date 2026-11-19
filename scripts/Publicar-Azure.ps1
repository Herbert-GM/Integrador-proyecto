[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory)][string]$Suscripcion,
    [Parameter(Mandatory)][string]$Grupo,
    [Parameter(Mandatory)][string]$Aplicacion
)
$ErrorActionPreference = 'Stop'
$proyecto = Split-Path $PSScriptRoot -Parent
$jar = Join-Path $proyecto 'target\despliegue\app.jar'
if (-not (Get-Command az -ErrorAction SilentlyContinue)) { throw 'Instala Azure CLI e inicia sesión antes de publicar.' }
if (-not (Test-Path -LiteralPath $jar)) { throw 'Ejecuta primero scripts\Preparar-Despliegue.ps1.' }
$hostAplicacion = & az webapp show --subscription $Suscripcion --resource-group $Grupo --name $Aplicacion --query defaultHostName --output tsv
if ($LASTEXITCODE -ne 0 -or -not $hostAplicacion) { throw 'No se encontró una aplicación existente con esos datos.' }
if ($PSCmdlet.ShouldProcess($Aplicacion, 'Publicar el JAR comprobado y reiniciar App Service')) {
    & az webapp deploy --subscription $Suscripcion --resource-group $Grupo --name $Aplicacion --src-path $jar --type jar --restart true
    if ($LASTEXITCODE -ne 0) { throw 'Azure no confirmó la publicación.' }
    Write-Output "Publicado. Verifica https://$hostAplicacion/salud y el recorrido de la aplicación."
}
