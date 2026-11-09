package pe.edu.utp.ensename.service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.edu.utp.ensename.model.*;
import pe.edu.utp.ensename.repository.OrientadorRepository;

@Service
public class CatalogoService {
    private final OrientadorRepository repository;
    private final Clock clock;

    public CatalogoService(OrientadorRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Orientador> buscar(Especialidad especialidad) {
        var ahora = clock.instant();
        return repository.buscarTodos().stream().filter(p -> p.visibleEn(ahora))
            .filter(p -> especialidad == null || p.especialidades().contains(especialidad))
            .sorted(Comparator.comparing(Orientador::nombre).thenComparingLong(Orientador::id)).toList();
    }

    public Optional<Orientador> detalleVisible(long id) {
        return repository.buscarPorId(id).filter(p -> p.visibleEn(clock.instant()));
    }
}
