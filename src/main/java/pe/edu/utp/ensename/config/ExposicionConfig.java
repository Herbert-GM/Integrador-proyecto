package pe.edu.utp.ensename.config;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.utp.ensename.repository.*;

@Configuration
@Profile("exposicion")
public class ExposicionConfig {
    public static void validar(Environment env) {
        if(env.acceptsProfiles(Profiles.of("produccion", "demo", "sqlserver"))
            || !env.acceptsProfiles(Profiles.of("postgres"))
            || !"jdbc:postgresql://127.0.0.1:55432/ensename_exposicion".equals(env.getProperty("spring.datasource.url"))
            || !"127.0.0.1".equals(env.getProperty("server.address")))
            throw new IllegalStateException("Exposición solo admite su base local aislada y dirección 127.0.0.1; nunca producción.");
    }
    @Bean ApplicationRunner prepararExposicion(Environment env, CuentaRepository repo, JdbcTemplate jdbc,
            PasswordEncoder encoder, Clock clock, org.springframework.transaction.PlatformTransactionManager transacciones) {
        return args -> {
            validar(env);
            String directorio=jdbc.queryForObject("SHOW data_directory",String.class);
            String esperado=env.getProperty("ensename.exposicion.directorio", "");
            if(esperado.isBlank() || !java.nio.file.Path.of(esperado).toAbsolutePath().normalize().equals(java.nio.file.Path.of(directorio).toAbsolutePath().normalize()))
                throw new IllegalStateException("No se confirmó el directorio PostgreSQL aislado del proyecto.");
            // No sobreescribe cambios del ensayo al reiniciar ni renueva suscripciones.
            new org.springframework.transaction.support.TransactionTemplate(transacciones).executeWithoutResult(estado -> {
            for(var perfil:PerfilesDemostracion.crear(clock)) {
                String correo=perfil.id()==3?"orientador@demo.invalid":"perfil"+perfil.id()+"@demo.invalid";
                if(repo.cuenta(correo).isPresent())continue;
                repo.registrar(perfil.nombre(),correo,encoder.encode(perfil.id()==3?"Demostracion 2026!":UUID.randomUUID().toString()));
                long id=repo.cuenta(correo).orElseThrow().id();
                Set<Integer> especialidades=repo.especialidades().stream()
                    .filter(e->perfil.especialidades().stream().anyMatch(p->p.getNombre().equals(e.nombre())))
                    .map(CuentaRepository.EspecialidadDato::id).collect(Collectors.toSet());
                repo.guardar(id,perfil.descripcion(),perfil.whatsapp(),especialidades,perfil.publicar());
                if(perfil.suscripcion()!=null)jdbc.update("INSERT INTO dbo.suscripcion(usuario_id,inicio,fin,es_simulada) VALUES(?,?,?,true)",
                    id,perfil.suscripcion().inicio().atZone(pe.edu.utp.ensename.model.Suscripcion.LIMA).toOffsetDateTime(),
                    perfil.suscripcion().fin().atZone(pe.edu.utp.ensename.model.Suscripcion.LIMA).toOffsetDateTime());
            }
            });
        };
    }
}
