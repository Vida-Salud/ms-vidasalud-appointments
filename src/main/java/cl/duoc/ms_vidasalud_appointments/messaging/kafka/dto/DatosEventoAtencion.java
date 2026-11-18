package cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto;

import java.time.LocalDateTime;

/**
 * Payload de los eventos Kafka. Separado de {@code DatosAtencion} (RabbitMQ) para que agregar
 * datos de auditoría no cambie el contrato que ya consume ms-vidasalud-notify.
 * estadoAnterior es null en APPOINTMENT_CREATED.
 */
public record DatosEventoAtencion(
        Long atencionId,
        String paciente,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        String estado,
        String estadoAnterior,
        ActorEvento actor) {
}
