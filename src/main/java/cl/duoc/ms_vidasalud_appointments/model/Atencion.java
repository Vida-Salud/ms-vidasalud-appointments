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


@Entity
@Table(name = "ATENCION")
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PACIENTE", nullable = false, length = 100)
    private String paciente;

    @Column(name = "SERVICIO", nullable = false, length = 100)
    private String servicio;

    @Column(name = "BOX", length = 50)
    private String box;

    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime fechaHora;


    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoAtencion estado;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    protected Atencion() {
    }

    public Atencion(String paciente, String servicio, String box, LocalDateTime fechaHora) {
        this.paciente = paciente;
        this.servicio = servicio;
        this.box = box;
        this.fechaHora = fechaHora;
        this.estado = EstadoAtencion.SOLICITADA;
    }


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
