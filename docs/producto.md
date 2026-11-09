# Objetivo y funciones de ENSEÑA-ME

Base de trabajo acordada el 8 de noviembre de 2026. Este archivo acompaña al código; no vuelve a generar el informe académico.

## Objetivo

Facilitar que estudiantes universitarios encuentren a otros estudiantes que ofrecen orientación académica, revisen sus especialidades y establezcan contacto externo por WhatsApp.

El catálogo será público y gratuito. Una cuenta podrá buscar orientación y tener un perfil de orientador. La suscripción mensual simulada se conserva por decisión expresa del autor y controla la visibilidad. No se procesa dinero. S/ 5 es una hipótesis del informe, no un cobro implementado. El autor confirma que el docente aprobó la propuesta.

## Recorridos

1. Visitante: inicio → catálogo → filtro por especialidad → detalle visible → contacto externo.
2. Orientador: registro → acceso → completar perfil → activar simulación → habilitar publicación → aparecer en catálogo.
3. Orientador que deja de publicar: ocultar perfil o vencer suscripción → dejar de aparecer en catálogo y detalle.

## Mapa funcional

| ID | Función | Criterio de cierre |
| --- | --- | --- |
| RF01 | Registro, inicio y cierre de sesión | Correo único, contraseña protegida y cierre efectivo de sesión |
| RF02 | Gestionar perfil propio | Guardar descripción, especialidades y teléfono; rechazar edición ajena |
| RF03 | Catálogo y filtro | Solo perfiles visibles de la especialidad elegida; explicar resultado vacío |
| RF04 | Detalle público | La URL directa no evade restricciones de visibilidad |
| RF05 | WhatsApp | Enlace codificado al número autorizado; no afirmar envío de mensaje |
| RF06 | Activación simulada | Solo suscripción propia, sin cobro ni solicitud de tarjeta |
| RF07 | Publicación y vigencia | Perfil completo, cuenta habilitada, publicación voluntaria y suscripción vigente |
| RF08 | Estado de suscripción | Inicio, fin y estado visibles únicamente al propietario |

## Reglas

- Un mes calendario en America/Lima, inicio incluido y fin excluido. Si no existe el día en el mes siguiente, usar el último día de ese mes.
- Repetir activación vigente no extiende el periodo; reactivar después de vencer inicia otro mes.
- Perfil completo: descripción, al menos una especialidad y teléfono válido.
- Ocultar voluntariamente prevalece sobre la suscripción vigente.
- Listado y detalle evalúan la misma regla de visibilidad en el servidor.
- La información del perfil es declarada por su propietario; no certifica competencias.
- Los perfiles ficticios de demostración no permiten contactar teléfonos reales.

## Pantallas previstas

Inicio; catálogo; detalle; registro; acceso; mi perfil; mi suscripción; errores y estados vacíos. El catálogo de especialidades parte de valores controlados. No se añade panel de administración en esta etapa.

## Fuera del alcance

Pagos reales, chat, reservas, agenda, videollamadas, matchmaking, reseñas y reputación. Coordinación y posibles honorarios entre estudiantes son externos.

## Arquitectura

Un proyecto Java 21 con MVC, servicios y repositorios. El repositorio de demostración en memoria se reemplazará por JPA/SQL Server. El reloj inyectable permite probar límites temporales. No se ejecuta el SQL histórico automáticamente. No se necesitan microservicios ni aplicaciones separadas.

## Fuentes

Sílabo e informe final aportados por el autor en la carpeta raíz ENSEÑA-ME. El sílabo define etapas académicas; el informe define las funciones. Las decisiones posteriores del autor actualizan el estado histórico del informe.
