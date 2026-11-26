# Publicar ENSEÑA-ME en Render y Neon

El autor autorizó PostgreSQL tras el rechazo de la oferta gratuita de Azure. El código admite PostgreSQL para Render/Neon, SQL Server para el desarrollo previo y el modo demo. No se copiaron datos personales ni cuentas locales a la nube.

El 22 de noviembre de 2026 se publicó [ENSEÑA-ME](https://ensename.onrender.com) mediante el Blueprint del repositorio, en Render Free y Neon Free (Ohio, PostgreSQL 18). La base inicial es `neondb`, con el esquema `dbo`; el usuario de ejecución es `ensename_runtime`, creado por SQL sin pertenencia a `neon_superuser`. Sus credenciales se configuran únicamente en Render. El 25 de noviembre se verificó el recorrido con la cuenta creada por el autor: perfil guardado, suscripción simulada vigente sin extensión al repetir, publicación autorizada en catálogo, detalle y pantalla previa a WhatsApp. No se enviaron mensajes ni se copiaron datos personales a Git. La operación se describe en [monitoreo](monitoreo.md).

El rol `ensename_app` creado inicialmente desde la consola no se utiliza en Render: Neon le asignó permisos adicionales y el propietario SQL rechazó tanto retirar la membresía como deshabilitar el inicio de sesión. Su limpieza sigue pendiente mediante la administración de Neon. No usar ese rol para la aplicación.

## Preparar Neon

1. Crear una cuenta en [Neon](https://console.neon.tech/) y seleccionar el plan Free. Elegir una región cercana a la que se utilizará en Render.
2. Crear un proyecto y una base llamada `ensename`. Conectarse a esa base en el editor SQL de Neon con su propietario.
3. Ejecutar `deploy/postgres/001-esquema.sql`. El script crea las cinco tablas, restricciones, índices y tres especialidades en una base nueva. No borra tablas; si el esquema ya existe, la transacción falla y se revierte.
4. Crear el rol de aplicación `ensename_app` con LOGIN y una contraseña segura mediante SQL, conservando el secreto únicamente en Neon y Render. No otorgarle el rol neon_superuser ni hacerlo propietario de las tablas. Una cuenta creada desde la consola puede tener privilegios adicionales; comprobarlos antes de usarla.
5. Ejecutar `deploy/postgres/002-permisos.sql` como propietario. Concede uso del esquema y las secuencias y únicamente los permisos de lectura/escritura que necesita ENSEÑA-ME.
6. En Connect, obtener el servidor con pooling y el nombre de base. Usar esos datos para construir la URL JDBC del ejemplo. No copiar el prefijo `postgresql://usuario:clave@...` directamente a DB_URL: el usuario y la contraseña se configuran por separado.

El esquema conserva el nombre `dbo` para compartir consultas entre PostgreSQL y SQL Server; es un esquema normal de PostgreSQL, no una dependencia de Microsoft. Las cuentas nuevas usan correo normalizado y un índice único sin distinguir mayúsculas. Las fechas usan timestamptz y la restricción mensual se evalúa en America/Lima, incluyendo finales de mes.

## Configurar Render

1. Crear una cuenta en [Render](https://dashboard.render.com/) y conectar el repositorio `Herbert-GM/Integrador-proyecto`.
2. Elegir New → Blueprint y seleccionar el repositorio con `render.yaml`, o crear un Web Service con runtime Docker y Dockerfile en la raíz. Confirmar el plan Free antes de crear el servicio.
3. Configurar las variables requeridas por el Blueprint: DB_URL, DB_USERNAME y DB_PASSWORD. No introducirlas en Git, capturas o mensajes del chat.
4. DB_USERNAME debe coincidir con el rol limitado creado por SQL (`ensename_runtime` en el despliegue actual). DB_PASSWORD es la contraseña propia de ese rol. DB_URL sigue este formato, sustituyendo servidor y base:

```text
jdbc:postgresql://ep-del-proyecto-pooler.region.aws.neon.tech/ensename?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory
```

5. El Blueprint establece SPRING_PROFILES_ACTIVE=postgres,produccion y SERVER_ADDRESS=0.0.0.0. La aplicación utiliza el PORT que asigne Render. El contenedor ejecuta Java 21 con un usuario sin privilegios y limita la proporción de memoria utilizada por el heap.
6. Render construye la imagen, ejecuta las pruebas independientes del motor y publica la URL HTTPS. No es necesario subir el JAR manualmente ni instalar el controlador de Windows.

La aplicación exige conexión TLS con certificado y hostname verificados en producción. La fábrica SSL utiliza el almacén de confianza de Java. No desactivar la validación si aparece un error: comprobar el hostname de Neon y el certificado de la imagen Java.

## Comprobar el despliegue

- `/vivo` verifica que el proceso responde sin consultar PostgreSQL; se utiliza para las comprobaciones automáticas de Render.
- `/salud` comprueba la conexión con PostgreSQL. Consultarlo manualmente tras publicar, evitando sondeos constantes que mantengan Neon activo.
- Registrar una cuenta de prueba propia, ingresar, completar el perfil, activar la suscripción simulada y habilitar publicación.
- Comprobar catálogo, filtro, detalle y aviso antes de WhatsApp. No enviar mensajes a teléfonos ficticios.
- Ocultar el perfil y comprobar que sus rutas de detalle y contacto devuelven 404.
- Cerrar sesión y verificar que las pantallas privadas requieren ingreso.
- Reiniciar o volver a desplegar y verificar que los datos se mantienen en Neon.
- Verificar HTTPS y cookies Secure, HttpOnly y SameSite. Comprobar que el proxy de Render trata correctamente las cabeceras reenviadas antes de confiar en la IP de cliente para los límites de acceso.

## Pruebas locales reproducibles

Se utiliza una instancia PostgreSQL 18 aislada dentro de `.local/postgres-test`, en 127.0.0.1:55432. El script no modifica el servicio PostgreSQL ya instalado ni SQL Server. Esta instancia de pruebas usa trust solo para acceso local y nunca debe exponerse ni desplegarse.

```powershell
.\scripts\Iniciar-PostgresPruebas.ps1
$env:RUN_POSTGRES_TESTS='true'
.\mvnw.cmd verify
```

Las pruebas web se ejecutan realmente sobre PostgreSQL y revierten los datos al terminar. La prueba de concurrencia crea una cuenta aleatoria temporal y la limpia en esa misma base aislada. Para detenerla:

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe' -D '.local\postgres-test' -m fast -w stop
```

Para ejecutar la aplicación con la base local de pruebas: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres`. No usar el perfil produccion sin TLS ni secretos configurados.

## Límites de la alternativa gratuita

Render Free puede suspender la aplicación tras inactividad y tardar en atender la siguiente visita. Neon Free tiene cuotas de almacenamiento y cómputo; comprobar las vigentes en su panel. Si un proveedor solicita un cambio a pago, revisar la decisión con el autor antes de aceptarlo. El límite de intentos de ENSEÑA-ME sigue siendo local por instancia y se restablece al reiniciar.

La oferta gratuita de Render Postgres tiene caducidad; por eso el diseño utiliza una base independiente en Neon. El dominio público responde mediante HTTPS y el recorrido principal ya se verificó con el autor. Los casos de ocultamiento y vencimiento se prueban sobre datos aislados, sin modificar la publicación ni las fechas de su cuenta real.

Referencias oficiales: [Docker en Render](https://render.com/docs/docker), [Blueprints](https://render.com/docs/blueprint-spec), [comprobaciones de salud](https://render.com/docs/health-checks), [límites gratuitos de Render](https://render.com/docs/free), [Neon Free](https://neon.com/blog/neon-free-plan-1-gb-per-project), [TLS en pgJDBC](https://jdbc.postgresql.org/documentation/ssl/).
