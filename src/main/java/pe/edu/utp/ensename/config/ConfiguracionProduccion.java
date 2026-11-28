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
        boolean postgres = environment.acceptsProfiles(Profiles.of("postgres"));
        boolean sqlserver = environment.acceptsProfiles(Profiles.of("sqlserver"));
        if (postgres == sqlserver || environment.acceptsProfiles(Profiles.of("demo", "exposicion"))) {
            throw new IllegalStateException("Producción requiere exactamente un perfil de base de datos y no permite demo.");
        }
        String url = environment.getProperty("DB_URL", "");
        List<String> opciones = Arrays.stream(url.toLowerCase(Locale.ROOT).split(";"))
            .map(String::strip).toList();
        if (postgres) {
            validarPostgres(url);
        } else if (!url.startsWith("jdbc:sqlserver://")
                || opciones.stream().filter(s -> s.startsWith("encrypt=")).count() != 1
                || !opciones.contains("encrypt=true")
                || opciones.stream().filter(s -> s.startsWith("trustservercertificate=")).count() != 1
                || !opciones.contains("trustservercertificate=false")
                || opciones.stream().anyMatch(s -> s.startsWith("integratedsecurity=") && !s.equals("integratedsecurity=false"))
                || opciones.stream().anyMatch(s -> s.startsWith("password=") || s.startsWith("user="))) {
            throw new IllegalStateException("DB_URL debe usar SQL Server con encrypt=true y trustServerCertificate=false, sin credenciales ni autenticación Windows.");
        }
        String usuario = environment.getProperty("DB_USERNAME", "").strip();
        if (usuario.isBlank() || (usuario.equalsIgnoreCase("sa") || usuario.equalsIgnoreCase("postgres"))
                || environment.getProperty("DB_PASSWORD", "").isBlank()) {
            throw new IllegalStateException("Configura una cuenta de base de datos limitada mediante DB_USERNAME y DB_PASSWORD.");
        }
    }

    private static void validarPostgres(String url) {
        try {
            if (!url.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException();
            java.net.URI uri = java.net.URI.create(url.substring(5));
            if (uri.getHost() == null || uri.getUserInfo() != null || uri.getRawQuery() == null) {
                throw new IllegalArgumentException();
            }
            java.util.Map<String,String> parametros = new java.util.HashMap<>();
            for (String parte : uri.getRawQuery().split("&")) {
                String[] par = parte.split("=", 2);
                String clave = java.net.URLDecoder.decode(par[0], java.nio.charset.StandardCharsets.UTF_8);
                String valor = par.length == 2 ? java.net.URLDecoder.decode(par[1], java.nio.charset.StandardCharsets.UTF_8) : "";
                if (parametros.putIfAbsent(clave, valor) != null) throw new IllegalArgumentException();
            }
            if (!"verify-full".equals(parametros.get("sslmode"))
                    || !"org.postgresql.ssl.DefaultJavaSSLFactory".equals(parametros.get("sslfactory"))
                    || parametros.keySet().stream().anyMatch(k -> java.util.Set.of("user","password","sslhostnameverifier","ssl").contains(k.toLowerCase(Locale.ROOT)))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("DB_URL de PostgreSQL debe usar sslmode=verify-full y la fábrica SSL estándar de Java, sin credenciales ni verificadores personalizados.");
        }
    }

    @Bean
    ApplicationRunner comprobarProduccion(Environment environment) {
        return args -> validar(environment);
    }
}
