package cl.duoc.ms_vidasalud_appointments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Atención médica agendada. Es la raíz del dominio de este microservicio.
 *
 * Los campos paciente, servicio y box son String por ahora: en el diseño final
 * serán referencias a ms-vidasalud-catalog, pero no se acopla todavía.
 */
@Entity
@Table(name = "ATENCION")
public class Atencion {

    /**
     * En Oracle 12c+ esto se traduce a "ID NUMBER(19,0) GENERATED AS IDENTITY".
     * Nota: con IDENTITY Hibernate no puede agrupar inserts en batch, porque
     * necesita leer el ID generado justo después de cada INSERT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PACIENTE", nullable = false, length = 100)
    private String paciente;

    @Column(name = "SERVICIO", nullable = false, length = 100)
    private String servicio;

    /** El box puede no estar asignado cuando la atención recién se solicita. */
    @Column(name = "BOX", length = 50)
    private String box;

    /** Fecha y hora en que se atenderá al paciente. Oracle: TIMESTAMP. */
    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * EnumType.STRING guarda el nombre del estado ("SOLICITADA") en lugar de su
     * posición numérica. Con ORDINAL, reordenar el enum corrompería los datos
     * existentes en silencio.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoAtencion estado;

    /** Auditoría: cuándo se creó el registro. La asigna @PrePersist, no el cliente. */
    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** JPA exige un constructor sin argumentos para poder instanciar por reflexión. */
    protected Atencion() {
    }

    public Atencion(String paciente, String servicio, String box, LocalDateTime fechaHora) {
        this.paciente = paciente;
        this.servicio = servicio;
        this.box = box;
        this.fechaHora = fechaHora;
        this.estado = EstadoAtencion.SOLICITADA;
    }

    /**
     * Se ejecuta justo antes del INSERT. Así la fecha de creación nunca depende
     * de que el service se acuerde de ponerla, ni de lo que mande el cliente.
     */
    @PrePersist
    private void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoAtencion.SOLICITADA;
        }
    }

    public Long getId() {
        return id;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoAtencion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAtencion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Igualdad por ID. No se usan los demás campos porque dos atenciones
     * distintas pueden coincidir en todo lo demás.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Atencion otra)) {
            return false;
        }
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Atencion{id=%d, paciente='%s', servicio='%s', estado=%s, fechaHora=%s}"
                .formatted(id, paciente, servicio, estado, fechaHora);
    }
}
