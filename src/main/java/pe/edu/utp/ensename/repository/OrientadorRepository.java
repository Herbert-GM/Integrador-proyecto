package pe.edu.utp.ensename.repository;

import java.util.List;
import java.util.Optional;
import pe.edu.utp.ensename.model.Orientador;

public interface OrientadorRepository {
    List<Orientador> buscarTodos();
    Optional<Orientador> buscarPorId(long id);
}
