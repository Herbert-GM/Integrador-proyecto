package pe.edu.utp.ensename.repository;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import pe.edu.utp.ensename.model.*;

/** Única fuente de ejemplos ficticios para demo de lectura y ensayo con cuentas. */
public final class PerfilesDemostracion {
    private PerfilesDemostracion() {}
    public static List<Orientador> crear(Clock clock) {
        var ahora=clock.instant();
        var vigente=Suscripcion.activar(ahora,null);
        var vencida=Suscripcion.activar(ahora.atZone(Suscripcion.LIMA).minusMonths(2).toInstant(),null);
        return List.of(
            new Orientador(1,"Ana · perfil de ejemplo","Te acompaño a entender los fundamentos de Java y resolver ejercicios paso a paso.",Set.of(Especialidad.PROGRAMACION),"51900000001",true,true,vigente),
            new Orientador(2,"Luis · perfil de ejemplo","Practicamos álgebra y razonamiento matemático con ejercicios explicados.",Set.of(Especialidad.MATEMATICAS),"51900000002",true,true,vigente),
            new Orientador(3,"Paula · perfil de ejemplo","Comprendemos claves, relaciones y consultas SQL con ejercicios guiados.",Set.of(Especialidad.BASES_DE_DATOS),"51900000003",true,true,vencida),
            new Orientador(4,"Perfil oculto","El propietario eligió ocultar su perfil.",Set.of(Especialidad.PROGRAMACION),"51900000004",true,false,vigente),
            new Orientador(5,"Perfil incompleto","",Set.of(),null,true,true,vigente),
            new Orientador(6,"Valeria · perfil de ejemplo","Aprende consultas SQL, relaciones y normalización con ejemplos de una biblioteca.",Set.of(Especialidad.BASES_DE_DATOS),"51900000006",true,true,vigente),
            new Orientador(7,"Diego · perfil de ejemplo","Organizamos clases, objetos y colecciones para comprender programación orientada a objetos.",Set.of(Especialidad.PROGRAMACION),"51900000007",true,true,vigente),
            new Orientador(8,"Camila · perfil de ejemplo","Resolvemos funciones, límites y derivadas explicando el razonamiento de cada paso.",Set.of(Especialidad.MATEMATICAS),"51900000008",true,true,vigente),
            new Orientador(9,"Mateo · perfil de ejemplo","Conectamos aplicaciones Java con PostgreSQL y practicamos consultas parametrizadas.",Set.of(Especialidad.PROGRAMACION,Especialidad.BASES_DE_DATOS),"51900000009",true,true,vigente),
            new Orientador(10,"Sofía · perfil de ejemplo","Relacionamos lógica, conjuntos y algoritmos mediante ejercicios pequeños.",Set.of(Especialidad.PROGRAMACION,Especialidad.MATEMATICAS),"51900000010",true,true,vigente),
            new Orientador(11,"Andrés · perfil de ejemplo","Diseñamos modelos de datos y practicamos consultas con agrupaciones y estadísticas sencillas.",Set.of(Especialidad.BASES_DE_DATOS,Especialidad.MATEMATICAS),"51900000011",true,true,vigente));
    }
}
