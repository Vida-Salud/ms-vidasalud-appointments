package cl.duoc.ms_vidasalud_appointments.repository;

import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de Atencion.
 *
 * No lleva @Repository: Spring Data detecta la interfaz por extender
 * JpaRepository y genera la implementación en tiempo de arranque.
 */
public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    /** Atenciones en un estado dado, p. ej. la cola de EN_ESPERA. */
    List<Atencion> findByEstado(EstadoAtencion estado);

    /** Historial de un paciente, de la atención más reciente a la más antigua. */
    List<Atencion> findByPacienteOrderByFechaHoraDesc(String paciente);

    /** Agenda de un rango de tiempo (Between en JPA es inclusivo en ambos extremos). */
    List<Atencion> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    /**
     * Sirve para detectar doble reserva de un box a la misma hora.
     * Se usará como validación en el service, no aquí.
     */
    boolean existsByBoxAndFechaHora(String box, LocalDateTime fechaHora);
}
