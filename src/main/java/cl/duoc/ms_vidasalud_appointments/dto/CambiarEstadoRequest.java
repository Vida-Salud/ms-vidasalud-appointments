package cl.duoc.ms_vidasalud_appointments.dto;

import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para mover una atención a otro estado (PUT /api/atenciones/{id}/estado).
 *
 * Es el único camino para cambiar el estado: ni la creación ni el PUT general
 * lo permiten. Si se envía un valor que no existe en EstadoAtencion, Jackson no
 * puede convertirlo y Spring responde 400 antes de llegar al service.
 */
public record CambiarEstadoRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoAtencion nuevoEstado
) {
}
