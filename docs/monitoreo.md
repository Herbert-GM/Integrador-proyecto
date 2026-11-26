# Monitoreo — semana 16

La semana 16 del sílabo corresponde al monitoreo del sistema y a la revisión del avance. Esta entrega añade observación operativa al proyecto publicado; no genera otro informe académico ni asigna por sí sola un porcentaje de cumplimiento.

## Qué se observa

- Render utiliza `/vivo`: comprueba que el servidor responde, sin despertar PostgreSQL con consultas periódicas.
- `/salud` devuelve HTTP 200 y `estado=disponible` al conectar con la base, o HTTP 503 y un estado genérico cuando falla. Consultarlo puntualmente después de desplegar o al investigar un problema.
- En producción, cada petición síncrona recibe `X-Request-ID`, generado por el servidor. Permite localizar el registro correspondiente en Render Logs.
- El registro `peticion` contiene identificador, método, ruta normalizada, estado HTTP y duración en milisegundos. No incluye correos, teléfonos, identificadores de perfiles, cuerpos, consultas, cookies, credenciales ni cabeceras del cliente.
- HTTP 5xx se registra como ERROR; HTTP 429 y respuestas desde 1000 ms como WARN; el resto como INFO. Los sondeos `/vivo` y archivos estáticos correctos se omiten salvo lentitud. Los fallos nunca se omiten.

No se añade un panel público de administración, un servicio de pago ni un sondeo externo permanente. El filtro mide el procesamiento del servidor; no mide red, carga de imágenes en el navegador ni tiempo de suspensión de Render. Está diseñado para los controladores síncronos actuales.

## Comprobación manual

Desde la raíz del repositorio, con PowerShell:

```powershell
.\scripts\Comprobar-Monitoreo.ps1
.\scripts\Comprobar-Monitoreo.ps1 -IncluirBaseDeDatos -Guardar
```

Cada ejecución realiza una consulta por ruta seleccionada. No se repite en un bucle. Comprueba también el JSON para no confundir la pantalla de arranque de Render (HTTP 200) con una respuesta de la aplicación. Devuelve código de salida 0 si todas pasan y 1 si alguna falla. `-Guardar` añade evidencia sin datos personales a `target/monitoreo/comprobaciones.jsonl`, fuera de Git.

La primera visita después de inactividad puede tardar más en Render Free. Una medición lenta durante el arranque no demuestra que las consultas normales sean lentas. Esperar a que termine el arranque y repetir manualmente una sola vez si es necesario. No configurar servicios que mantengan artificialmente activos Render o Neon.

## Diagnóstico y respuesta

| Señal | Revisión | Acción |
| --- | --- | --- |
| `/vivo` no responde o devuelve HTML | Render: servicio, eventos y último despliegue | Distinguir suspensión, arranque o caída; revisar logs antes de reiniciar |
| `/vivo` pasa y `/salud` falla | Render Logs y Neon: estado/cuotas/conexión | Revisar secretos en el panel privado y TLS; no imprimir valores ni desactivar certificados |
| HTTP 5xx | Buscar `peticion` por `X-Request-ID` y momento | Reproducir, corregir en local, ejecutar pruebas y publicar un commit |
| HTTP 429 | Ruta y frecuencia de intentos | Esperar la ventana del límite; no quitar la protección para resolver el aviso |
| WARN por duración | Comparar visitas normales posteriores al arranque | Investigar solo si se repite; separar procesamiento del servidor de latencia percibida |
| Perfil ausente | Perfil completo, publicación voluntaria y vigencia | Aplicar las reglas del producto; no modificar la base para saltarlas |

Si una actualización falla, Render conserva la versión anterior hasta que la nueva supere sus comprobaciones. Para un problema funcional en una versión ya activa, identificar el último commit válido y acordar su redepliegue. No restaurar ni borrar la base como primera medida.

## Validación y pendientes

En el recorrido de producción del 25 de noviembre se verificaron registro del autor, guardado del perfil, activación mensual simulada, repetición sin extensión de vigencia, publicación en catálogo, detalle y pantalla previa a WhatsApp. No se enviaron mensajes. No se ocultó el perfil del autor ni se alteró su fecha de vencimiento para probar casos destructivos; esas reglas permanecen cubiertas por pruebas aisladas.

Pendiente operativo: el rol inicial `ensename_app` de consola sigue sin utilizarse y requiere limpieza desde la administración de Neon. La aplicación usa `ensename_runtime`, sin membresía administrativa. El selector de país queda para una entrega posterior.

Referencias: [comprobaciones de salud de Render](https://render.com/docs/health-checks) y [OncePerRequestFilter de Spring](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/web/filter/OncePerRequestFilter.html).
