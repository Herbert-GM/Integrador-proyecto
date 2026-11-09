package pe.edu.utp.ensename.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.utp.ensename.model.Suscripcion;

@Configuration
public class SeguridadConfig {
    @Bean
    Clock clock() { return Clock.system(Suscripcion.LIMA); }

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/", "/catalogo", "/orientadores/*", "/css/**", "/img/**", "/error").permitAll()
            .anyRequest().denyAll()).build();
    }
}
