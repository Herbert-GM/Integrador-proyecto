# ENSEÑA-ME

Plataforma de orientación académica entre estudiantes. Una misma cuenta puede buscar ayuda y ofrecer orientación. El catálogo es público; un perfil aparece si está completo, su propietario lo publica y tiene una suscripción mensual simulada vigente.

## Estado al 18 de noviembre de 2026

Implementados: registro, inicio y cierre de sesión, edición exclusiva del perfil propio, especialidades, activación mensual sin cobro, estado de suscripción, catálogo, filtro y detalle protegido por visibilidad. Persistencia en el esquema SQL Server existente mediante Spring JDBC y consultas parametrizadas. Las contraseñas usan BCrypt. Se conservan las protecciones CSRF y las cabeceras de seguridad.

También implementados: aviso y salida a WhatsApp con comprobación de visibilidad, límites de ingreso/registro, endpoint /salud y validación de configuración segura en producción. Las pruebas verifican el destino de WhatsApp sin enviar mensajes.

Pendiente: activar la cuenta de alojamiento, desplegar y comprobar el dominio HTTPS. La semana 15 todavía no se declara cerrada. No se atribuye un porcentaje académico por contar archivos o pruebas.

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

## Despliegue y continuidad

Usar los perfiles `sqlserver,produccion` y proporcionar DB_URL, DB_USERNAME y DB_PASSWORD mediante el alojamiento. DB_URL debe requerir cifrado y validar el certificado (encrypt=true;trustServerCertificate=false). La autenticación final depende del proveedor; el código de cuentas y las pantallas no cambian. Producción exige HTTPS para las cookies seguras. No se ha desplegado todavía.

- [Objetivo y funciones](docs/producto.md)
- [Entregas y continuidad](docs/implementacion.md)
- [Conexión y permisos](docs/conexion.md)
- [Evidencia de seguridad](docs/seguridad.md)

Repositorio: https://github.com/Herbert-GM/Integrador-proyecto. Carpeta activa: C:\Users\Usuario\Documents\ChatGPT\Integrador\ensename. Los antecedentes APF1–APF3 se conservan completos.


## Preparar la publicación

```powershell
.\scripts\Preparar-Despliegue.ps1
```

Genera `target/despliegue/app.jar`, esquema inicial para una base nueva y variables de ejemplo. El paquete queda también en `target/ensename-despliegue.zip`. Consultar [despliegue en Azure](docs/despliegue.md). La publicación requiere una cuenta activada por el autor; los scripts no crean ni contratan recursos automáticamente.
