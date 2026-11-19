package pe.edu.utp.ensename.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.env.*;
@ControllerAdvice
public class EstadoInterfaz {
 private final Environment environment;
 public EstadoInterfaz(Environment environment){this.environment=environment;}
 @ModelAttribute("cuentasDisponibles") public boolean cuentas(){return environment.acceptsProfiles(Profiles.of("sqlserver", "postgres"));}
}
