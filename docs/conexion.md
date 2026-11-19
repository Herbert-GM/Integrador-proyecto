# Conexión de desarrollo y despliegue

## Desarrollo local

Usamos autenticación de Windows para conectar Java con SQL Server Express. La cuenta del proceso es HERBERTGM\Usuario; el servidor local se identifica como DESKTOP-FV1Q5DC\SQLEXPRESS. La conexión a Ensename ya está comprobada desde PowerShell y Java.

```powershell
.\scripts\Preparar-Local.ps1
.\scripts\Comprobar-SqlServer.ps1
.\scripts\Iniciar-Local.ps1
```

JDK 21 x64 es obligatorio. El primer script descarga desde Maven Central el controlador nativo oficial de Microsoft a `.local/native` (excluido de Git). El segundo solo consulta servidor, base y cuenta; el tercero arranca con el perfil sqlserver. Ninguno configura contraseñas administrativas, cambia políticas globales de Windows ni crea tablas automáticamente.

La conexión local cifra el tráfico y confía en el certificado autofirmado de SQL Express. La configuración de producción deberá verificar el certificado del alojamiento.

## Permisos autorizados

El autor autorizó asociar la cuenta Windows a Ensename con estos permisos:

| Tabla | Permisos |
| --- | --- |
| Usuario | SELECT, INSERT, UPDATE |
| Orientador | SELECT, INSERT, UPDATE |
| Especialidad | SELECT |
| OrientadorEspecialidad | SELECT, INSERT, DELETE |
| Suscripcion | SELECT, INSERT |

La aplicación no tiene sysadmin ni db_owner ni permisos para crear tablas. El acceso administrativo se utilizó únicamente para asignar los permisos autorizados y preparar las tres especialidades iniciales. Ninguna contraseña administrativa se guarda en el proyecto. `scripts/especialidades.sql` es idempotente y conserva datos existentes.

## Esquema y persistencia

Se reutilizan las cinco tablas del diseño anterior. Spring JDBC ejecuta consultas parametrizadas y transacciones; no se ejecuta el SQL histórico automáticamente. La implementación usa JDBC para evitar cambios automáticos en el esquema recibido. El plan inicial de JPA queda actualizado con esta decisión.

La identidad de la base de datos permite que ENSEÑA-ME lea y guarde datos. Los estudiantes ingresan con correo y contraseña de la aplicación, protegida mediante hash BCrypt; no usan cuentas de Windows ni de SQL Server.

## Despliegue posterior

Activar `sqlserver,produccion` y proporcionar DB_URL, DB_USERNAME y DB_PASSWORD desde el alojamiento. DB_URL debe exigir cifrado y verificación del certificado: `encrypt=true;trustServerCertificate=false`. La identidad concreta dependerá del proveedor: cuenta de servicio Windows o autenticación que ofrezca la base alojada. La lógica y las pantallas de cuentas se conservan.

El perfil produccion exige cookies seguras, por lo que requiere HTTPS y una configuración correcta del proxy del proveedor. La cuenta de migración debe ser distinta de la identidad de ejecución. El despliegue aún no se ha realizado.

Referencia: [conexión y autenticación integrada con Microsoft JDBC](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver17).


## Actualización 2026-11-18 — PostgreSQL

El autor autorizó PostgreSQL para Render/Neon. La conexión Windows descrita aquí sigue disponible para SQL Server local. La guía vigente de nube es [Render y Neon](render-neon.md).
