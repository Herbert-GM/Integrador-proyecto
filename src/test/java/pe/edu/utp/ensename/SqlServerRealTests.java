package pe.edu.utp.ensename;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.utp.ensename.repository.CuentaRepository;
import static org.assertj.core.api.Assertions.*;

/** Prueba optativa real: todas las escrituras se revierten al finalizar. */
@SpringBootTest
@ActiveProfiles("sqlserver")
@EnabledIfEnvironmentVariable(named="RUN_SQLSERVER_TESTS",matches="true")
@Transactional
class SqlServerRealTests {
    @Autowired CuentaRepository repo;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    @Test void windowsPersistePerfilYSuscripcionSinCobro() {
        assertThat(jdbc.queryForObject("SELECT SUSER_SNAME()",String.class)).doesNotContain("sa");
        String correo=UUID.randomUUID()+"@example.invalid";
        String clave="Prueba transaccional sin cuenta permanente";
        repo.registrar("Prueba temporal",correo,encoder.encode(clave));
        var cuenta=repo.cuenta(correo).orElseThrow();
        assertThat(encoder.matches(clave,cuenta.hash())).isTrue();
        int especialidad=repo.especialidades().getFirst().id();
        repo.guardar(cuenta.id(),"Perfil de prueba temporal","51900000001",Set.of(especialidad),true);
        var ahora=Instant.parse("2026-11-14T17:00:00Z");
        var s=repo.activar(cuenta.id(),ahora);
        assertThat(repo.activar(cuenta.id(),ahora.plusSeconds(30))).isEqualTo(s);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.Suscripcion WHERE usuario_id=?",Integer.class,cuenta.id())).isEqualTo(1);
        assertThat(repo.buscarPorId(cuenta.id()).orElseThrow().visibleEn(ahora)).isTrue();
        assertThat(repo.buscarPorId(cuenta.id()).orElseThrow().visibleEn(s.fin())).isFalse();
        repo.guardar(cuenta.id(),"Perfil de prueba temporal","51900000001",Set.of(especialidad),false);
        assertThat(repo.buscarPorId(cuenta.id()).orElseThrow().visibleEn(ahora)).isFalse();
    }
}
