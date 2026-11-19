# Preparación de despliegue de ENSEÑA-ME

La aplicación se entrega como JAR ejecutable Java 21 para Azure App Service (Java SE) y Azure SQL Database. La preparación está implementada; aún no existe una cuenta de nube ni una aplicación pública. No se declara cerrada la semana 15 hasta publicar y comprobar el enlace HTTPS.

## Activación de la cuenta

El autor debe activar [Azure for Students](https://azure.microsoft.com/en-us/free/students) con su verificación de estudiante y aceptar personalmente las condiciones. La oferta anuncia acceso sin tarjeta para estudiantes elegibles. Confirmar la suscripción exacta: Students Starter no es compatible con la oferta gratuita de Azure SQL.

Antes de crear recursos, revisar las cuotas vigentes y la estimación del portal. Para la base gratuita, seleccionar la pausa al agotar el límite mensual; no habilitar continuidad con cobro. Para App Service, comprobar disponibilidad de Java 21 SE, Linux y plan F1 en la región. Si el portal no ofrece esa combinación gratuita, detener la creación y revisar opciones con el autor. No se autoriza contratar un plan de pago automáticamente.

Fuentes: [oferta para estudiantes](https://azure.microsoft.com/en-us/free/students), [oferta gratuita SQL y límites](https://learn.microsoft.com/en-us/azure/azure-sql/database/free-offer?view=azuresql), [Java SE en App Service](https://learn.microsoft.com/en-us/azure/app-service/configure-language-java-deploy-run).

## Paquete local comprobable

Con JAVA_HOME apuntando al JDK 21:

```powershell
.\scripts\Preparar-Despliegue.ps1
```

El resultado queda en `target/despliegue`: app.jar, esquema inicial, especialidades, variables de ejemplo, estas instrucciones y huella SHA256. No contiene contraseñas, datos personales, archivos .env ni la biblioteca de autenticación Windows.

`mvnw verify` ejecuta las pruebas H2 y web. Para incluir la prueba real Windows, preparar el controlador local y definir RUN_SQLSERVER_TESTS=true con .local/native en PATH. La prueba real revierte sus escrituras; no envía mensajes de WhatsApp.

## Base de datos nueva

Crear una base vacía en Azure SQL. Conectarse a ella con una identidad de preparación y ejecutar `001-esquema.sql` y luego `002-especialidades.sql`. El primer archivo no crea bases ni cambia de base con USE. Si detecta Usuario existente, se detiene sin alterar los datos. No ejecutarlo sobre la base local como una actualización.

Crear una identidad de ejecución `ensename_app` con una contraseña propia guardada en Azure, separada del administrador. Conceder únicamente SELECT/INSERT/UPDATE en Usuario y Orientador; SELECT en Especialidad; SELECT/INSERT/DELETE en OrientadorEspecialidad; SELECT/INSERT en Suscripcion. No usar sa ni conceder db_owner. No importar usuarios, contraseñas o contactos locales a la nube como datos de ejemplo.

Las reglas de red de SQL deben permitir solo las direcciones de salida de la aplicación y la dirección temporal necesaria para preparar el esquema. Retirar esta última tras finalizar. No abrir acceso general a Internet ni habilitar indiscriminadamente todos los servicios Azure.

## Configurar App Service

Crear una aplicación Java 21 SE (no WAR/Tomcat) dentro del plan revisado. Activar HTTPS Only y usar el dominio HTTPS asignado por Azure. Configurar las variables de `deploy/azure/configuracion.env.example` en App Service, sustituyendo servidor, base, usuario y contraseña. Usar el gestor de secretos del alojamiento; no guardar los valores reales en Git ni en el historial de comandos.

SPRING_PROFILES_ACTIVE debe ser `sqlserver,produccion`. La aplicación rechaza modo demo, credenciales vacías, usuario sa, autenticación Windows o una conexión sin cifrado/verificación de certificado cuando arranca en producción. Las cookies de sesión son Secure, HttpOnly y SameSite=Lax. HTTP se redirige a HTTPS.

El modo framework de cabeceras reenviadas presupone el proxy controlado de App Service. El servidor de aplicación no debe exponerse directamente evitando ese proxy. Comprobar que las cabeceras de cliente se sustituyen en el borde antes de confiar en la dirección remota para limitar accesos.

## Publicar sobre la aplicación existente

Una vez que el autor active la cuenta, instale Azure CLI e inicie sesión, utilizar:

```powershell
.\scripts\Publicar-Azure.ps1 -Suscripcion ID -Grupo GRUPO -Aplicacion NOMBRE -WhatIf
.\scripts\Publicar-Azure.ps1 -Suscripcion ID -Grupo GRUPO -Aplicacion NOMBRE
```

El script publica únicamente sobre una aplicación existente. No crea recursos ni modifica credenciales. Azure recibe app.jar mediante `az webapp deploy --type jar`. Referencia: [publicación de JAR en App Service](https://learn.microsoft.com/en-us/azure/app-service/deploy-zip).

## Comprobación para cerrar semana 15

1. HTTPS responde; HTTP redirige sin bucles. `/salud` devuelve disponible sin mostrar credenciales.
2. Registrar una cuenta de prueba propia, ingresar, completar perfil, activar simulación y publicar.
3. Comprobar catálogo, detalle y aviso antes de WhatsApp. Verificar el destino sin enviar mensajes de prueba a terceros.
4. Ocultar el perfil y comprobar que detalle/contacto/salida a WhatsApp dejan de estar disponibles por URL directa.
5. Cerrar sesión y comprobar que las pantallas privadas exigen ingreso.
6. Reiniciar App Service y comprobar que el perfil y su suscripción se conservan.
7. Verificar cookies Secure/HttpOnly/SameSite y las cabeceras de protección en el dominio público.
8. Registrar URL, versión publicada, resultado y costes estimados en la continuidad del proyecto.

## Límites conocidos

El límite de acceso permite 10 solicitudes de ingreso cada 15 minutos y 5 registros por hora por dirección de cliente. Cuenta todas las solicitudes, incluidas las correctas. Se mantiene en memoria en una sola instancia; un reinicio restablece los contadores y varias instancias requieren un límite compartido o una política del proveedor. Usuarios que compartan una dirección de red comparten el límite. No es una protección distribuida contra denegación de servicio.

La base gratuita puede pausarse. Las conexiones inactivas se liberan en producción para facilitar la pausa, y la primera consulta puede tardar más al reactivarse. `/salud` consulta la base: no programar comprobaciones frecuentes que impidan su pausa y consuman la cuota. Las cuotas gratuitas pueden impedir disponibilidad continua; comprobarlas antes de la sustentación.

Verificación de correo y recuperación de contraseña aún no forman parte de esta entrega. La demostración académica debe comunicar estas limitaciones. El seguimiento de logs, copias y mantenimiento se abordará en las semanas siguientes.


## Actualización 2026-11-18 — PostgreSQL

La guía vigente tras la decisión de cambiar a PostgreSQL es [Render y Neon](render-neon.md). Este archivo conserva la alternativa anterior de Azure/SQL Server. No se contrató ni desplegó ningún recurso en Azure.
