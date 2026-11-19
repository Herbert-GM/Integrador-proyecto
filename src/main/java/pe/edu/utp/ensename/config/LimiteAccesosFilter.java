package pe.edu.utp.ensename.config;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class LimiteAccesosFilter extends OncePerRequestFilter {
    private final LimiteAccesos limite;

    public LimiteAccesosFilter(LimiteAccesos limite) { this.limite = limite; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String ruta = request.getServletPath();
        if (request.getMethod().equals("POST") && (ruta.equals("/ingresar") || ruta.equals("/registro"))
                && !limite.permitir(request.getRemoteAddr(), ruta)) {
            response.setStatus(429);
            response.setHeader("Retry-After", ruta.equals("/registro") ? "3600" : "900");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Demasiados intentos. Espera unos minutos antes de volver a intentarlo.");
            return;
        }
        chain.doFilter(request, response);
    }
}
