package pe.edu.utp.ensename;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.utp.ensename.repository.CuentaRepository;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("postgres")
@TestPropertySource(properties={
    "spring.datasource.url=jdbc:postgresql://127.0.0.1:55432/ensename",
    "spring.datasource.username=ensename_test", "spring.datasource.password="})
@EnabledIfEnvironmentVariable(named="RUN_POSTGRES_TESTS",matches="true")
class PostgresConcurrenciaTests {
    @Autowired CuentaRepository repo;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    @Test void solicitudesSimultaneasCreanUnSoloPeriodo() throws Exception {
        String correo=UUID.randomUUID()+"@example.invalid";
        repo.registrar("Prueba temporal de concurrencia",correo,encoder.encode("Prueba temporal de concurrencia"));
        long id=repo.cuenta(correo).orElseThrow().id();
        try (var executor=Executors.newFixedThreadPool(2)) {
            var salida=new CountDownLatch(1);
            Callable<pe.edu.utp.ensename.model.Suscripcion> tarea=()->{
                salida.await();
                return repo.activar(id,Instant.parse("2026-01-31T15:00:00Z"));
            };
            var primera=executor.submit(tarea);
            var segunda=executor.submit(tarea);
            salida.countDown();
            var s=primera.get(15,TimeUnit.SECONDS);
            assertThat(segunda.get(15,TimeUnit.SECONDS)).isEqualTo(s);
            assertThat(s.fin()).isEqualTo(Instant.parse("2026-02-28T15:00:00Z"));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.suscripcion WHERE usuario_id=?",Integer.class,id)).isEqualTo(1);
        } finally {
            // Solo se limpia la cuenta aleatoria creada en esta base local aislada de pruebas.
            jdbc.update("DELETE FROM dbo.suscripcion WHERE usuario_id=?",id);
            jdbc.update("DELETE FROM dbo.orientador WHERE usuario_id=?",id);
            jdbc.update("DELETE FROM dbo.usuario WHERE usuario_id=?",id);
        }
    }
}
