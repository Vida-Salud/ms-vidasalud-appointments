package cl.duoc.ms_vidasalud_appointments.dto;

import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;

import java.time.LocalDateTime;

/**
 * Representación de una atención hacia el exterior.
 *
 * Existe para desacoplar el contrato de la API del esquema de la base: se puede
 * renombrar una columna en Oracle sin romper a los consumidores del endpoint.
 *
 * estado se declara como enum, no como String: Jackson lo serializa igual
 * ("SOLICITADA") pero el compilador impide construir una respuesta con un valor
 * que no existe en el dominio.
 */
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
