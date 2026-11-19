package pe.edu.utp.ensename;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import pe.edu.utp.ensename.config.ConfiguracionProduccion;
import static org.assertj.core.api.Assertions.*;

class ProduccionTests {
    private MockEnvironment valida() {
        var e = new MockEnvironment();
        e.setActiveProfiles("sqlserver", "produccion");
        return e.withProperty("DB_URL", "jdbc:sqlserver://prueba.database.windows.net:1433;databaseName=Ensename;encrypt=true;trustServerCertificate=false")
            .withProperty("DB_USERNAME", "ensename_app").withProperty("DB_PASSWORD", "valor-ficticio-de-prueba");
    }

    @Test void admiteConfiguracionCifradaExterna() {
        assertThatCode(() -> ConfiguracionProduccion.validar(valida())).doesNotThrowAnyException();
    }

    @Test void rechazaDemoCredencialesAdministrativasYCertificadoSinValidar() {
        var demo = valida(); demo.setActiveProfiles("demo", "sqlserver", "produccion");
        assertThatIllegalStateException().isThrownBy(() -> ConfiguracionProduccion.validar(demo));
        var admin = valida().withProperty("DB_USERNAME", "sa");
        assertThatIllegalStateException().isThrownBy(() -> ConfiguracionProduccion.validar(admin));
        var sinCertificado = valida().withProperty("DB_URL", "jdbc:sqlserver://local;encrypt=true;trustServerCertificate=true");
        assertThatIllegalStateException().isThrownBy(() -> ConfiguracionProduccion.validar(sinCertificado));
        var vacia = valida().withProperty("DB_PASSWORD", "");
        assertThatIllegalStateException().isThrownBy(() -> ConfiguracionProduccion.validar(vacia));
    }
}
