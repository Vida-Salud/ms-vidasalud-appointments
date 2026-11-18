package cl.duoc.ms_vidasalud_appointments.service;

import cl.duoc.ms_vidasalud_appointments.dto.ActualizarAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.dto.AtencionResponse;
import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.dto.CrearAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.exception.DatoInvalidoException;
import cl.duoc.ms_vidasalud_appointments.exception.RecursoNoEncontradoException;
import cl.duoc.ms_vidasalud_appointments.exception.ReglaNegocioException;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import cl.duoc.ms_vidasalud_appointments.repository.AtencionRepository;
import cl.duoc.ms_vidasalud_appointments.security.Usuario;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class AtencionServiceImpl implements AtencionService {

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

    private final AtencionRepository atencionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public AtencionServiceImpl(AtencionRepository atencionRepository,
                               ApplicationEventPublisher eventPublisher,
                               Clock clock) {
        this.atencionRepository = atencionRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * Transaccional para que el evento de creación llegue a los {@code @TransactionalEventListener}
     * (AFTER_COMMIT): sin transacción activa, Spring descarta el evento.
     */
    @Transactional
    @Override
    public AtencionResponse crear(CrearAtencionRequest request, Usuario usuario) {
        validarFechaFutura(request.fechaHora());
        Atencion atencion = toEntity(request);
        if (!usuario.personal()) {
            atencion.setPaciente(usuario.nombre());
            atencion.setPacienteId(usuario.id());
        }
        Atencion guardada = atencionRepository.save(atencion);

        publicarEvento(guardada, null, usuario);

        return toResponse(guardada);
    }

    @Override
    public AtencionResponse buscarPorId(Long id, Usuario usuario) {
        Atencion atencion = buscarEntidad(id);
        if (!usuario.personal() && !usuario.id().equals(atencion.getPacienteId())) {
            throw new RecursoNoEncontradoException("No existe una atención con id " + id);
        }
        return toResponse(atencion);
    }

    @Override
    public List<AtencionResponse> listar(Usuario usuario, EstadoAtencion estado,
                                         LocalDate desde, LocalDate hasta) {
        String pacienteId = usuario.personal() ? null : usuario.id();
        return atencionRepository.buscar(pacienteId, estado,
                        desde == null ? null : desde.atStartOfDay(),
                        hasta == null ? null : hasta.plusDays(1).atStartOfDay())   // "to" incluye ese día
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AtencionResponse actualizar(Long id, ActualizarAtencionRequest request) {
        Atencion atencion = buscarEntidad(id);
        validarEditable(atencion);
        validarFechaFutura(request.fechaHora());

        atencion.setServicio(normalizar(request.servicio()));
        atencion.setBox(normalizar(request.box()));
        atencion.setFechaHora(request.fechaHora());

        return toResponse(atencionRepository.save(atencion));
    }

    @Transactional
    @Override
    public AtencionResponse cambiarEstado(Long id, CambiarEstadoRequest request, Usuario usuario) {
        Atencion atencion = buscarEntidad(id);
        EstadoAtencion estadoAnterior = atencion.getEstado();
        validarTransicion(estadoAnterior, request.status());

        atencion.setEstado(request.status());
        Atencion guardada = atencionRepository.save(atencion);

        publicarEvento(guardada, estadoAnterior, usuario);

        return toResponse(guardada);
    }

    private void publicarEvento(Atencion atencion, EstadoAtencion estadoAnterior, Usuario actor) {
        eventPublisher.publishEvent(new AtencionEstadoCambiadoEvent(
                atencion.getId(),
                atencion.getPaciente(),
                atencion.getServicio(),
                atencion.getBox(),
                atencion.getFechaHora(),
                atencion.getEstado(),
                estadoAnterior,
                actor.id(),
                actor.nombre(),
                actor.rol()));
    }

    private Atencion buscarEntidad(Long id) {
        return atencionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una atención con id " + id));
    }


    private void validarTransicion(EstadoAtencion actual, EstadoAtencion nuevo) {
        Set<EstadoAtencion> permitidos = TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());

        if (!permitidos.contains(nuevo)) {
            String detalle = permitidos.isEmpty()
                    ? actual + " es un estado final y no admite cambios"
                    : "Los estados permitidos desde " + actual + " son: " + permitidos;

            throw new ReglaNegocioException(
                    "Transición inválida de " + actual + " a " + nuevo + ". " + detalle);
        }
    }

    private void validarEditable(Atencion atencion) {
        if (!ESTADOS_EDITABLES.contains(atencion.getEstado())) {
            throw new ReglaNegocioException(
                    "No se puede modificar una atención en estado " + atencion.getEstado()
                            + ". Solo se permite en: " + ESTADOS_EDITABLES);
        }
    }

    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now(clock))) {
            throw new DatoInvalidoException(
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
