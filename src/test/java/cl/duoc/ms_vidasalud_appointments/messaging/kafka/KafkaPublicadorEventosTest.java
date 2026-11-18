package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.config.TraceIdFilter;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.ActorEvento;
import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.EventoAtencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaPublicadorEventosTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneId.of("America/Santiago"));
    private static final String TOPICO = "appointments.events";

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, EventoAtencion> template = mock(KafkaTemplate.class);
    private final KafkaPublicadorEventos publicador = new KafkaPublicadorEventos(template, RELOJ, TOPICO);

    @Test
    void publicaEnElTopicoConElAtencionIdComoKeyYElEnvelopeCompleto() {
        when(template.send(anyString(), anyString(), any())).thenReturn(new CompletableFuture<>());

        MDC.put(TraceIdFilter.MDC_KEY, "abc-123");
        try {
            publicador.publicar(evento(EstadoAtencion.CONFIRMADA, EstadoAtencion.SOLICITADA));
        } finally {
            MDC.remove(TraceIdFilter.MDC_KEY);
        }

        EventoAtencion mensaje = mensajeEnviado();
        assertThat(mensaje.version()).isEqualTo(EventoAtencion.VERSION_ACTUAL);
        assertThat(mensaje.eventId()).isNotBlank();
        assertThat(mensaje.type()).isEqualTo("APPOINTMENT_STATUS_CHANGED");
        assertThat(mensaje.timestamp()).isEqualTo(Instant.parse("2026-11-10T12:00:00Z"));
        assertThat(mensaje.correlationId()).isEqualTo("atencion-42");
        assertThat(mensaje.traceId()).isEqualTo("abc-123");
        assertThat(mensaje.payload().atencionId()).isEqualTo(42L);
        assertThat(mensaje.payload().paciente()).isEqualTo("Ana");
        assertThat(mensaje.payload().estado()).isEqualTo("CONFIRMADA");
        assertThat(mensaje.payload().estadoAnterior()).isEqualTo("SOLICITADA");
        assertThat(mensaje.payload().actor()).isEqualTo(new ActorEvento("oid-op", "Olga Operadora", "Operador"));
    }

    @Test
    void sinEstadoAnteriorElTypeEsAppointmentCreated() {
        when(template.send(anyString(), anyString(), any())).thenReturn(new CompletableFuture<>());

        publicador.publicar(evento(EstadoAtencion.SOLICITADA, null));

        EventoAtencion mensaje = mensajeEnviado();
        assertThat(mensaje.type()).isEqualTo("APPOINTMENT_CREATED");
        assertThat(mensaje.payload().estado()).isEqualTo("SOLICITADA");
        assertThat(mensaje.payload().estadoAnterior()).isNull();
        assertThat(mensaje.payload().actor().id()).isEqualTo("oid-op");
    }

    @Test
    void unaKafkaExceptionDeSpringNoSePropaga() {
        when(template.send(anyString(), anyString(), any())).thenThrow(new KafkaException("Send failed"));

        assertThatCode(() -> publicador.publicar(evento(EstadoAtencion.CONFIRMADA, EstadoAtencion.SOLICITADA)))
                .doesNotThrowAnyException();
    }

    @Test
    void unaKafkaExceptionDelClienteNoSePropaga() {
        when(template.send(anyString(), anyString(), any()))
                .thenThrow(new org.apache.kafka.common.KafkaException("Failed to construct kafka producer"));

        assertThatCode(() -> publicador.publicar(evento(EstadoAtencion.CONFIRMADA, EstadoAtencion.SOLICITADA)))
                .doesNotThrowAnyException();
    }

    @Test
    void unEnvioQueFallaEnElCallbackNoSePropaga() {
        when(template.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new KafkaException("broker caído")));

        assertThatCode(() -> publicador.publicar(evento(EstadoAtencion.CONFIRMADA, EstadoAtencion.SOLICITADA)))
                .doesNotThrowAnyException();
    }

    private EventoAtencion mensajeEnviado() {
        ArgumentCaptor<EventoAtencion> captor = ArgumentCaptor.forClass(EventoAtencion.class);
        verify(template).send(eq(TOPICO), eq("42"), captor.capture());
        return captor.getValue();
    }

    private static AtencionEstadoCambiadoEvent evento(EstadoAtencion estado, EstadoAtencion estadoAnterior) {
        return new AtencionEstadoCambiadoEvent(42L, "Ana", "Dental", "Box 3",
                LocalDateTime.of(2026, 11, 20, 10, 30), estado, estadoAnterior,
                "oid-op", "Olga Operadora", "Operador");
    }
}
