package pe.edu.utp.ensename.config;
import org.springframework.context.annotation.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.utp.ensename.repository.CuentaRepository;
@Configuration
public class CuentasConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
    @Bean @Profile({"sqlserver", "postgres"}) UserDetailsService usuarios(CuentaRepository repo){
        return correo->{var c=repo.cuenta(correo.strip().toLowerCase(java.util.Locale.ROOT)).orElseThrow(()->new UsernameNotFoundException("Credenciales incorrectas"));return User.withUsername(c.correo()).password(c.hash()).roles("ESTUDIANTE").disabled(!c.habilitado()).build();};
    }
}
