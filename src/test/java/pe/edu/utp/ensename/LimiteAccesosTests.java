package pe.edu.utp.ensename;

import java.time.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import pe.edu.utp.ensename.config.*;
import static org.assertj.core.api.Assertions.*;

class LimiteAccesosTests {
    static class Reloj extends Clock {
        final AtomicReference<Instant> ahora = new AtomicReference<>(Instant.parse("2026-11-18T15:00:00Z"));
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return ahora.get(); }
    }

    @Test void limitaIntentosYLiberaAlTerminarLaVentana() {
        var reloj = new Reloj();
        var limite = new LimiteAccesos(reloj, 100);
        for (int i = 0; i < 10; i++) assertThat(limite.permitir("uno", "/ingresar")).isTrue();
        assertThat(limite.permitir("uno", "/ingresar")).isFalse();
        assertThat(limite.permitir("otro", "/ingresar")).isTrue();
        reloj.ahora.updateAndGet(i -> i.plusSeconds(900));
        assertThat(limite.permitir("uno", "/ingresar")).isTrue();
    }

    @Test void registroTieneLimiteIndependienteYMemoriaAcotada() {
        var reloj = new Reloj();
        var limite = new LimiteAccesos(reloj, 2);
        for (int i = 0; i < 5; i++) assertThat(limite.permitir("uno", "/registro")).isTrue();
        assertThat(limite.permitir("uno", "/registro")).isFalse();
        assertThat(limite.permitir("uno", "/ingresar")).isTrue();
        assertThat(limite.permitir("otro", "/ingresar")).isFalse();
        reloj.ahora.updateAndGet(i -> i.plusSeconds(3600));
        assertThat(limite.permitir("otro", "/ingresar")).isTrue();
    }

    @Test void filtroResponde429YNoEjecutaLaAutenticacion() throws Exception {
        var filtro = new LimiteAccesosFilter(new LimiteAccesos(new Reloj(), 100));
        for (int i = 0; i < 10; i++) {
            var request = new MockHttpServletRequest("POST", "/ingresar");
            request.setServletPath("/ingresar");
            filtro.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        }
        var request = new MockHttpServletRequest("POST", "/ingresar");
        request.setServletPath("/ingresar");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filtro.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("900");
        assertThat(chain.getRequest()).isNull();
    }
}
