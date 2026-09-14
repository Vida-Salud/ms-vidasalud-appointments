package cl.duoc.ms_vidasalud_appointments.service;

import cl.duoc.ms_vidasalud_appointments.dto.ActualizarAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.dto.AtencionResponse;
import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.dto.CrearAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import cl.duoc.ms_vidasalud_appointments.repository.AtencionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class AtencionService {

    @Autowired
    private AtencionRepository atencionRepository;

    private static final Map<EstadoAtencion, Set<EstadoAtencion>> TRANSICIONES_VALIDAS =
            new EnumMap<>(Map.of(
                    EstadoAtencion.SOLICITADA,  Set.of(EstadoAtencion.CONFIRMADA, EstadoAtencion.CANCELADA),
                    EstadoAtencion.CONFIRMADA,  Set.of(EstadoAtencion.EN_ESPERA, EstadoAtencion.CANCELADA),
                    EstadoAtencion.EN_ESPERA,   Set.of(EstadoAtencion.EN_ATENCION, EstadoAtencion.CANCELADA),
                    EstadoAtencion.EN_ATENCION, Set.of(EstadoAtencion.CERRADA),
                    EstadoAtencion.CERRADA,     Set.of(),
                    EstadoAtencion.CANCELADA,   Set.of()
            ));


    private static final Set<EstadoAtencion> ESTADOS_EDITABLES = EnumSet.of(
            EstadoAtencion.SOLICITADA,
            EstadoAtencion.CONFIRMADA,
            EstadoAtencion.EN_ESPERA
    );

    public AtencionResponse crear(CrearAtencionRequest request) {
        validarFechaFutura(request.fechaHora());
        Atencion atencion = toEntity(request);
        return toResponse(atencionRepository.save(atencion));
    }

    public AtencionResponse buscarPorId(Long id) {
        return toResponse(buscarEntidad(id));
    }


    public List<AtencionResponse> listar() {
        return atencionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AtencionResponse actualizar(Long id, ActualizarAtencionRequest request) {
        Atencion atencion = buscarEntidad(id);
        validarEditable(atencion);
        validarFechaFutura(request.fechaHora());

        atencion.setServicio(normalizar(request.servicio()));
        atencion.setBox(normalizar(request.box()));
        atencion.setFechaHora(request.fechaHora());

        return toResponse(atencionRepository.save(atencion));
    }

    public AtencionResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        Atencion atencion = buscarEntidad(id);
        validarTransicion(atencion.getEstado(), request.nuevoEstado());

        atencion.setEstado(request.nuevoEstado());
        return toResponse(atencionRepository.save(atencion));
    }

    private Atencion buscarEntidad(Long id) {
        return atencionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe una atención con id " + id));
    }


    private void validarTransicion(EstadoAtencion actual, EstadoAtencion nuevo) {
        Set<EstadoAtencion> permitidos = TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());

        if (!permitidos.contains(nuevo)) {
            String detalle = permitidos.isEmpty()
                    ? actual + " es un estado final y no admite cambios"
                    : "Los estados permitidos desde " + actual + " son: " + permitidos;

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transición inválida de " + actual + " a " + nuevo + ". " + detalle);
        }
    }

    private void validarEditable(Atencion atencion) {
        if (!ESTADOS_EDITABLES.contains(atencion.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar una atención en estado " + atencion.getEstado()
                            + ". Solo se permite en: " + ESTADOS_EDITABLES);
        }
    }

    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha y hora de la atención debe ser futura");
        }
    }

    private Atencion toEntity(CrearAtencionRequest request) {
        return new Atencion(
                normalizar(request.paciente()),
                normalizar(request.servicio()),
                normalizar(request.box()),
                request.fechaHora()
        );
    }

    private AtencionResponse toResponse(Atencion atencion) {
        return new AtencionResponse(
                atencion.getId(),
                atencion.getPaciente(),
                atencion.getServicio(),
                atencion.getBox(),
                atencion.getFechaHora(),
                atencion.getEstado(),
                atencion.getFechaCreacion()
        );
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
