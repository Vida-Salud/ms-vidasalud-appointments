package cl.duoc.ms_vidasalud_appointments.messaging;

import cl.duoc.ms_vidasalud_appointments.messaging.config.RabbitMQConfig;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NotificacionListenerTest {

    @Mock
    PublicadorComandos publicador;

    @InjectMocks
    NotificacionListener listener;

    @Test
    void confirmadaPublicaElCorreoPorElTopic() {
        var evento = evento(EstadoAtencion.CONFIRMADA);
        listener.alCambiarEstado(evento);
        verify(publicador).publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                RabbitMQConfig.EMAIL_CONFIRMADA_ROUTING_KEY, evento);
        verifyNoMoreInteractions(publicador);
    }

    @Test
    void enEsperaPublicaTicketYLlamadoDeBox() {
        var evento = evento(EstadoAtencion.EN_ESPERA);
        listener.alCambiarEstado(evento);
        verify(publicador).publicar(RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.ADMISSION_TICKET_ROUTING_KEY, evento);
        verify(publicador).publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                RabbitMQConfig.EMAIL_LLAMADO_BOX_ROUTING_KEY, evento);
        verifyNoMoreInteractions(publicador);
    }

    @Test
    void cerradaPublicaLaFichaYElCorreoDeCierre() {
        var evento = evento(EstadoAtencion.CERRADA);
        listener.alCambiarEstado(evento);
        verify(publicador).publicar(RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.RECORD_GEN_ROUTING_KEY, evento);
        verify(publicador).publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                RabbitMQConfig.EMAIL_CIERRE_ROUTING_KEY, evento);
        verifyNoMoreInteractions(publicador);
    }

    @Test
    void canceladaPublicaUnSoloMensajeEnElFanout() {
        var evento = evento(EstadoAtencion.CANCELADA);
        listener.alCambiarEstado(evento);
        verify(publicador).publicar(RabbitMQConfig.FANOUT_EXCHANGE, RabbitMQConfig.FANOUT_SIN_ROUTING_KEY,
                RabbitMQConfig.TYPE_ATENCION_CANCELADA, evento);
        verifyNoMoreInteractions(publicador);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoAtencion.class, names = {"SOLICITADA", "EN_ATENCION"})
    void estadosSinComandoNoPublican(EstadoAtencion estado) {
        listener.alCambiarEstado(evento(estado));
        verifyNoInteractions(publicador);
    }

    private static AtencionEstadoCambiadoEvent evento(EstadoAtencion estado) {
        return new AtencionEstadoCambiadoEvent(42L, "Ana", "Dental", "Box 3",
                LocalDateTime.of(2026, 11, 20, 10, 30), estado);
    }
}
