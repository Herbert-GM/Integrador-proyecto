package pe.edu.utp.ensename;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import pe.edu.utp.ensename.config.MonitoreoFilter;
import static org.assertj.core.api.Assertions.*;

class MonitoreoTests {
    private final Logger logger = (Logger) LoggerFactory.getLogger(MonitoreoFilter.class);
    private ListAppender<ILoggingEvent> appender;
    private final MonitoreoFilter filter = new MonitoreoFilter();

    @BeforeEach void capturar() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }
    @AfterEach void liberar() { logger.detachAppender(appender); appender.stop(); }

    @Test void noRegistraSecretosNiIdentificadoresDelUsuario() throws Exception {
        var request = new MockHttpServletRequest("POST", "/orientadores/123456789/whatsapp");
        request.setQueryString("correo=privado@example.invalid&telefono=51999999999");
        request.addHeader("Authorization", "Bearer secreto");
        request.addHeader("X-Request-ID", "identificador-controlado-por-cliente");
        request.setContent("clave=contrasena-privada".getBytes());
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> ((jakarta.servlet.http.HttpServletResponse) res).setStatus(302));
        String log = appender.list.getFirst().getFormattedMessage();
        assertThat(log).contains("ruta=/orientadores/{id}/whatsapp", "estado=302", "duracion_ms=")
                .doesNotContain("123456789", "51999999999", "privado@", "secreto", "contrasena", "controlado-por-cliente");
        assertThat(response.getHeader("X-Request-ID")).matches("[0-9a-f-]{36}");
        assertThat(log).contains(response.getHeader("X-Request-ID"));
    }

    @Test void excepcionGeneraErrorSinPublicarMensaje() {
        var request = new MockHttpServletRequest("GET", "/salud");
        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> { throw new ServletException("password=secreto"); })).isInstanceOf(ServletException.class);
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(appender.list.getFirst().getFormattedMessage()).contains("estado=500").doesNotContain("password", "secreto");
        assertThat(appender.list.getFirst().getThrowableProxy()).isNull();
    }

    @Test void noOcultaFalloDeSondeoYDistingueLimiteDeAcceso() throws Exception {
        filter.doFilter(new MockHttpServletRequest("GET", "/vivo"), new MockHttpServletResponse(), (q, s) -> {});
        assertThat(appender.list).isEmpty();
        filter.doFilter(new MockHttpServletRequest("GET", "/vivo"), new MockHttpServletResponse(),
                (q, s) -> ((jakarta.servlet.http.HttpServletResponse) s).setStatus(503));
        assertThat(appender.list.getLast().getLevel()).isEqualTo(Level.ERROR);
        filter.doFilter(new MockHttpServletRequest("POST", "/ingresar"), new MockHttpServletResponse(),
                (q, s) -> ((jakarta.servlet.http.HttpServletResponse) s).setStatus(429));
        assertThat(appender.list.getLast().getLevel()).isEqualTo(Level.WARN);
    }

    @Test void rutaDesconocidaNoExponeTextoArbitrario() throws Exception {
        filter.doFilter(new MockHttpServletRequest("GET", "/privado@example.invalid"),
                new MockHttpServletResponse(), (q, s) -> {});
        assertThat(appender.list.getFirst().getFormattedMessage()).contains("ruta=/otra").doesNotContain("privado@");
    }
}
