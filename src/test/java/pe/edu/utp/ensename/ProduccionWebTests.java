package pe.edu.utp.ensename;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"sqlserver", "produccion"})
@TestPropertySource(properties={
    "spring.datasource.url=jdbc:h2:mem:produccion;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "DB_URL=jdbc:sqlserver://prueba.database.windows.net:1433;databaseName=Ensename;encrypt=true;trustServerCertificate=false",
    "DB_USERNAME=ensename_app", "DB_PASSWORD=ficticia-solo-para-test"})
class ProduccionWebTests {
    @Autowired MockMvc mvc;

    @Test void redirigeHttpAHttps() throws Exception {
        mvc.perform(get("/ingresar")).andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", startsWith("https://")));
    }

    @Test void saludHttpsNoExponeDatosDeConexion() throws Exception {
        mvc.perform(get("https://localhost/salud").secure(true)).andExpect(status().isOk())
            .andExpect(content().json("{\"estado\":\"disponible\"}"))
            .andExpect(header().string("Strict-Transport-Security", containsString("max-age=")));
    }
}
