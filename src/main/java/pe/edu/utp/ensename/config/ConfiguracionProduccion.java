package pe.edu.utp.ensename.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration
@Profile("produccion")
public class ConfiguracionProduccion {
    public static void validar(Environment environment) {
        if (!environment.acceptsProfiles(Profiles.of("sqlserver"))
                || environment.acceptsProfiles(Profiles.of("demo"))) {
            throw new IllegalStateException("Producción requiere sqlserver y no permite demo.");
        }
        String url = environment.getProperty("DB_URL", "");
        List<String> opciones = Arrays.stream(url.toLowerCase(Locale.ROOT).split(";"))
            .map(String::strip).toList();
        if (!url.startsWith("jdbc:sqlserver://")
                || opciones.stream().filter(s -> s.startsWith("encrypt=")).count() != 1
                || !opciones.contains("encrypt=true")
                || opciones.stream().filter(s -> s.startsWith("trustservercertificate=")).count() != 1
                || !opciones.contains("trustservercertificate=false")
                || opciones.stream().anyMatch(s -> s.startsWith("integratedsecurity=") && !s.equals("integratedsecurity=false"))
                || opciones.stream().anyMatch(s -> s.startsWith("password=") || s.startsWith("user="))) {
            throw new IllegalStateException("DB_URL debe usar SQL Server con encrypt=true y trustServerCertificate=false, sin credenciales ni autenticación Windows.");
        }
        String usuario = environment.getProperty("DB_USERNAME", "").strip();
        if (usuario.isBlank() || usuario.equalsIgnoreCase("sa")
                || environment.getProperty("DB_PASSWORD", "").isBlank()) {
            throw new IllegalStateException("Configura una cuenta de base de datos limitada mediante DB_USERNAME y DB_PASSWORD.");
        }
    }

    @Bean
    ApplicationRunner comprobarProduccion(Environment environment) {
        return args -> validar(environment);
    }
}
