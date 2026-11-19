# Verificación de seguridad

Fecha: 14 de noviembre de 2026. Alcance: aplicación local con perfil `demo`, catálogo, filtros, detalle y recursos estáticos. Este registro es evidencia técnica de desarrollo, no un informe académico nuevo.

## Correcciones aplicadas

Se añade una política de contenido que permite estilos, imágenes y fuentes del propio servidor y bloquea scripts, objetos embebidos, inclusión en marcos, bases de URL y formularios hacia otros sitios. Se añade Referrer-Policy sin referencia y se deniega cámara, micrófono y geolocalización mediante Permissions-Policy. Se mantienen la protección CSRF, el bloqueo por defecto de rutas no autorizadas y las cabeceras de Spring Security.

Referencia técnica: [cabeceras de Spring Security](https://docs.spring.io/spring-security/reference/servlet/exploits/headers.html) y [protección CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

## Pruebas automatizadas

| Comprobación | Resultado esperado |
| --- | --- |
| Cabeceras CSP, anti-marcos, no-sniff, privacidad y caché | Presentes en catálogo |
| Escritura sin token CSRF o con token inválido | HTTP 403 |
| Escritura con token válido e identidad de prueba | HTTP 403: no hay rutas de escritura habilitadas |
| Acceso a configuraciones, documentos internos y administración | HTTP 403 |
| Filtros con script, cadena SQL o ruta relativa | HTTP 400: el filtro solo admite especialidades enumeradas |
| Identificador no numérico o inexistente | HTTP 400 o 404, respectivamente |
| Recursos gráficos y estilos | HTTP 200 |
| Origen externo | Sin habilitación de CORS |
| Perfil oculto, vencido o incompleto por URL directa | HTTP 404 |
| Números de ejemplo | Ausentes del HTML público |
| Límites de vigencia y fin de mes | Correctos según America/Lima |

La suite ejecuta 26 pruebas con JUnit y MockMvc, sin fallos, errores ni omisiones. La compilación y el empaquetado Maven se verifican con Java 21. Las pruebas de filtros no son una prueba de inyección SQL contra una base: la aplicación todavía usa un repositorio en memoria.

## Pendientes para cerrar la semana 14

No se declara alcanzado el 70% académico. Faltan persistencia, cuentas, edición propia y activación desde pantalla; por tanto, todavía no se pueden comprobar contraseñas, cierre de sesión, aislamiento entre dos propietarios o concurrencia de activaciones. Las identidades usadas con MockMvc son simuladas y no prueban un inicio de sesión real.

SQL Server Express está activo. La base `Ensename` está ONLINE, pero `HAS_DBACCESS` devuelve 0 para la cuenta Windows actual, que tampoco es administradora del servidor. No se cambiaron datos, permisos ni propietarios. Se necesita resolver el acceso autorizado antes de integrar la persistencia y las cuentas.

HTTPS, cookies seguras bajo HTTPS y pruebas en el alojamiento se comprobarán durante el despliegue de semana 15. Este registro no constituye una auditoría exhaustiva ni una certificación de seguridad.


## Actualización de cuentas y persistencia

Se resuelve el acceso mediante la cuenta Windows limitada autorizada por el autor. Se implementan registro transaccional, contraseña BCrypt (coste 12), correo único normalizado, acceso con errores genéricos, cierre de sesión con invalidación, protección de fijación de sesión, validación del perfil y selección de propietario a partir de la sesión. Los formularios no aceptan un identificador de propietario como autoridad. CSRF protege registro, acceso, edición, activación y salida.

Se añaden pruebas con H2 aislada para registro/duplicados, hash de contraseña, credenciales inválidas, cuenta deshabilitada, acceso anónimo, edición con identificador ajeno, activación ajena, validación y escape de texto con etiquetas script. La prueba SQL Server real comprueba conexión Windows, registro, actualización, activación idempotente, ocultamiento y vencimiento; todas sus escrituras se revierten. Las tablas y las restricciones SQL Server reales no se sustituyen por las pruebas H2.

El bloqueo de la sección anterior y las funciones de cuentas pendientes quedan resueltos en esta actualización. Aún no se ha realizado una auditoría externa, un análisis completo de dependencias ni una prueba de carga/concurrencia bajo múltiples procesos. El bloqueo transaccional de SQL Server serializa las activaciones de un mismo propietario; no se declara ejecutada una prueba de carga concurrente. Limitación de intentos, recuperación de contraseña y verificación de correo quedan pendientes antes de abrir un registro público en Internet. Los controles de HTTPS y del alojamiento se realizarán en semana 15.

Resultado final: 36 pruebas ejecutadas, sin fallos, errores ni omisiones; incluye nueve pruebas de cuentas y una prueba real de SQL Server con rollback. Maven verify generó el JAR. Esta entrega completa los controles locales acordados; la publicación en Internet requiere los pendientes de despliegue indicados arriba.


## Actualización 2026-11-18

Contacto con comprobación de visibilidad tanto en el aviso como en la salida a WhatsApp. Destino fijo, texto codificado y ninguna confirmación ficticia de mensaje enviado. Las pruebas no abren ni envían mensajes reales.

Límites locales: 10 solicitudes de ingreso cada 15 minutos y 5 registros por hora por dirección. Respuesta 429 con Retry-After; capacidad de contadores acotada. Se cuentan también solicitudes correctas. Los límites se reinician con el proceso y no se comparten entre instancias. El proxy de alojamiento debe sustituir las cabeceras del cliente; requiere verificación en el despliegue.

Producción rechaza modo demo, usuario sa, credenciales vacías, conexión sin cifrado o confianza en certificado sin validar. Redirige HTTP a HTTPS. /salud consulta la base y no expone errores internos. Las pruebas HTTPS usan un servidor de prueba; no prueban el certificado de un alojamiento que aún no existe.

Suite final: 45 pruebas aprobadas, sin omisiones. Persisten como pendientes la verificación del alojamiento, la recuperación de contraseña y la verificación de correo. Las comprobaciones de salud no deben sondear constantemente una base gratuita que deba pausarse.


## Actualización 2026-11-18 — PostgreSQL

Se añade PostgreSQL con TLS y validación del hostname para Neon. El perfil produccion exige exactamente un motor. La edición y activación usan bloqueo por propietario compatible con PostgreSQL y SQL Server. Se repiten las pruebas web sobre PostgreSQL real y se comprueba que dos activaciones simultáneas producen un solo periodo. El esquema PostgreSQL restringe teléfono, correo único y mes calendario en Lima. No se ha verificado todavía una conexión a Neon ni un dominio público.

Validación de la adaptación: 59 pruebas aprobadas sin errores ni omisiones con PostgreSQL real, SQL Server real y concurrencia. La imagen Docker se construyó en Linux y pasó sus pruebas sin bases externas (13 pruebas optativas omitidas allí, ejecutadas en la suite local completa). El contenedor final se conectó a un PostgreSQL 18 privado, mostró registro HTTP 200 y /salud disponible. Se comprobó usuario de proceso sin privilegios. Las bases de pruebas no contienen datos reales. Falta comprobar TLS y el recorrido completo en las cuentas reales de Render y Neon.
