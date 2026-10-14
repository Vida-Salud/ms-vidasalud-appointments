package cl.duoc.ms_vidasalud_appointments.messaging.dto;

import java.time.Instant;

public record MensajeComando(
        String eventId,
        String type,
        Instant timestamp,
        String correlationId,
        DatosAtencion data) {
}
