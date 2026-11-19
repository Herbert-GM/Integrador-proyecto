package pe.edu.utp.ensename;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.utp.ensename.repository.CuentaRepository;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("sqlserver")
@TestPropertySource(properties={"spring.datasource.url=jdbc:h2:mem:cuentas;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password="})
@Sql("/cuentas.sql")
class CuentasSeguridadTests {
    @Autowired MockMvc mvc;
    @Autowired CuentaRepository repo;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    private String correo;
    private static final String CLAVE="Prueba de seguridad 2026!";

    @BeforeEach void cuenta(){correo=java.util.UUID.randomUUID()+"@example.invalid";repo.registrar("Estudiante de prueba",correo,encoder.encode(CLAVE));}

    @Test void registroPersisteHashYRechazaDuplicado() throws Exception {
        String nuevo=java.util.UUID.randomUUID()+"@example.invalid";
        mvc.perform(post("/registro").with(csrf()).param("nombre","Nuevo").param("correo",nuevo).param("clave",CLAVE))
            .andExpect(redirectedUrl("/ingresar?registrado"));
        var c=repo.cuenta(nuevo).orElseThrow();assertThat(c.hash()).isNotEqualTo(CLAVE);assertThat(encoder.matches(CLAVE,c.hash())).isTrue();
        mvc.perform(post("/registro").with(csrf()).param("nombre","Otro").param("correo",nuevo).param("clave",CLAVE)).andExpect(status().isOk()).andExpect(view().name("registro"));
    }
    @Test void accesoValidoCreaSesionYSalidaLaInvalida() throws Exception {
        var resultado=mvc.perform(post("/ingresar").with(csrf()).param("correo",correo).param("clave",CLAVE))
            .andExpect(authenticated().withUsername(correo)).andExpect(redirectedUrl("/mi-perfil")).andReturn();
        var sesion=(MockHttpSession)resultado.getRequest().getSession(false);
        mvc.perform(get("/mi-perfil").session(sesion)).andExpect(status().isOk());
        mvc.perform(post("/salir").session(sesion).with(csrf())).andExpect(redirectedUrl("/ingresar?saliste"));
        assertThat(sesion.isInvalid()).isTrue();
    }
    @Test void accesoIncorrectoYCuentaDeshabilitadaSeRechazan() throws Exception {
        mvc.perform(post("/ingresar").with(csrf()).param("correo",correo).param("clave","incorrecta"))
            .andExpect(unauthenticated()).andExpect(redirectedUrl("/ingresar?error"));
        jdbc.update("UPDATE dbo.Usuario SET habilitado=0 WHERE correo=?",correo);
        mvc.perform(post("/ingresar").with(csrf()).param("correo",correo).param("clave",CLAVE))
            .andExpect(unauthenticated()).andExpect(redirectedUrl("/ingresar?error"));
    }
    @Test void anonimoNoAccedeADatosPrivados() throws Exception {
        mvc.perform(get("/mi-perfil")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/suscripcion")).andExpect(status().is3xxRedirection());
    }
    @Test void editarIgnoraIdAjenoYConservaSuPerfil() throws Exception {
        String otro=java.util.UUID.randomUUID()+"@example.invalid";repo.registrar("Otro estudiante",otro,encoder.encode(CLAVE));
        long ajeno=repo.cuenta(otro).orElseThrow().id();
        mvc.perform(post("/mi-perfil").with(user(correo)).with(csrf()).param("usuario_id",Long.toString(ajeno))
            .param("descripcion","Descripción propia").param("telefono","51900000001").param("especialidades","1").param("publicar","true"))
            .andExpect(redirectedUrl("/mi-perfil?guardado"));
        assertThat(repo.buscarPorId(ajeno).orElseThrow().descripcion()).isEmpty();
        assertThat(repo.buscarPorId(repo.cuenta(correo).orElseThrow().id()).orElseThrow().descripcion()).isEqualTo("Descripción propia");
    }
    @Test void csrfProtegeEdicionYActivacion() throws Exception {
        mvc.perform(post("/mi-perfil").with(user(correo))).andExpect(status().isForbidden());
        mvc.perform(post("/suscripcion/activar").with(user(correo))).andExpect(status().isForbidden());
    }
    @Test void activacionIgnoraPropietarioAjenoYSeRenderiza() throws Exception {
        String otro=java.util.UUID.randomUUID()+"@example.invalid";
        repo.registrar("Otro",otro,encoder.encode(CLAVE));
        long ajeno=repo.cuenta(otro).orElseThrow().id();
        mvc.perform(post("/suscripcion/activar").with(user(correo)).with(csrf()).param("usuario_id",Long.toString(ajeno)))
            .andExpect(redirectedUrl("/suscripcion?activada"));
        assertThat(repo.suscripcion(ajeno)).isNull();
        mvc.perform(get("/suscripcion").with(user(correo))).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Suscripción vigente")));
    }
    @Test void camposInvalidosNoSeGuardan() throws Exception {
        mvc.perform(post("/mi-perfil").with(user(correo)).with(csrf()).param("descripcion","Texto").param("telefono","abc").param("especialidades","999"))
            .andExpect(status().isOk()).andExpect(view().name("mi-perfil"));
        assertThat(repo.seleccionadas(repo.cuenta(correo).orElseThrow().id())).isEmpty();
        mvc.perform(post("/registro").with(csrf()).param("nombre"," ").param("correo","invalido").param("clave","corta"))
            .andExpect(view().name("registro"));
    }
    @Test void datosDelPerfilSeEscapanAlRenderizar() throws Exception {
        long id=repo.cuenta(correo).orElseThrow().id();repo.guardar(id,"<script>alert(1)</script>","51900000001",java.util.Set.of(1),false);
        mvc.perform(get("/mi-perfil").with(user(correo))).andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script>alert(1)</script>"))));
    }
    @Test void contactoCodificaTextoYCompruebaPublicacionAlSalir() throws Exception {
        long id = repo.cuenta(correo).orElseThrow().id();
        repo.guardar(id, "Orientación académica", "51900000001", java.util.Set.of(1), true);
        repo.activar(id, java.time.Instant.now());
        mvc.perform(get("/orientadores/" + id + "/contacto"))
            .andExpect(status().isOk()).andExpect(view().name("contacto"));
        var resultado = mvc.perform(get("/orientadores/" + id + "/whatsapp"))
            .andExpect(status().isFound()).andReturn();
        var uri = java.net.URI.create(resultado.getResponse().getHeader("Location"));
        assertThat(uri.getScheme()).isEqualTo("https");
        assertThat(uri.getHost()).isEqualTo("wa.me");
        assertThat(uri.getPath()).isEqualTo("/51900000001");
        assertThat(java.net.URLDecoder.decode(uri.getRawQuery(), java.nio.charset.StandardCharsets.UTF_8))
            .contains("ENSEÑA-ME").contains("orientación académica");
        repo.guardar(id, "Orientación académica", "51900000001", java.util.Set.of(1), false);
        mvc.perform(get("/orientadores/" + id + "/whatsapp")).andExpect(status().isNotFound());
        mvc.perform(get("/orientadores/" + id + "/contacto")).andExpect(status().isNotFound());
    }

    @Test void contactoNoAceptaNumeroNiDestinoDelVisitante() throws Exception {
        long id = repo.cuenta(correo).orElseThrow().id();
        repo.guardar(id, "Orientación", "51900000001", java.util.Set.of(1), true);
        repo.activar(id, java.time.Instant.now());
        mvc.perform(get("/orientadores/" + id + "/whatsapp").param("telefono", "51999999999")
                .param("redirect", "https://otro.invalid"))
            .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("https://wa.me/51900000001?text=")));
        jdbc.update("UPDATE dbo.Suscripcion SET inicio=?,fin=? WHERE usuario_id=?",
            java.time.OffsetDateTime.parse("2020-01-01T10:00:00-05:00"),
            java.time.OffsetDateTime.parse("2020-02-01T10:00:00-05:00"),id);
        mvc.perform(get("/orientadores/" + id + "/contacto")).andExpect(status().isNotFound());
    }
}
