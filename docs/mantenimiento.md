# Mantenimiento y recuperación — semana 17

Esta guía acompaña al código y cubre respaldos, prueba de recuperación, diagnóstico y publicación de correcciones. No sustituye un respaldo por el historial de Git: Git conserva código, no las cuentas de Neon.

## Respaldo previo a la exposición

El 27 de noviembre de 2026 se creó y se verificó por listado un snapshot manual del proyecto de Neon existente:

- Nombre: `ensename-pre-final-20261127`.
- Identificador: `snap-winter-block-bd9uwmyf`.
- Rama de origen: `production` (`br-solitary-union-bdmvi6ha`).
- Vencimiento configurado: **6 de diciembre de 2026, 00:00, America/Lima**.

La creación no modificó los registros ni cambió la conexión de Render. El snapshot permanece en el mismo proyecto; no se exportaron datos reales al equipo. No se ha realizado una restauración de este snapshot, por lo que no se afirma que su recuperación completa ya esté probada. La prueba de recuperación descrita abajo se hizo con datos ficticios y pg_dump.

Antes de la exposición y de cambios de esquema, revisar disponibilidad y respaldos en Neon. Crear una copia reciente si hay nuevos registros; la copia del 27 no contiene cambios posteriores. No se configuró una tarea recurrente ni se cambió el plan del proveedor.

## Exportación cifrada opcional

`Respaldar-Postgres.ps1` exporta exclusivamente el esquema `dbo`, sus datos y sus secuencias en formato custom de pg_dump. No exporta roles ni permisos; estos se preparan separadamente mediante SQL revisado. Requiere PostgreSQL 18 y PowerShell 7 en Windows. Para una base externa solicita una contraseña mediante entrada segura, usa conexión directa sin pooler y exige TLS con validación de certificado y hostname.

```powershell
.\scripts\Respaldar-Postgres.ps1 -Servidor 'ep-TU-SERVIDOR.region.aws.neon.tech' -Base neondb -Usuario TU_USUARIO_DE_RESPALDO
```

Usar una cuenta autorizada que pueda leer las tablas y los estados de las secuencias. La cuenta limitada de la aplicación no debe ampliarse para hacer respaldos. Si el sistema no reconoce la CA con `-CertificadoRaiz system`, indicar el archivo de la CA confiable; no quitar `verify-full`. No copiar URLs con contraseñas al comando, Git o el chat.

El resultado queda en `.local/respaldos/*.dump.dpapi` más su SHA256. DPAPI `CurrentUser` cifra el archivo para **esta cuenta de Windows en este equipo**: no es una copia portátil ni sustituye el respaldo de Neon. No perder el perfil de Windows. Los respaldos incluyen hashes de contraseñas y datos personales, aunque no contraseñas de conexión. No subirlos a Git ni incluirlos en el ZIP de exposición. La huella detecta corrupción accidental; no acredita un archivo recibido de terceros. Solo restaurar copias propias y confiables.

## Prueba de recuperación sin tocar producción

```powershell
.\scripts\Iniciar-PostgresPruebas.ps1
.\scripts\Respaldar-Postgres.ps1 -LocalDePrueba -Base ensename
.\scripts\Probar-Restauracion.ps1 -Respaldo 'RUTA_COMPLETA_AL_ARCHIVO.dump.dpapi' -BaseNueva ensename_restauracion_ensayo
```

El modo local comprueba que el puerto 55432 de 127.0.0.1 corresponde al directorio aislado `.local/postgres-test`. La restauración no acepta un servidor externo y solo crea bases cuyo nombre comienza por `ensename_restauracion_`. Rechaza destinos existentes y huellas inválidas antes de crear una base. Nunca utiliza `--clean`, `DROP DATABASE` o sustitución de datos. Restaura en una sola transacción, sin propietarios/permisos del origen, y comprueba cinco tablas y conteos agregados. El destino se conserva para inspección; si falla, queda disponible para diagnóstico y no se borra automáticamente.

Validación realizada con una base ficticia: se recuperaron **1 usuario, 1 orientador, 3 especialidades, 2 relaciones y 1 suscripción**. Se comprobó la continuación de la secuencia de usuarios. También se verificaron rechazo de destino existente y rechazo de huella inválida. No se usaron los datos del autor.

## Recuperación real

1. Determinar si el problema está en el servicio, la configuración o los datos; usar `docs/monitoreo.md`.
2. Para un fallo de código, redeplegar el último commit válido; no restaurar la base por defecto.
3. Ante pérdida de datos, identificar respaldo, fecha y cambios que se perderían. Obtener autorización específica antes de restaurar o sustituir una rama/base publicada.
4. Preferir una rama o base nueva y verificar esquema, conteos, integridad y acceso antes de cambiar la conexión. No dar por recuperado el servicio solo porque se creó un destino.
5. Si se necesita cambiar credenciales/conexión en Render, hacerlo únicamente en su configuración privada; probar `/salud` y el recorrido público después.

Objetivos iniciales académicos: copia manual antes de cambios y un ensayo de recuperación antes de la exposición. No existe todavía una garantía medida de RPO/RTO, respaldo automático continuo ni prueba de recuperación completa del snapshot de Neon.

## Rutina

Antes de cambiar código: Git limpio, respaldo cuando afecte datos y criterio de reversión. Después: pruebas pertinentes, revisión de secretos, commit, push y comprobación de la versión. Durante la exposición: consultar `/vivo` y `/salud` puntualmente; no mantenerlos en un bucle. Revisar las cuotas y la retención desde el panel antes de asumir que una copia sigue disponible.

Pendiente independiente: el rol inicial de consola `ensename_app` sigue sin utilizarse; su desactivación fue rechazada por permisos. No se eliminó ni se reemplazó. La aplicación usa `ensename_runtime`.

Fuentes: [backups de Neon](https://neon.com/docs/postgres/backup-restore/backups), [pg_dump 18](https://www.postgresql.org/docs/18/app-pgdump.html), [pg_restore 18](https://www.postgresql.org/docs/18/app-pgrestore.html).
