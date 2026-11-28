# ENSEÑA-ME

Plataforma de orientación académica entre estudiantes. Una misma cuenta puede buscar ayuda y ofrecer orientación. El catálogo es público; un perfil aparece si está completo, su propietario lo publica y tiene una suscripción mensual simulada vigente.

## Estado al 27 de noviembre de 2026

Implementados: registro, inicio y cierre de sesión, edición exclusiva del perfil propio, especialidades, activación mensual sin cobro, estado de suscripción, catálogo, filtro y detalle protegido por visibilidad. Persistencia en PostgreSQL para el despliegue y compatibilidad con SQL Server local mediante Spring JDBC y consultas parametrizadas. Las contraseñas usan BCrypt. Se conservan las protecciones CSRF y las cabeceras de seguridad.

También implementados: aviso y salida a WhatsApp con comprobación de visibilidad, límites de ingreso/registro, endpoint /salud y validación de configuración segura en producción. Las pruebas verifican el destino de WhatsApp sin enviar mensajes.

Publicado en [ensename.onrender.com](https://ensename.onrender.com), con Render Free y Neon. Se verificaron registro, perfil, activación simulada y publicación del autor. La semana 16 incorpora [monitoreo y diagnóstico](docs/monitoreo.md): tiempos y errores sin datos personales, identificador de petición y comprobación manual de disponibilidad. No se atribuye un porcentaje académico por contar archivos o pruebas.

Semana 17: [mantenimiento y recuperación](docs/mantenimiento.md), snapshot de Neon previo a la exposición, exportación local cifrada y restauración probada con datos ficticios. La [guía del 4 de diciembre](docs/exposicion.md) incluye recorrido y alternativa local sin Internet. Para crear el ZIP después de verificar y guardar el código en Git: `scripts/Preparar-Exposicion.ps1`.

## Iniciar con Windows y SQL Server

Requisitos: JDK 21 x64, SQL Server Express, base Ensename con el esquema de `docs/u2-diseno/base-de-datos/sql/Ensename_BD_Completa.sql`, especialidades y cuenta Windows autorizada con permisos limitados.

```powershell
.\scripts\Preparar-Local.ps1
.\scripts\Comprobar-SqlServer.ps1
.\scripts\Iniciar-Local.ps1
```

Abrir http://localhost:8080/registro para crear una cuenta. La contraseña debe tener de 12 a 64 caracteres y como máximo 72 bytes UTF-8. Después de ingresar, completar el perfil, activar la suscripción simulada y habilitar la publicación. Para otro puerto: `.\scripts\Iniciar-Local.ps1 -Puerto 8095`.

La aplicación se conecta mediante la cuenta Windows del proceso. No necesita ni almacena la contraseña administrativa de SQL Server. El controlador nativo oficial se descarga a `.local/native`, excluido de Git. Los scripts no cambian políticas globales de Windows ni el PATH permanente.

## Demostración sin base de datos

```powershell
.\mvnw.cmd spring-boot:run
```

El perfil predeterminado `demo` mantiene cinco perfiles ficticios en memoria; registro y gestión privada requieren `sqlserver`. No combinar ambos perfiles. La demo permite mostrar perfiles visibles, vencidos, incompletos y ocultos sin usar teléfonos reales.

## Pruebas

```powershell
.\mvnw.cmd verify
```

La suite habitual usa H2 aislada para cuentas y MockMvc para los recorridos web. La prueba SQL Server real es optativa y revierte todas sus escrituras al finalizar. Tras preparar el controlador:

```powershell
$env:PATH=(Join-Path (Get-Location) '.local\native')+';'+$env:PATH
$env:RUN_SQLSERVER_TESTS='true'
.\mvnw.cmd verify
```

## Despliegue en Render y Neon

Tras el rechazo de la oferta de Azure, el autor autorizó PostgreSQL. La guía vigente es [Render y Neon](docs/render-neon.md). `render.yaml` configura el servicio gratuito y `Dockerfile` compila/prueba en Linux con Java 21. El dominio público ya está activo.

Configurar `postgres,produccion` con DB_URL, DB_USERNAME y DB_PASSWORD en Render. La URL PostgreSQL exige `sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory`. No copiar las credenciales a Git. El esquema nuevo está en `deploy/postgres/001-esquema.sql`; no se migraron cuentas ni datos personales de SQL Server.

## Alternativa anterior de SQL Server

Usar los perfiles `sqlserver,produccion` y proporcionar DB_URL, DB_USERNAME y DB_PASSWORD mediante el alojamiento. DB_URL debe requerir cifrado y validar el certificado (encrypt=true;trustServerCertificate=false). La autenticación final depende del proveedor; el código de cuentas y las pantallas no cambian. Producción exige HTTPS para las cookies seguras. Esta alternativa no es el despliegue público vigente.

- [Objetivo y funciones](docs/producto.md)
- [Entregas y continuidad](docs/implementacion.md)
- [Conexión y permisos](docs/conexion.md)
- [Evidencia de seguridad](docs/seguridad.md)

Repositorio: https://github.com/Herbert-GM/Integrador-proyecto. Carpeta activa: C:\Users\Usuario\Documents\ChatGPT\Integrador\ensename. Los antecedentes APF1–APF3 se conservan completos.


## Preparar la publicación

```powershell
.\scripts\Preparar-Despliegue.ps1
```

Genera `target/despliegue-postgres/app.jar`, esquema inicial para una base nueva, permisos limitados y variables de ejemplo. El paquete queda también en `target/ensename-despliegue-postgres.zip`. Para la alternativa SQL Server usar `-Motor sqlserver`. Consultar [despliegue en Render y Neon](docs/render-neon.md). La publicación requiere una cuenta activada por el autor; los scripts no crean ni contratan recursos automáticamente.


## Verificar PostgreSQL real local

```powershell
.\scripts\Iniciar-PostgresPruebas.ps1
$env:RUN_POSTGRES_TESTS='true'
.\mvnw.cmd verify
```

Utiliza PostgreSQL 18 instalado con una instancia aislada en .local/postgres-test, accesible únicamente en 127.0.0.1:55432. Repite los recorridos web y comprueba activaciones simultáneas. No usar esta configuración de pruebas para un alojamiento.
