package pe.edu.utp.ensename;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class EnsenameApplicationTests {
    @Autowired MockMvc mvc;

    @Test void inicioPublicoExplicaLaDemostracion() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("ENSEÑA-ME")));
    }
    @Test void catalogoRenderizaSoloPerfilesVisibles() throws Exception {
        mvc.perform(get("/catalogo")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Ana · perfil de ejemplo")))
            .andExpect(content().string(not(containsString("Perfil vencido"))))
            .andExpect(content().string(not(containsString("Perfil oculto"))));
    }
    @Test void detalleDemoNoExponeTelefonoNiEnlaceDeContacto() throws Exception {
        mvc.perform(get("/orientadores/1")).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("51900000001"))))
            .andExpect(content().string(not(containsString("wa.me"))));
    }
    @Test void urlDirectaNoEvadeVisibilidad() throws Exception {
        for (int id : new int[]{3, 4, 5, 999}) {
            mvc.perform(get("/orientadores/" + id)).andExpect(status().isNotFound());
        }
    }
    @Test void filtroSinResultadosTieneMensaje() throws Exception {
        mvc.perform(get("/catalogo").param("especialidad", "BASES_DE_DATOS"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Aún no hay perfiles disponibles")));
    }
    @Test void filtroDesconocidoSeRechaza() throws Exception {
        mvc.perform(get("/catalogo").param("especialidad", "INVALIDA")).andExpect(status().isBadRequest());
    }
    @Test void endpointsDeEscrituraNoEstanAbiertos() throws Exception {
        mvc.perform(post("/orientadores/1")).andExpect(status().isForbidden());
    }
}
