package pe.edu.utp.ensename.repository;

import java.util.*;
import java.time.*;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.ensename.model.*;

@Repository
@Profile("sqlserver")
public class CuentaRepository implements OrientadorRepository {
    private final JdbcTemplate jdbc;
    public CuentaRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public record Cuenta(long id,String nombre,String correo,String hash,boolean habilitado) {}
    public Optional<Cuenta> cuenta(String correo) {
        return jdbc.query("SELECT usuario_id,nombre,correo,password_hash,habilitado FROM dbo.Usuario WHERE correo=?",(r,n)->new Cuenta(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getBoolean(5)),correo).stream().findFirst();
    }
    @Transactional
    public void registrar(String nombre,String correo,String hash) {
        jdbc.update("INSERT INTO dbo.Usuario(nombre,correo,password_hash) VALUES(?,?,?)",nombre,correo,hash);
        long id=cuenta(correo).orElseThrow().id();
        jdbc.update("INSERT INTO dbo.Orientador(usuario_id) VALUES(?)",id);
    }
    public List<EspecialidadDato> especialidades() {
        return jdbc.query("SELECT especialidad_id,nombre FROM dbo.Especialidad ORDER BY nombre",(r,n)->new EspecialidadDato(r.getInt(1),r.getString(2)));
    }
    public record EspecialidadDato(int id,String nombre) {}
    public Set<Integer> seleccionadas(long id) {
        return new HashSet<>(jdbc.queryForList("SELECT especialidad_id FROM dbo.OrientadorEspecialidad WHERE usuario_id=?",Integer.class,id));
    }
    public Optional<Orientador> buscarPorId(long id) {
        return jdbc.query("SELECT u.usuario_id,u.nombre,o.descripcion,o.whatsapp,u.habilitado,o.publicar FROM dbo.Usuario u JOIN dbo.Orientador o ON u.usuario_id=o.usuario_id WHERE u.usuario_id=?",(r,n)->new Orientador(r.getLong(1),r.getString(2),r.getString(3),categorias(r.getLong(1)),r.getString(4),r.getBoolean(5),r.getBoolean(6),suscripcion(r.getLong(1))),id).stream().findFirst();
    }
    private Set<Especialidad> categorias(long id) {
        Set<Especialidad> resultado=EnumSet.noneOf(Especialidad.class);
        var nombres=jdbc.queryForList("SELECT e.nombre FROM dbo.Especialidad e JOIN dbo.OrientadorEspecialidad oe ON e.especialidad_id=oe.especialidad_id WHERE oe.usuario_id=?",String.class,id);
        for(String nombre:nombres){String s=nombre.toLowerCase(Locale.ROOT);if(s.contains("java")||s.contains("program")||s.contains("html")||s.contains("poo"))resultado.add(Especialidad.PROGRAMACION);else if(s.contains("sql")||s.contains("datos"))resultado.add(Especialidad.BASES_DE_DATOS);else if(s.contains("matem")||s.contains("álgebra")||s.contains("algebra")||s.contains("cálculo"))resultado.add(Especialidad.MATEMATICAS);}
        return resultado;
    }
    public List<Orientador> buscarTodos() {
        List<Orientador> resultado=new ArrayList<>();
        for(Long id:jdbc.queryForList("SELECT usuario_id FROM dbo.Orientador",Long.class))buscarPorId(id).ifPresent(resultado::add);
        return resultado;
    }
    public Suscripcion suscripcion(long id) {
        return jdbc.query("SELECT inicio,fin FROM dbo.Suscripcion WHERE usuario_id=? ORDER BY inicio DESC,suscripcion_id DESC",(r,n)->new Suscripcion(r.getObject(1,OffsetDateTime.class).toInstant(),r.getObject(2,OffsetDateTime.class).toInstant()),id).stream().findFirst().orElse(null);
    }
    @Transactional
    public void guardar(long id,String descripcion,String telefono,Set<Integer> especialidades,boolean publicar) {
        jdbc.update("UPDATE dbo.Orientador SET descripcion=?,whatsapp=?,publicar=?,actualizado_en=CURRENT_TIMESTAMP WHERE usuario_id=?",descripcion,telefono,publicar,id);
        jdbc.update("DELETE FROM dbo.OrientadorEspecialidad WHERE usuario_id=?",id);
        for(int especialidad:especialidades)jdbc.update("INSERT INTO dbo.OrientadorEspecialidad(usuario_id,especialidad_id) VALUES(?,?)",id,especialidad);
    }
    @Transactional
    public Suscripcion activar(long id,Instant ahora) {
        ahora = ahora.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        // El bloqueo del propietario serializa activaciones simultáneas de la misma cuenta.
        jdbc.queryForObject("SELECT usuario_id FROM dbo.Orientador WITH (UPDLOCK,HOLDLOCK) WHERE usuario_id=?",Long.class,id);
        Suscripcion anterior=suscripcion(id), nueva=Suscripcion.activar(ahora,anterior);
        if(nueva!=anterior)jdbc.update("INSERT INTO dbo.Suscripcion(usuario_id,inicio,fin,es_simulada) VALUES(?,?,?,1)",id,nueva.inicio().atZone(Suscripcion.LIMA).toOffsetDateTime(),nueva.fin().atZone(Suscripcion.LIMA).toOffsetDateTime());
        return nueva;
    }
}
