package pe.edu.utp.ensename;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.utp.ensename.config.*;
import pe.edu.utp.ensename.controller.ContactoController;
import pe.edu.utp.ensename.service.CatalogoService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExposicionTests {
    private MockEnvironment local(){var e=new MockEnvironment();e.setActiveProfiles("postgres","exposicion");return e.withProperty("spring.datasource.url","jdbc:postgresql://127.0.0.1:55432/ensename_exposicion").withProperty("server.address","127.0.0.1");}
    @Test void soloPermiteBaseYDireccionAisladas() {
        assertThatCode(()->ExposicionConfig.validar(local())).doesNotThrowAnyException();
        assertThatIllegalStateException().isThrownBy(()->ExposicionConfig.validar(local().withProperty("server.address","0.0.0.0")));
        assertThatIllegalStateException().isThrownBy(()->ExposicionConfig.validar(local().withProperty("spring.datasource.url","jdbc:postgresql://neon.tech/neondb")));
        assertThatIllegalStateException().isThrownBy(()->ExposicionConfig.validar(local().withProperty("spring.datasource.url","jdbc:postgresql://127.0.0.1:55432/ensename")));
        var e=local();e.setActiveProfiles("postgres","produccion","exposicion");
        assertThatIllegalStateException().isThrownBy(()->ExposicionConfig.validar(e));
        assertThatIllegalStateException().isThrownBy(()->ConfiguracionProduccion.validar(e));
    }
    @Test void contactoDeExposicionSeBloqueaTambienPorUrlDirecta() throws Exception {
        var catalogo=mock(CatalogoService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new ContactoController(catalogo,local())).build();
        mvc.perform(get("/orientadores/1/contacto")).andExpect(status().isNotFound());
        mvc.perform(get("/orientadores/1/whatsapp")).andExpect(status().isNotFound());
        verifyNoInteractions(catalogo);
    }
}
