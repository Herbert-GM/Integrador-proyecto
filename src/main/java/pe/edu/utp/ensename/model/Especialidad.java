package pe.edu.utp.ensename.model;

public enum Especialidad {
    PROGRAMACION("Programación"), MATEMATICAS("Matemáticas"), BASES_DE_DATOS("Bases de datos");

    private final String nombre;
    Especialidad(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
}
