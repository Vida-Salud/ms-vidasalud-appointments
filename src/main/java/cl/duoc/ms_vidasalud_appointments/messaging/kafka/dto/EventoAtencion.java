package cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto;

import cl.duoc.ms_vidasalud_appointments.messaging.dto.DatosAtencion;

import java.time.Instant;

/**
 * Envelope de los eventos que este servicio publica en Kafka. Comparte la forma de
 * {@code MensajeComando} para que los consumidores traten igual los dos canales, pero es un hecho
 * ocurrido (no un comando) y lo leen todos los interesados.
 */
public record EventoAtencion(
        int version,
        String eventId,
        String type,
        Instant timestamp,
        String correlationId,
        String traceId,
        DatosAtencion payload) {

    public static final int VERSION_ACTUAL = 1;

    public static final String TYPE_ESTADO_CAMBIADO = "APPOINTMENT_STATUS_CHANGED";
}
