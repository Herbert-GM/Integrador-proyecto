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
        perfiles = PerfilesDemostracion.crear(clock);
    }

    public List<Orientador> buscarTodos() { return perfiles; }
    public Optional<Orientador> buscarPorId(long id) {
        return perfiles.stream().filter(p -> p.id() == id).findFirst();
    }
}
