package pe.edu.utp.ensename.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Límite local por dirección y operación; no bloquea permanentemente una cuenta. */
public class LimiteAccesos {
    private record Ventana(Instant inicio, int usados) {}
    private final Map<String, Ventana> ventanas = new HashMap<>();
    private final Clock clock;
    private final int capacidad;

    public LimiteAccesos(Clock clock, int capacidad) {
        this.clock = clock;
        this.capacidad = capacidad;
    }

    public synchronized boolean permitir(String direccion, String operacion) {
        Instant ahora = clock.instant();
        ventanas.entrySet().removeIf(e -> !ahora.isBefore(e.getValue().inicio().plus(Duration.ofHours(1))));
        String clave = operacion + ":" + direccion;
        int limite = operacion.equals("/registro") ? 5 : 10;
        Duration duracion = operacion.equals("/registro") ? Duration.ofHours(1) : Duration.ofMinutes(15);
        Ventana anterior = ventanas.get(clave);
        if (anterior == null || !ahora.isBefore(anterior.inicio().plus(duracion))) {
            if (anterior == null && ventanas.size() >= capacidad) return false;
            ventanas.put(clave, new Ventana(ahora, 1));
            return true;
        }
        if (anterior.usados() >= limite) return false;
        ventanas.put(clave, new Ventana(anterior.inicio(), anterior.usados() + 1));
        return true;
    }
}
