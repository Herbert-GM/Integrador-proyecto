# Entregas y continuidad

## Ruta de implementación

| Entrega | Trabajo | Estado |
| --- | --- | --- |
| E01 | Proyecto ejecutable, catálogo demo, reglas de suscripción y publicación, pruebas | Implementado; resultados de validación al cierre de la sesión |
| E02 | SQL Server mediante JDBC, variables de entorno y reutilización del esquema existente | Implementado |
| E03 | Registro, autenticación, cierre de sesión y validaciones | Implementado |
| E04 | Edición del perfil propio, especialidades, publicar y ocultar | Implementado |
| E05 | Activación desde pantalla y estado de suscripción; bloqueo SQL por propietario | Implementado; carga concurrente pendiente |
| E06 | Contacto WhatsApp autorizado, recorrido integrado y adaptación visual | Pendiente |

Cada entrega incluye sus pruebas. La activación reutilizará las reglas probadas y deberá impedir periodos duplicados ante solicitudes concurrentes. Se integrará SQL Server antes de crear cuentas reales.

## Relación con el sílabo

- Semanas 9–12: arquitectura, bibliotecas Java, versiones y APF3.
- Semana 13: avance al 60% con pruebas de software.
- Semana 14: seguridad y correcciones, avance al 70%.
- Semana 15: despliegue, avance al 80%.
- Semana 16: monitoreo, avance al 90%.
- Semana 17: mantenimiento y avance al 100%.
- Semana 18: proyecto final.

El sílabo no asigna pesos por función. No se declara 60% por contar carpetas, pantallas o pruebas. Revisar con el autor las funciones terminadas y la evidencia requerida por el docente.

## Cierre de cada sesión

1. Revisar Git y preservar trabajo anterior.
2. Definir una entrega y sus criterios de aceptación.
3. Implementar y ejecutar pruebas pertinentes.
4. Registrar resultado, limitaciones y siguiente paso.
5. Revisar el diff y excluir secretos y archivos generados.
6. Crear un commit descriptivo con la identidad configurada.
7. Subir al remoto acordado sin sobrescribir historial.

Ejemplos: `feat: agregar catálogo público de orientadores`, `feat: permitir edición del perfil propio`, `fix: ocultar perfiles al vencer suscripción`.

Git guarda versiones locales; el push publica en GitHub. Distinguir entre implementado, probado, guardado en commit y subido.

## Sesión 2026-11-08

- Confirmado: no existía código previo; las carpetas eran la estructura presentada en APF3.
- Confirmado: conservar suscripción simulada para perfiles de ejemplo.
- Se conserva el repositorio y todos los antecedentes académicos.
- E01 contiene cinco perfiles ficticios: dos visibles, uno vencido, uno oculto y uno incompleto. Todavía no son cuentas con acceso.
- Se añade Maven Wrapper para evitar una instalación global obligatoria de Maven.
- El autor proporcionó el repositorio `https://github.com/Herbert-GM/Integrador-proyecto` y la identidad de commits, configurada solo en la copia activa.
- Próximo paso: comprobar la instancia de SQL Server y definir base de desarrollo antes de crear o modificar bases.
- Validación: Maven `verify` con Java 21.0.6 terminó correctamente; 18 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas. Se generó el JAR ejecutable.
- Revisión en navegador: inicio, catálogo con dos perfiles, filtro de programación con un perfil y detalle de Ana. También se comprobó visualmente el inicio en una ventana estrecha; aún no se declara una auditoría completa de accesibilidad o tamaños.
- Se creó una copia sincronizada en `C:\Users\Usuario\Documents\ChatGPT\Integrador\ensename` para preservar el índice de la carpeta anterior. Esta es la carpeta activa para próximas sesiones. El historial remoto de APF3 y sus documentos se conservan completos.
- La entrega se prepara como un commit sobre `main`; la confirmación de publicación se verifica con GitHub al terminar, fuera de este registro.

## Mejora visual basada en Figma

Se adapta la dirección visual del prototipo APF2 al inicio, catálogo y detalle: azul principal, acentos menta, cabecera con logotipo original, ilustración y tarjetas. Los perfiles siguen siendo los datos ficticios de la implementación. Registro y acceso siguen pendientes. Validación: 18 pruebas aprobadas y revisión visual del catálogo en navegador.

## Sesión 2026-11-14 — seguridad

Se añaden cabeceras de protección y ocho pruebas de seguridad web. Maven verify: 26 pruebas aprobadas, sin errores ni omisiones; JAR generado. Evidencia y límites en `docs/seguridad.md`. La semana 14 sigue pendiente de cuentas y pruebas de propiedad. SQL Server Express funciona, pero la identidad de Windows no tiene acceso a Ensename; se solicitó al autor el método de conexión autorizado. No se modificó la base de datos.


## Actualización 2026-11-14 — cuentas y SQL Server

Se implementan E02 a E05: acceso Windows limitado a Ensename, persistencia Spring JDBC sobre las tablas existentes, registro, acceso y salida, perfil propio, especialidades y suscripción mensual desde pantalla. Se eligió JDBC para reutilizar el esquema SQL recibido sin generar tablas ni conceder permisos DDL a la aplicación; el plan inicial mencionaba JPA y queda actualizado con esta decisión. El controlador de autenticación nativa queda fuera de Git.

El autor autorizó los permisos limitados de la cuenta Windows. No se guarda ninguna contraseña administrativa. Se mantienen modo demo y perfil sqlserver; produccion externaliza conexión, activa cookies seguras y se usará en semana 15. No se ha realizado despliegue.

El impedimento de acceso descrito al inicio de esta sesión queda resuelto. Próximo trabajo: contacto WhatsApp autorizado y preparación del despliegue, incluida la limitación de intentos de acceso y la revisión de secretos/configuración del alojamiento. No se declara un porcentaje académico sin evaluación del docente.

Validación final de esta entrega: Maven verify con Java 21, 36 pruebas ejecutadas y aprobadas sin omisiones, incluida SQL Server real con rollback; JAR generado. Registro e ingreso revisados visualmente en navegador.


## Sesión 2026-11-18 — preparación de semana 15

Se completa el contacto externo con aviso y nueva comprobación de visibilidad al abrir WhatsApp. Se limita el destino a wa.me y se codifica el mensaje. La demo no habilita contactos. Se añaden límites por dirección para ingreso y registro, HTTPS obligatorio en producción, comprobación de conexión cifrada y cuenta limitada al arrancar, y /salud sin detalles internos.

Se prepara el JAR y un paquete con esquema Azure SQL para base nueva, especialidades, ejemplo de variables, instrucciones y huella SHA256. Scripts de preparación y publicación sobre App Service existente. El esquema histórico y la base local se conservan. No se migraron datos a la nube ni se contrató alojamiento.

Validación final: 45 pruebas aprobadas, sin errores ni omisiones; incluye SQL Server real con rollback, contacto seguro, límites y HTTPS. Se generó el paquete Maven. Los scripts de PowerShell pasan el análisis de sintaxis. La publicación remota del script todavía no se ejecutó porque no existe una cuenta Azure activada.

El autor indicó que no dispone de alojamiento. Se propone Azure for Students, sujeto a elegibilidad, disponibilidad y revisión de costes. La activación personal de la cuenta y la comprobación del dominio público quedan pendientes; semana 15 sigue abierta hasta publicar y comprobar el recorrido.


## Actualización 2026-11-18 — PostgreSQL

El autor autorizó cambiar a PostgreSQL tras el rechazo de Azure gratuito. Se adapta el repositorio, perfiles de cuenta y validación de producción; se conserva SQL Server local. Se preparan esquema/permisos de PostgreSQL, Dockerfile y Blueprint Render Free, sin contraseñas ni datos personales. La imagen usa un usuario sin privilegios. Se añade /vivo sin consulta a base para no mantener Neon activo mediante sondeos. La aplicación pública sigue pendiente de cuentas Neon/Render y verificación del dominio HTTPS.

Validación de la adaptación: 59 pruebas aprobadas sin errores ni omisiones con PostgreSQL real, SQL Server real y concurrencia. La imagen Docker se construyó en Linux y pasó sus pruebas sin bases externas (13 pruebas optativas omitidas allí, ejecutadas en la suite local completa). El contenedor final se conectó a un PostgreSQL 18 privado, mostró registro HTTP 200 y /salud disponible. Se comprobó usuario de proceso sin privilegios. Las bases de pruebas no contienen datos reales. Falta comprobar TLS y el recorrido completo en las cuentas reales de Render y Neon.
