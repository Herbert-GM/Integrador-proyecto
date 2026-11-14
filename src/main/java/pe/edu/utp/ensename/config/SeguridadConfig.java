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
            .anyRequest().denyAll())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'none'; style-src 'self'; img-src 'self'; font-src 'self'; "
                    + "base-uri 'none'; form-action 'self'; frame-ancestors 'none'; object-src 'none'"))
                .referrerPolicy(referrer -> referrer.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .addHeaderWriter(new org.springframework.security.web.header.writers.StaticHeadersWriter(
                    "Permissions-Policy", "camera=(), microphone=(), geolocation=()")))
            .build();
    }
}
