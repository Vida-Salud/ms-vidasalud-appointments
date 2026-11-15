package cl.duoc.ms_vidasalud_appointments.repository;

import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {
    @Query("""
            select a from Atencion a
            where (:pacienteId is null or a.pacienteId = :pacienteId)
              and (:estado is null or a.estado = :estado)
              and (:desde is null or a.fechaHora >= :desde)
              and (:hasta is null or a.fechaHora < :hasta)
            order by a.fechaHora desc
            """)
    List<Atencion> buscar(@Param("pacienteId") String pacienteId,
                          @Param("estado") EstadoAtencion estado,
                          @Param("desde") LocalDateTime desde,
                          @Param("hasta") LocalDateTime hasta);

    boolean existsByBoxAndFechaHora(String box, LocalDateTime fechaHora);
}
