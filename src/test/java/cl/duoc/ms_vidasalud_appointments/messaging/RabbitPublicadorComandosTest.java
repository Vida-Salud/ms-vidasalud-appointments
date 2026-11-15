package cl.duoc.ms_vidasalud_appointments.messaging;

import cl.duoc.ms_vidasalud_appointments.config.TraceIdFilter;
import cl.duoc.ms_vidasalud_appointments.messaging.dto.MensajeComando;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitPublicadorComandosTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneId.of("America/Santiago"));

    private final RabbitTemplate template = mock(RabbitTemplate.class);
    private final RabbitPublicadorComandos publicador = new RabbitPublicadorComandos(template, RELOJ);

    @Test
    void elEnvelopeLlevaVersionTraceIdYEventIdComoCorrelacion() {
        MDC.put(TraceIdFilter.MDC_KEY, "abc-123");
        try {
            publicador.publicar("cmd.topic", "email.confirmada", new AtencionEstadoCambiadoEvent(
                    42L, "Ana", "Dental", "Box 3", LocalDateTime.of(2026, 11, 20, 10, 30),
                    EstadoAtencion.CONFIRMADA));
        } finally {
            MDC.remove(TraceIdFilter.MDC_KEY);
        }

        ArgumentCaptor<MensajeComando> captor = ArgumentCaptor.forClass(MensajeComando.class);
        ArgumentCaptor<CorrelationData> correlacion = ArgumentCaptor.forClass(CorrelationData.class);
        verify(template).convertAndSend(eq("cmd.topic"), eq("email.confirmada"), captor.capture(), correlacion.capture());

        MensajeComando mensaje = captor.getValue();
        assertThat(mensaje.version()).isEqualTo(1);
        assertThat(mensaje.traceId()).isEqualTo("abc-123");
        assertThat(mensaje.type()).isEqualTo("email.confirmada");
        assertThat(mensaje.correlationId()).isEqualTo("atencion-42");
        assertThat(mensaje.timestamp()).isEqualTo(Instant.parse("2026-11-10T12:00:00Z"));
        assertThat(correlacion.getValue().getId()).isEqualTo(mensaje.eventId());
    }
}
