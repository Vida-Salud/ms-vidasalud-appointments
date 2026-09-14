package cl.duoc.ms_vidasalud_appointments.repository;

import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {
    List<Atencion> findByEstado(EstadoAtencion estado);
    List<Atencion> findByPacienteOrderByFechaHoraDesc(String paciente);
    List<Atencion> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
    boolean existsByBoxAndFechaHora(String box, LocalDateTime fechaHora);
}
