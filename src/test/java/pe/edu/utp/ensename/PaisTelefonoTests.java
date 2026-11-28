package pe.edu.utp.ensename;
import org.junit.jupiter.api.Test;
import pe.edu.utp.ensename.model.PaisTelefono;
import static org.assertj.core.api.Assertions.*;

class PaisTelefonoTests {
    @Test void agregaPrefijoYConservaNumeroGuardadoAlEditar() {
        assertThat(PaisTelefono.PE.internacional("900000001")).isEqualTo("51900000001");
        for(String numero:new String[]{"51900000001","573001234567","56912345678","34912345678"}) {
            var seleccion=PaisTelefono.separar(numero);
            assertThat(seleccion.pais().internacional(seleccion.numero())).isEqualTo(numero);
        }
        assertThat(PaisTelefono.separar(null).pais()).isEqualTo(PaisTelefono.PE);
    }
    @Test void rechazaPrefijoDuplicadoSignoMasYLongitudIncorrecta() {
        for(String numero:new String[]{"51900000001","+51900000001","9000","900 000001","000000000"})
            assertThatIllegalArgumentException().isThrownBy(()->PaisTelefono.PE.internacional(numero));
    }
}
