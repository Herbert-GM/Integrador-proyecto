# ENSEÑA-ME

Plataforma de orientación académica entre estudiantes: descubrir orientadores por especialidad, consultar perfiles y, en la versión completa, contactarlos por WhatsApp. Una misma cuenta podrá buscar ayuda y ofrecer orientación.

Proyecto académico de Herbert Alain Gadea Mendoza. El autor confirma el visto bueno del docente. Los antecedentes académicos se conservan sin reescribirlos.

## Estado real

Primera entrega implementada: inicio, catálogo público, filtro, detalle y reglas de suscripción y publicación con pruebas automatizadas. Los perfiles se guardan en memoria y se regeneran al arrancar.

Pendientes: registro, acceso, edición del perfil, activación desde pantalla, persistencia SQL Server y contacto real. No se atribuye un porcentaje académico al avance.

## Ejecutar

Requisito: JDK 21 y JAVA_HOME apuntando al JDK. Maven Wrapper descarga Maven y las dependencias oficiales en el primer uso.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080. Solo escucha en la computadora local. Para otro puerto, definir `$env:PORT='8081'` antes de ejecutar. Detener con Ctrl+C.

En macOS/Linux: `sh mvnw test` y `sh mvnw spring-boot:run`.

```powershell
.\mvnw.cmd clean verify
java -jar target/ensename-0.1.0-SNAPSHOT.jar
```

El perfil predeterminado es `demo`. Aún no existe modo de producción: cambiar a otro perfil sin implementar otro repositorio impide el arranque.

## Demostración

| ID | Caso | Resultado |
| --- | --- | --- |
| 1 | Ana, programación, suscripción vigente | Visible |
| 2 | Luis, matemáticas, suscripción vigente | Visible |
| 3 | Suscripción vencida | Oculto |
| 4 | Oculto voluntariamente con suscripción vigente | Oculto |
| 5 | Perfil incompleto | Oculto |

Los casos ocultos tampoco se exponen por URL directa. Son perfiles ficticios, todavía no cuentas autenticables. Los números de prueba no se muestran ni generan enlaces externos. Los datos se restablecen al reiniciar; las suscripciones vigentes de ejemplo empiezan en ese arranque.

## Organización

- `src/main/java/pe/edu/utp/ensename`: controladores, servicios, modelos, repositorio y configuración.
- `src/main/resources`: vistas Thymeleaf y estilos locales sin CDN.
- `src/test/java`: pruebas de negocio e integración web.
- [Objetivo y funciones](docs/producto.md): decisiones vigentes y criterios de aceptación.
- [Entregas y continuidad](docs/implementacion.md): pendientes y cierre de sesiones con Git.
- `docs/u1-planificacion`, `docs/u2-diseno`, `docs/u3-arquitectura`: antecedentes conservados.

Un único proyecto Maven con capas MVC es suficiente para el alcance individual y permite cambiar el repositorio demo por SQL Server.

Tecnologías actuales: Java 21, Spring Boot 3.5.16, Spring MVC, Thymeleaf, Spring Security, JUnit y Maven. Logback llega con Spring Boot. JPA, SQL Server y Bootstrap 5 quedan pendientes; no se declaran integrados.

## Git

Repositorio: https://github.com/Herbert-GM/Integrador-proyecto. La carpeta activa es `C:\Users\Usuario\Documents\ChatGPT\Integrador\ensename`, clonada del historial publicado de APF3. La carpeta anterior de entregables se conserva como antecedente. No subir contraseñas, configuraciones locales, dependencias descargadas ni `target`.
