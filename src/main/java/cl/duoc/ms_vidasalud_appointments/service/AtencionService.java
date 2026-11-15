package cl.duoc.ms_vidasalud_appointments.service;

import cl.duoc.ms_vidasalud_appointments.dto.ActualizarAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.dto.AtencionResponse;
import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.dto.CrearAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import cl.duoc.ms_vidasalud_appointments.security.Usuario;

import java.time.LocalDate;
import java.util.List;

public interface AtencionService {
    AtencionResponse crear(CrearAtencionRequest request, Usuario usuario);

    List<AtencionResponse> listar(Usuario usuario, EstadoAtencion estado, LocalDate desde, LocalDate hasta);

    AtencionResponse buscarPorId(Long id, Usuario usuario);

    AtencionResponse actualizar(Long id, ActualizarAtencionRequest request);

    AtencionResponse cambiarEstado(Long id, CambiarEstadoRequest request);
}
