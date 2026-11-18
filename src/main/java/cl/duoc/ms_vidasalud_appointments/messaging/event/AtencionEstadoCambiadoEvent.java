package cl.duoc.ms_vidasalud_appointments.messaging.event;

import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;

import java.time.LocalDateTime;

/**
 * Cambio de estado de una atención, incluida su creación (estadoAnterior = null, estado = SOLICITADA).
 * El actor es el usuario que hizo la acción, para que audit sepa quién solicitó, confirmó, atendió o cerró.
 */
public record AtencionEstadoCambiadoEvent(
        Long atencionId,
        String paciente,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        EstadoAtencion estado,
        EstadoAtencion estadoAnterior,
        String actorId,
        String actorNombre,
        String actorRol) {
}
