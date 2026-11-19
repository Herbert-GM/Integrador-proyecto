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
    SecurityFilterChain seguridad(HttpSecurity http, org.springframework.core.env.Environment environment) throws Exception {
        boolean cuentas = environment.acceptsProfiles(org.springframework.core.env.Profiles.of("sqlserver"));
        if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("produccion"))) {
            http.redirectToHttps(org.springframework.security.config.Customizer.withDefaults());
        }
        http.authorizeHttpRequests(auth -> {
            if (cuentas) {
                auth.requestMatchers("/ingresar", "/registro").permitAll()
                    .requestMatchers("/mi-perfil", "/suscripcion", "/suscripcion/activar").authenticated();
            }
            auth
            .requestMatchers(org.springframework.http.HttpMethod.GET, "/", "/salud", "/catalogo", "/orientadores/*", "/orientadores/*/contacto", "/orientadores/*/whatsapp", "/css/**", "/img/**", "/error").permitAll()
            .anyRequest().denyAll();
        });
        if (cuentas) {
            http.addFilterBefore(new LimiteAccesosFilter(new LimiteAccesos(clock(), 10000)),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
            http.formLogin(login -> login.loginPage("/ingresar").usernameParameter("correo")
                    .passwordParameter("clave").defaultSuccessUrl("/mi-perfil", true)
                    .failureUrl("/ingresar?error"))
                .logout(logout -> logout.logoutUrl("/salir").logoutSuccessUrl("/ingresar?saliste")
                    .invalidateHttpSession(true).deleteCookies("JSESSIONID"))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()));
        }
        return http
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
