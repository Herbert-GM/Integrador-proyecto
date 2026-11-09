package pe.edu.utp.ensename;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pe.edu.utp.ensename.model.*;
import pe.edu.utp.ensename.repository.*;
import pe.edu.utp.ensename.service.CatalogoService;
import static org.assertj.core.api.Assertions.*;

class CatalogoServiceTests {
    private final Instant ahora = Instant.parse("2026-11-08T15:00:00Z");
    private final Clock clock = Clock.fixed(ahora, Suscripcion.LIMA);

    @Test void catalogoYDetalleCompartenReglaDeVisibilidad() {
        var service = new CatalogoService(new DemoOrientadorRepository(clock), clock);
        assertThat(service.buscar(null)).extracting(Orientador::id).containsExactly(1L, 2L);
        for (long id : new long[]{3, 4, 5, 999}) assertThat(service.detalleVisible(id)).isEmpty();
    }
    @Test void filtraPorEspecialidadYDevuelveVacioSinCoincidencias() {
        var service = new CatalogoService(new DemoOrientadorRepository(clock), clock);
        assertThat(service.buscar(Especialidad.PROGRAMACION)).extracting(Orientador::id).containsExactly(1L);
        assertThat(service.buscar(Especialidad.BASES_DE_DATOS)).isEmpty();
    }
    @Test void vencimientoOcultaTambienElDetalleSinReiniciarRepositorio() {
        var repo = new DemoOrientadorRepository(clock);
        var vencimiento = Suscripcion.activar(ahora, null).fin();
        var service = new CatalogoService(repo, Clock.fixed(vencimiento, Suscripcion.LIMA));
        assertThat(service.buscar(null)).isEmpty();
        assertThat(service.detalleVisible(1)).isEmpty();
    }
    @Test void noExponeCuentaDeshabilitadaTelefonoInvalidoOSinSuscripcion() {
        var s = Suscripcion.activar(ahora, null);
        var especialidades = Set.of(Especialidad.PROGRAMACION);
        var perfiles = List.of(
            new Orientador(1, "Ejemplo", "Descripción", especialidades, "51900000001", false, true, s),
            new Orientador(2, "Ejemplo", "Descripción", especialidades, "texto", true, true, s),
            new Orientador(3, "Ejemplo", "Descripción", especialidades, "51900000001", true, true, null));
        var repo = new OrientadorRepository() {
            public List<Orientador> buscarTodos() { return perfiles; }
            public Optional<Orientador> buscarPorId(long id) { return perfiles.stream().filter(p -> p.id() == id).findFirst(); }
        };
        var service = new CatalogoService(repo, clock);
        assertThat(service.buscar(null)).isEmpty();
        for (var p : perfiles) assertThat(service.detalleVisible(p.id())).isEmpty();
    }
}
