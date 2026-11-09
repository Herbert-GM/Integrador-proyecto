package pe.edu.utp.ensename.repository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pe.edu.utp.ensename.model.*;

/** Datos ficticios en memoria. No representan cuentas ni contactos reales. */
@Repository
@Profile("demo")
public class DemoOrientadorRepository implements OrientadorRepository {
    private final List<Orientador> perfiles;

    public DemoOrientadorRepository(Clock clock) {
        var ahora = clock.instant();
        var vigente = Suscripcion.activar(ahora, null);
        var vencida = Suscripcion.activar(ahora.atZone(Suscripcion.LIMA).minusMonths(2).toInstant(), null);
        perfiles = List.of(
            new Orientador(1, "Ana · perfil de ejemplo", "Te acompaño a entender los fundamentos de Java y resolver ejercicios paso a paso.", Set.of(Especialidad.PROGRAMACION), "51900000001", true, true, vigente),
            new Orientador(2, "Luis · perfil de ejemplo", "Practicamos álgebra y razonamiento matemático con ejercicios explicados.", Set.of(Especialidad.MATEMATICAS), "51900000002", true, true, vigente),
            new Orientador(3, "Perfil vencido", "Ejemplo de suscripción vencida.", Set.of(Especialidad.BASES_DE_DATOS), "51900000003", true, true, vencida),
            new Orientador(4, "Perfil oculto", "El propietario eligió ocultar su perfil.", Set.of(Especialidad.PROGRAMACION), "51900000004", true, false, vigente),
            new Orientador(5, "Perfil incompleto", "", Set.of(), null, true, true, vigente));
    }

    public List<Orientador> buscarTodos() { return perfiles; }
    public Optional<Orientador> buscarPorId(long id) {
        return perfiles.stream().filter(p -> p.id() == id).findFirst();
    }
}
