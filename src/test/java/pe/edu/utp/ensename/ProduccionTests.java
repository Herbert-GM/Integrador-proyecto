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
    private MockEnvironment postgres(String parametros) {
        var e = valida(); e.setActiveProfiles("postgres", "produccion");
        return e.withProperty("DB_URL", "jdbc:postgresql://prueba.neon.tech/ensename?" + parametros);
    }
    @Test void postgresValidaCertificadoYHost() {
        var e = postgres("sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory");
        assertThatCode(() -> ConfiguracionProduccion.validar(e)).doesNotThrowAnyException();
        for (String opciones : new String[]{"sslmode=require", "sslmode=disable",
                "sslmode=verify-full&sslfactory=org.postgresql.ssl.NonValidatingFactory",
                "sslmode=verify-full&sslmode=disable&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory",
                "sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory&password=prueba"}) {
            assertThatIllegalStateException().isThrownBy(() -> ConfiguracionProduccion.validar(postgres(opciones)));
        }
    }
    @Test void noPermiteDosMotoresALaVez() {
        var e=valida();e.setActiveProfiles("sqlserver", "postgres", "produccion");
        assertThatIllegalStateException().isThrownBy(() -> ConfiguracionProduccion.validar(e));
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
