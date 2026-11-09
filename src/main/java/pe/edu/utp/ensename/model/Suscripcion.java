package pe.edu.utp.ensename.model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/** Un mes calendario en Lima; inicio incluido y fin excluido. Sin cobros. */
public record Suscripcion(Instant inicio, Instant fin) {
    public static final ZoneId LIMA = ZoneId.of("America/Lima");

    public Suscripcion {
        Objects.requireNonNull(inicio);
        Objects.requireNonNull(fin);
        if (!fin.equals(inicio.atZone(LIMA).plusMonths(1).toInstant())) {
            throw new IllegalArgumentException("La suscripción debe durar un mes calendario.");
        }
    }

    public static Suscripcion activar(Instant ahora, Suscripcion anterior) {
        Objects.requireNonNull(ahora);
        if (anterior != null && anterior.vigenteEn(ahora)) return anterior;
        return new Suscripcion(ahora, ahora.atZone(LIMA).plusMonths(1).toInstant());
    }

    public boolean vigenteEn(Instant ahora) {
        return !ahora.isBefore(inicio) && ahora.isBefore(fin);
    }
}
