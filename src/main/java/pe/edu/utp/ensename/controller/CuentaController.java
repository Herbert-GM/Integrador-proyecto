package pe.edu.utp.ensename.controller;
import java.security.Principal;
import java.time.Clock;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import pe.edu.utp.ensename.repository.CuentaRepository;

@Controller @Profile({"sqlserver", "postgres"})
public class CuentaController {
    private final CuentaRepository repo; private final PasswordEncoder encoder; private final Clock clock;
    public CuentaController(CuentaRepository repo,PasswordEncoder encoder,Clock clock){this.repo=repo;this.encoder=encoder;this.clock=clock;}
    public static class Registro {
        @NotBlank @Size(max=120) public String nombre;
        @NotBlank @Email @Size(max=254) public String correo;
        @NotBlank @Size(min=12,max=64) public String clave;
        public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
        public String getCorreo(){return correo;} public void setCorreo(String v){correo=v;}
        public String getClave(){return clave;} public void setClave(String v){clave=v;}
    }
    public static class Perfil {
        @NotBlank @Size(max=1000) public String descripcion;
        @Pattern(regexp="[1-9][0-9]{7,14}") @NotBlank public String telefono;
        @NotEmpty @Size(max=20) public Set<Integer> especialidades=new HashSet<>();
        public boolean publicar;
        public String getDescripcion(){return descripcion;}public void setDescripcion(String v){descripcion=v;}
        public String getTelefono(){return telefono;}public void setTelefono(String v){telefono=v;}
        public Set<Integer> getEspecialidades(){return especialidades;}public void setEspecialidades(Set<Integer> v){especialidades=v;}
        public boolean isPublicar(){return publicar;}public void setPublicar(boolean v){publicar=v;}
    }
    @GetMapping("/ingresar") String ingreso(){return "ingresar";}
    @GetMapping("/registro") String registro(Model m){m.addAttribute("registro",new Registro());return "registro";}
    @PostMapping("/registro") String registrar(@Valid @ModelAttribute("registro") Registro f,BindingResult errores){
        if(f.clave!=null&&f.clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)errores.rejectValue("clave","longitud","La contraseña admite hasta 72 bytes UTF-8.");
        if(errores.hasErrors())return "registro";
        try{repo.registrar(f.nombre.strip(),f.correo.strip().toLowerCase(Locale.ROOT),encoder.encode(f.clave));}
        catch(DuplicateKeyException e){errores.reject("registro","No se pudo registrar esa cuenta. Prueba iniciar sesión o utiliza otro correo.");return "registro";}
        return "redirect:/ingresar?registrado";
    }
    private CuentaRepository.Cuenta propia(Principal p){return repo.cuenta(p.getName()).filter(CuentaRepository.Cuenta::habilitado).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN));}
    @GetMapping("/mi-perfil") String perfil(Principal p,Model m){var c=propia(p);var o=repo.buscarPorId(c.id()).orElseThrow();var f=new Perfil();f.descripcion=o.descripcion();f.telefono=o.whatsapp();f.publicar=o.publicar();f.especialidades=repo.seleccionadas(c.id());m.addAttribute("perfil",f);m.addAttribute("especialidades",repo.especialidades());return "mi-perfil";}
    @PostMapping("/mi-perfil") String guardar(Principal p,@Valid @ModelAttribute("perfil") Perfil f,BindingResult errores,Model m){var c=propia(p);var permitidas=repo.especialidades();if(f.especialidades!=null&&!permitidas.stream().map(CuentaRepository.EspecialidadDato::id).toList().containsAll(f.especialidades))errores.rejectValue("especialidades","invalida","Especialidad no válida.");if(errores.hasErrors()){m.addAttribute("especialidades",permitidas);return "mi-perfil";}repo.guardar(c.id(),f.descripcion.strip(),f.telefono,f.especialidades,f.publicar);return "redirect:/mi-perfil?guardado";}
    @GetMapping("/suscripcion") String suscripcion(Principal p,Model m){var s=repo.suscripcion(propia(p).id());m.addAttribute("suscripcion",s);m.addAttribute("vigente",s!=null&&s.vigenteEn(clock.instant()));return "suscripcion";}
    @PostMapping("/suscripcion/activar") String activar(Principal p){repo.activar(propia(p).id(),clock.instant());return "redirect:/suscripcion?activada";}
}
