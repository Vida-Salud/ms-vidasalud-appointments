package cl.duoc.ms_vidasalud_appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos que envía el cliente para agendar una atención (POST).
 *
 * No expone id ni estado a propósito: el id lo genera Oracle y el estado inicial
 * lo fija el service en SOLICITADA. Si el cliente pudiera elegir el estado,
 * podría crear una atención ya CERRADA y saltarse la máquina de estados.
 *
 * Los @Size reflejan los length de la entidad. Así un valor demasiado largo se
 * rechaza con 400 aquí, en vez de llegar a Oracle y provocar un ORA-12899 (500).
 *
 * La validación de que fechaHora sea futura NO va aquí: es una regla de negocio
 * y vive en el service.
 */
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
