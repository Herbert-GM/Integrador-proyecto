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
