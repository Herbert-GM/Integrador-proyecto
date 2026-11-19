package pe.edu.utp.ensename.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.utp.ensename.model.Orientador;
import pe.edu.utp.ensename.service.CatalogoService;

@Controller
@Profile("sqlserver")
public class ContactoController {
    private final CatalogoService catalogo;

    public ContactoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    private Orientador visible(long id) {
        return catalogo.detalleVisible(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/orientadores/{id}/contacto")
    String confirmar(@PathVariable long id, Model model) {
        model.addAttribute("orientador", visible(id));
        return "contacto";
    }

    @GetMapping("/orientadores/{id}/whatsapp")
    ResponseEntity<Void> abrir(@PathVariable long id) {
        // Se vuelve a comprobar la publicación al salir, aunque la página se abriera antes.
        Orientador perfil = visible(id);
        String mensaje = URLEncoder.encode(
            "Hola, encontré tu perfil en ENSEÑA-ME y quisiera consultarte sobre orientación académica.",
            StandardCharsets.UTF_8);
        URI destino = URI.create("https://wa.me/" + perfil.whatsapp() + "?text=" + mensaje);
        return ResponseEntity.status(HttpStatus.FOUND).location(destino).build();
    }
}
