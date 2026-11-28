# Ensayo para el viernes 4 de diciembre de 2026

Guía operativa del proyecto, sin generar otro informe académico.

## Antes de presentar

- Confirmar [ENSEÑA-ME](https://ensename.onrender.com) y abrir la página con anticipación: Render Free puede tardar al despertar.
- Ejecutar `scripts/Comprobar-Monitoreo.ps1 -IncluirBaseDeDatos -Guardar` una vez y revisar que ambas rutas estén disponibles.
- Confirmar perfil público, especialidades y suscripción; la cuenta del autor conserva su periodo vigente. No alterar fechas ni cuentas para escenificar errores.
- Revisar en Neon que el respaldo previo sigue disponible y su fecha corresponde al estado que se desea conservar.
- Preparar el JAR y probar la alternativa local antes de salir. Tener Java 21 y el ZIP descomprimido.
- Abrir únicamente pestañas necesarias. Mantener cerrados contraseñas, variables privadas de Render y gestores de credenciales al compartir pantalla.

## Recorrido provisional de 10 minutos

El autor estima diez minutos; confirmar el límite con el docente. Si las preguntas son aparte, usar el último minuto para resumir resultados.

| Tiempo | Qué mostrar |
| --- | --- |
| 0:00–1:00 | Problema, objetivo y quién utiliza ENSEÑA-ME |
| 1:00–4:00 | Catálogo, filtro, detalle, aviso de contacto, perfil y suscripción |
| 4:00–6:00 | Arquitectura y decisiones de seguridad |
| 6:00–8:00 | Pruebas, monitoreo, respaldo y recuperación aislada |
| 8:00–9:00 | Resultado, alcance y límites |
| 9:00–10:00 | Preguntas o margen por una carga lenta |

Llegar con la sesión propia iniciada para el recorrido; no gastar tiempo creando otra cuenta ni escribiendo contraseñas ante el público. Mostrar una especialidad en el filtro y un único perfil es suficiente.

1. **Problema y objetivo:** estudiantes encuentran orientación de otros estudiantes y coordinan externamente. No se certifican competencias.
2. **Visitante:** inicio, catálogo, filtro por especialidad, detalle y aviso previo a WhatsApp. Detenerse antes de enviar cualquier mensaje.
3. **Cuenta:** entrar manualmente, mostrar perfil propio y estado de suscripción. Explicar que S/5 es una referencia y la activación es simulada, sin cobro ni tarjeta.
4. **Reglas:** solo aparecen perfiles completos, publicados por su dueño y con suscripción vigente. Mostrar la regla y las pruebas, sin ocultar o vencer el perfil real durante la exposición.
5. **Implementación:** Java 21, Spring MVC, servicios, repositorios JDBC, consultas parametrizadas y PostgreSQL. Render aloja la aplicación; Neon conserva los datos; GitHub guarda versiones de código.
6. **Seguridad:** BCrypt, sesión, CSRF, acceso al perfil propio, TLS y usuario de base limitado. No mostrar secretos como evidencia.
7. **Pruebas y operación:** resultados Maven, `/vivo` frente a `/salud`, `X-Request-ID`, registro de tiempos/errores, respaldo y ensayo de recuperación aislado.
8. **Límites:** sin pagos reales, chat, reservas, reseñas ni verificación de competencias. Selector de país y mejoras visuales son posteriores al cierre funcional.

## Si falla Internet o el alojamiento

Desde el repositorio:

```powershell
.\scripts\Iniciar-DemoExposicion.ps1 -JavaHome 'C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot'
```

Desde el ZIP descomprimido, ejecutar el mismo script que está junto a `app.jar`. Abrir `http://127.0.0.1:8090`; Ctrl+C detiene la demo. No necesita Maven, Internet, SQL Server ni Neon durante su ejecución. Sí necesita Java 21 instalado.

Esta alternativa muestra cinco perfiles ficticios y permite demostrar catálogo, filtros y visibilidad. **No contiene cuentas reales, registro/inicio de sesión ni contacto activo.** No presentarla como una copia completa de la base publicada. El recorrido de cuentas se explica con las pruebas aisladas y la validación previa.

Para crear el ZIP desde un árbol Git limpio después de ejecutar Maven verify:

```powershell
.\scripts\Preparar-Exposicion.ps1
```

El paquete queda en `target/exposicion-*.zip` y contiene el JAR, el lanzador, esta guía y un manifiesto de commit/SHA256. No lleva `.env`, respaldos, contraseñas ni datos de cuentas reales. El manifiesto identifica los archivos; no reemplaza las pruebas ni una firma digital.

## Comprobación final el día anterior

Ensayar con el equipo que se llevará, revisar batería/proyector/red y ejecutar también la demo local. Confirmar acceso propio sin guardar la contraseña en diapositivas o comandos. Si el docente solicita diapositivas o un formato de entrega específico, confirmar ese requisito antes de prepararlo; esta guía no presupone uno.
