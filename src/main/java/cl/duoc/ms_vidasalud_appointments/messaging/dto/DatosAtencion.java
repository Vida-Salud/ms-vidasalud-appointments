package cl.duoc.ms_vidasalud_appointments.messaging.dto;

import java.time.LocalDateTime;

public record DatosAtencion(
        Long atencionId,
        String paciente,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        String estado) {
}
