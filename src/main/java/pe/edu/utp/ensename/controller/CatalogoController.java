package pe.edu.utp.ensename.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.utp.ensename.model.Especialidad;
import pe.edu.utp.ensename.service.CatalogoService;

@Controller
public class CatalogoController {
    private final CatalogoService catalogo;
    public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }

    @GetMapping("/")
    String inicio() { return "index"; }

    @GetMapping("/catalogo")
    String catalogo(@RequestParam(required = false) Especialidad especialidad, Model model) {
        model.addAttribute("especialidades", Especialidad.values());
        model.addAttribute("seleccionada", especialidad);
        model.addAttribute("orientadores", catalogo.buscar(especialidad));
        return "catalogo";
    }

    @GetMapping("/orientadores/{id}")
    String perfil(@PathVariable long id, Model model) {
        model.addAttribute("orientador", catalogo.detalleVisible(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "perfil";
    }
}
