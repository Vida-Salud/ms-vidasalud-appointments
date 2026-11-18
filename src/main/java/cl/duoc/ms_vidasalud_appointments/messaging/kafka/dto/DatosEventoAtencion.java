package cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto;

import java.time.LocalDateTime;

/**
 * Payload de los eventos Kafka. Separado de {@code DatosAtencion} (RabbitMQ) para que agregar
 * datos de auditoría no cambie el contrato que ya consume ms-vidasalud-notify.
 * <p>
 * No lleva el nombre del paciente: el tópico guarda los eventos 7 días y lo leen varios servicios
 * que no lo necesitan (minimización de datos personales). Quien necesite el detalle lo consulta en
 * appointments con el {@code atencionId}. estadoAnterior es null en APPOINTMENT_CREATED.
 */
public record DatosEventoAtencion(
        Long atencionId,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        String estado,
        String estadoAnterior,
        ActorEvento actor,
        OrigenEvento origen) {
}
