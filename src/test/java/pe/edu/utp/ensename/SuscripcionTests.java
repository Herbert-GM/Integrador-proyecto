package pe.edu.utp.ensename;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import pe.edu.utp.ensename.model.Suscripcion;
import static org.assertj.core.api.Assertions.*;

class SuscripcionTests {
    private final Instant inicio = Instant.parse("2026-01-31T15:00:00Z");

    @Test void ajustaAlUltimoDiaDelMes() {
        assertThat(Suscripcion.activar(inicio, null).fin()).isEqualTo(Instant.parse("2026-02-28T15:00:00Z"));
    }
    @Test void incluyeInicioYExcluyeFin() {
        var s = Suscripcion.activar(inicio, null);
        assertThat(s.vigenteEn(inicio.minusNanos(1))).isFalse();
        assertThat(s.vigenteEn(inicio)).isTrue();
        assertThat(s.vigenteEn(s.fin().minusNanos(1))).isTrue();
        assertThat(s.vigenteEn(s.fin())).isFalse();
    }
    @Test void repetirActivacionNoExtiendeVigencia() {
        var s = Suscripcion.activar(inicio, null);
        assertThat(Suscripcion.activar(inicio.plusSeconds(60), s)).isSameAs(s);
    }
    @Test void reactivarVencidaCreaNuevoPeriodo() {
        var s = Suscripcion.activar(inicio, null);
        assertThat(Suscripcion.activar(s.fin(), s).inicio()).isEqualTo(s.fin());
        assertThat(Suscripcion.activar(s.fin(), s).fin()).isEqualTo(Instant.parse("2026-03-28T15:00:00Z"));
    }
    @Test void usaElDiaDeLimaAunqueUtcEsteEnOtroMes() {
        var s = Suscripcion.activar(Instant.parse("2026-02-01T02:00:00Z"), null);
        assertThat(s.fin()).isEqualTo(Instant.parse("2026-03-01T02:00:00Z"));
    }
    @Test void contemplaAnioBisiesto() {
        var s = Suscripcion.activar(Instant.parse("2028-01-31T15:00:00Z"), null);
        assertThat(s.fin()).isEqualTo(Instant.parse("2028-02-29T15:00:00Z"));
    }
    @Test void rechazaDuracionArbitraria() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Suscripcion(inicio, inicio.plusSeconds(60)));
    }
}
