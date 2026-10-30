# Enseña-me

Proyecto universitario individual de Herbert Alain Gadea Mendoza (U24239559).

## Entregables

- [Unidad 1 Planificación](docs/u1-planificacion/): Canvas, requerimientos y documentos de gestión PMBOK.
- [Unidad 2 Diseño](docs/u2-diseno/): BPMN, base de datos y prototipo de interfaz.
- [Unidad 3 Arquitectura](docs/u3-arquitectura/): diseño MVC propuesto.

## Estructura propuesta

Los archivos de ejemplo están vacíos y representan la organización prevista del proyecto.

```text
proyecto/
├── README.md
├── docs/
│   ├── u1-planificacion/
│   ├── u2-diseno/
│   └── u3-arquitectura/
└── src/
    ├── main/
    │   ├── java/pe/edu/utp/ensename/
    │   │   ├── EnsenameApplication.java
    │   │   ├── model/
    │   │   │   ├── Usuario.java
    │   │   │   ├── Orientador.java
    │   │   │   ├── Especialidad.java
    │   │   │   └── Suscripcion.java
    │   │   ├── controller/CatalogoController.java
    │   │   ├── service/CatalogoService.java
    │   │   ├── repository/OrientadorRepository.java
    │   │   └── config/SeguridadConfig.java
    │   └── resources/
    │       ├── application.properties
    │       ├── templates/
    │       │   ├── index.html
    │       │   ├── catalogo.html
    │       │   └── perfil.html
    │       └── static/
    │           ├── css/estilos.css
    │           ├── js/app.js
    │           └── img/.gitkeep
    └── test/java/pe/edu/utp/ensename/
        └── EnsenameApplicationTests.java
```