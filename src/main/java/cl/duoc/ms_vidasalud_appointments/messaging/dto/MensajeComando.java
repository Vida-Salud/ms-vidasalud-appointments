package cl.duoc.ms_vidasalud_appointments.messaging.dto;

import java.time.Instant;

public record MensajeComando(
        int version,
        String eventId,
        String type,
        Instant timestamp,
        String correlationId,
        String traceId,
        DatosAtencion data) {

    public static final int VERSION_ACTUAL = 1;
}
