package cl.duoc.ms_vidasalud_appointments.dto;

import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;

import java.time.LocalDateTime;


public record AtencionResponse(
        Long id,
        String paciente,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        EstadoAtencion estado,
        LocalDateTime fechaCreacion
) {
}
