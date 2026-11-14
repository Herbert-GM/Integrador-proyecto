# Entregas y continuidad

## Ruta de implementación

| Entrega | Trabajo | Estado |
| --- | --- | --- |
| E01 | Proyecto ejecutable, catálogo demo, reglas de suscripción y publicación, pruebas | Implementado; resultados de validación al cierre de la sesión |
| E02 | JPA/SQL Server, variables de entorno, esquema reproducible y datos de ejemplo aislados | Pendiente |
| E03 | Registro, autenticación, cierre de sesión y validaciones | Pendiente |
| E04 | Edición del perfil propio, especialidades, publicar y ocultar | Pendiente |
| E05 | Activación desde pantalla, estado de suscripción y control de concurrencia | Pendiente |
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
