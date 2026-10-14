package cl.duoc.ms_vidasalud_appointments.messaging.event;

import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;

import java.time.LocalDateTime;

public record AtencionEstadoCambiadoEvent(
        Long atencionId,
        String paciente,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        EstadoAtencion estado) {
}
