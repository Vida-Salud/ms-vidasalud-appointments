package cl.duoc.ms_vidasalud_appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;


public record CrearAtencionRequest(

        @NotBlank(message = "El paciente es obligatorio")
        @Size(max = 100, message = "El paciente no puede exceder 100 caracteres")
        String paciente,

        @NotBlank(message = "El servicio es obligatorio")
        @Size(max = 100, message = "El servicio no puede exceder 100 caracteres")
        String servicio,

        /** Opcional: al solicitar la atención puede no haber box asignado todavía. */
        @Size(max = 50, message = "El box no puede exceder 50 caracteres")
        String box,

        @NotNull(message = "La fecha y hora son obligatorias")
        LocalDateTime fechaHora
) {
}
