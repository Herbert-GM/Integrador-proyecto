package pe.edu.utp.ensename;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class SeguridadWebTests {
    @Autowired MockMvc mvc;

    @Test void aplicaCabecerasDeProteccion() throws Exception {
        mvc.perform(get("/catalogo")).andExpect(status().isOk())
            .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
            .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
            .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test void rechazaEscriturasInclusoConCsrfYUsuarioAutenticado() throws Exception {
        for (String ruta : new String[]{"/orientadores/1", "/mi-perfil", "/suscripcion"}) {
            mvc.perform(post(ruta).with(csrf()).with(user("otro")))
                .andExpect(status().isForbidden());
        }
    }

    @Test void csrfRechazaSolicitudesSinToken() throws Exception {
        mvc.perform(post("/orientadores/1")).andExpect(status().isForbidden());
        mvc.perform(post("/orientadores/1").with(csrf().useInvalidToken()))
            .andExpect(status().isForbidden());
    }

    @Test void noExponeArchivosInternosNiEndpointsAdministrativos() throws Exception {
        for (String ruta : new String[]{"/.env", "/pom.xml", "/application.properties",
                "/docs/producto.md", "/actuator/env", "/actuator/heapdump", "/admin"}) {
            mvc.perform(get(ruta)).andExpect(status().isForbidden());
        }
    }

    @Test void contenidoMaliciosoNoSeInterpretaComoFiltro() throws Exception {
        for (String entrada : new String[]{"<script>alert(1)</script>", "' OR 1=1 --", "../application.properties"}) {
            mvc.perform(get("/catalogo").param("especialidad", entrada))
                .andExpect(status().isBadRequest());
        }
    }

    @Test void idMaliciosoNoPermiteConsultarOtroRecurso() throws Exception {
        mvc.perform(get("/orientadores/no-es-numero")).andExpect(status().isBadRequest());
        mvc.perform(get("/orientadores/-1")).andExpect(status().isNotFound());
    }

    @Test void recursosDeLaInterfazSonPublicos() throws Exception {
        mvc.perform(get("/css/estilos.css")).andExpect(status().isOk());
        mvc.perform(get("/img/figma/ccd6c.svg")).andExpect(status().isOk());
    }

    @Test void noHabilitaCorsParaOrigenAjeno() throws Exception {
        mvc.perform(get("/catalogo").header("Origin", "https://ejemplo-ajeno.invalid"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
