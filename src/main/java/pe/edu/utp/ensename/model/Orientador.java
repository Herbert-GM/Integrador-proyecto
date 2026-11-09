package pe.edu.utp.ensename.model;

import java.time.Instant;
import java.util.Set;

public record Orientador(long id, String nombre, String descripcion, Set<Especialidad> especialidades,
                         String whatsapp, boolean habilitado, boolean publicar, Suscripcion suscripcion) {
    public Orientador {
        especialidades = especialidades == null ? Set.of() : Set.copyOf(especialidades);
    }

    public boolean visibleEn(Instant ahora) {
        return habilitado && publicar && nombre != null && !nombre.isBlank()
                && descripcion != null && !descripcion.isBlank() && !especialidades.isEmpty()
                && whatsapp != null && whatsapp.matches("[1-9][0-9]{7,14}")
                && suscripcion != null && suscripcion.vigenteEn(ahora);
    }
}
