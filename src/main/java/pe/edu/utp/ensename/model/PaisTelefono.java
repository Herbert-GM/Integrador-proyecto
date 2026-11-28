package pe.edu.utp.ensename.model;

public enum PaisTelefono {
    PE("Perú (+51)", "51", 9), CO("Colombia (+57)", "57", 10), CL("Chile (+56)", "56", 9),
    INTERNACIONAL("Otro país · número internacional completo", "", 0);

    private final String etiqueta;
    private final String prefijo;
    private final int longitud;
    PaisTelefono(String etiqueta, String prefijo, int longitud) {
        this.etiqueta=etiqueta; this.prefijo=prefijo; this.longitud=longitud;
    }
    public String getEtiqueta(){return etiqueta;}
    public String internacional(String numero) {
        if(numero==null || !numero.matches("[1-9][0-9]{7,14}") || (longitud>0 && numero.length()!=longitud))
            throw new IllegalArgumentException("Número no válido para el país elegido.");
        return prefijo+numero;
    }
    public record Seleccion(PaisTelefono pais, String numero) {}
    public static Seleccion separar(String numero) {
        if(numero==null || numero.isBlank())return new Seleccion(PE, "");
        for(var pais:values())if(pais.longitud>0 && numero.startsWith(pais.prefijo)
                && numero.length()==pais.prefijo.length()+pais.longitud)
            return new Seleccion(pais,numero.substring(pais.prefijo.length()));
        return new Seleccion(INTERNACIONAL,numero);
    }
}
