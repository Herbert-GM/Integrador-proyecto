package pe.edu.utp.ensename.config;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Observación de peticiones síncronas sin registrar datos de los usuarios. */
@Component
@Profile("produccion")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MonitoreoFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoreoFilter.class);
    private static final Set<String> RUTAS = Set.of("/", "/catalogo", "/ingresar", "/registro",
            "/mi-perfil", "/suscripcion", "/suscripcion/activar", "/salir", "/vivo", "/salud", "/error");
    private static final Set<String> METODOS = Set.of("GET", "POST", "HEAD", "PUT", "PATCH", "DELETE", "OPTIONS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String id = UUID.randomUUID().toString();
        response.setHeader("X-Request-ID", id);
        long inicio = System.nanoTime();
        boolean fallo = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            fallo = true;
            throw ex;
        } finally {
            int estado = fallo ? 500 : response.getStatus();
            long duracion = (System.nanoTime() - inicio) / 1_000_000;
            String ruta = rutaSegura(request.getRequestURI());
            String metodo = METODOS.contains(request.getMethod()) ? request.getMethod() : "OTRO";
            // Los sondeos y recursos estáticos correctos no generan ruido en los logs.
            boolean silencioso = ruta.equals("/vivo") || ruta.equals("/recurso-estatico");
            if (estado >= 500) {
                LOG.error("peticion id={} metodo={} ruta={} estado={} duracion_ms={}", id, metodo, ruta, estado, duracion);
            } else if (duracion >= 1000 || estado == 429) {
                LOG.warn("peticion id={} metodo={} ruta={} estado={} duracion_ms={}", id, metodo, ruta, estado, duracion);
            } else if (!silencioso || estado >= 400) {
                LOG.info("peticion id={} metodo={} ruta={} estado={} duracion_ms={}", id, metodo, ruta, estado, duracion);
            }
        }
    }

    private static String rutaSegura(String uri) {
        if (RUTAS.contains(uri)) return uri;
        if (uri.matches("/orientadores/[0-9]+")) return "/orientadores/{id}";
        if (uri.matches("/orientadores/[0-9]+/contacto")) return "/orientadores/{id}/contacto";
        if (uri.matches("/orientadores/[0-9]+/whatsapp")) return "/orientadores/{id}/whatsapp";
        if (uri.startsWith("/css/") || uri.startsWith("/img/")) return "/recurso-estatico";
        return "/otra";
    }
}
