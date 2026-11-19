package pe.edu.utp.ensename.controller;

import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaludController {
    private final ObjectProvider<JdbcTemplate> jdbc;
    public SaludController(ObjectProvider<JdbcTemplate> jdbc) { this.jdbc = jdbc; }

    @GetMapping("/salud")
    ResponseEntity<Map<String,String>> salud() {
        try {
            JdbcTemplate conexion = jdbc.getIfAvailable();
            if (conexion != null) conexion.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("estado", "disponible"));
        } catch (org.springframework.dao.DataAccessException e) {
            return ResponseEntity.status(503).body(Map.of("estado", "no disponible"));
        }
    }
}
