package cl.duoc.ms_vidasalud_appointments.dto;

import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import jakarta.validation.constraints.NotNull;


public record CambiarEstadoRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoAtencion nuevoEstado
) {
}
