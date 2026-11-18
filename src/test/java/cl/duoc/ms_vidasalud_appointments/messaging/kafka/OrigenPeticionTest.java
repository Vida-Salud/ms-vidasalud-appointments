package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.OrigenEvento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class OrigenPeticionTest {

    private final OrigenPeticion origen = new OrigenPeticion("ms-vidasalud-appointments");

    @AfterEach
    void limpiar() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void usaElPrimerValorDeXForwardedFor() {
        peticion("10.0.0.5", "190.20.30.40, 10.0.0.1");

        assertThat(origen.actual()).isEqualTo(new OrigenEvento("ms-vidasalud-appointments", "190.20.30.40"));
    }

    @Test
    void sinCabeceraUsaLaIpDeLaConexion() {
        peticion("10.0.0.5", null);

        assertThat(origen.actual().ip()).isEqualTo("10.0.0.5");
    }

    @Test
    void unaCabeceraQueNoEsIpSeIgnora() {
        peticion("10.0.0.5", "<script>alert(1)</script>");

        assertThat(origen.actual().ip()).isEqualTo("10.0.0.5");
    }

    @Test
    void sinPeticionEnCursoLaIpEsNull() {
        assertThat(origen.actual()).isEqualTo(new OrigenEvento("ms-vidasalud-appointments", null));
    }

    private static void peticion(String ipConexion, String xForwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ipConexion);
        if (xForwardedFor != null) {
            request.addHeader("X-Forwarded-For", xForwardedFor);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
