package cl.duoc.ms_vidasalud_appointments.service;

import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.dto.CrearAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import cl.duoc.ms_vidasalud_appointments.repository.AtencionRepository;
import cl.duoc.ms_vidasalud_appointments.security.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Los eventos de dominio llevan quién hizo la acción y desde qué estado se partió. */
@ExtendWith(MockitoExtension.class)
class EventosAtencionServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"),
            ZoneId.of("America/Santiago"));
    private static final LocalDateTime MANANA = LocalDateTime.of(2026, 11, 11, 10, 0);

    @Mock AtencionRepository atencionRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    AtencionService service;

    @BeforeEach
    void crearServicio() {
        service = new AtencionServiceImpl(atencionRepository, eventPublisher, RELOJ);
    }

    @Test
    void crearPublicaUnEventoSolicitadaConElActor() {
        when(atencionRepository.save(any(Atencion.class))).thenAnswer(inv -> {
            Atencion atencion = inv.getArgument(0);
            ReflectionTestUtils.setField(atencion, "id", 7L);
            return atencion;
        });
        var cliente = new Usuario("oid-ana", "Ana Pérez", false, "Cliente");

        service.crear(new CrearAtencionRequest("Ana", "Dental", null, MANANA), cliente);

        AtencionEstadoCambiadoEvent evento = eventoPublicado();
        assertThat(evento.atencionId()).isEqualTo(7L);
        assertThat(evento.estado()).isEqualTo(EstadoAtencion.SOLICITADA);
        assertThat(evento.estadoAnterior()).isNull();
        assertThat(evento.actorId()).isEqualTo("oid-ana");
        assertThat(evento.actorNombre()).isEqualTo("Ana Pérez");
        assertThat(evento.actorRol()).isEqualTo("Cliente");
    }

    @Test
    void crearEsTransaccionalParaQueElEventoLlegueAlListenerAfterCommit() throws NoSuchMethodException {
        assertThat(AtencionServiceImpl.class.getMethod("crear", CrearAtencionRequest.class, Usuario.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void cambiarEstadoLlevaElEstadoAnteriorYElActor() {
        Atencion atencion = new Atencion("Ana", "Dental", "Box 1", MANANA);
        atencion.setEstado(EstadoAtencion.CONFIRMADA);
        when(atencionRepository.findById(1L)).thenReturn(Optional.of(atencion));
        when(atencionRepository.save(any(Atencion.class))).thenAnswer(inv -> inv.getArgument(0));
        var operador = new Usuario("oid-op", "Olga Operadora", true, "Operador");

        service.cambiarEstado(1L, new CambiarEstadoRequest(EstadoAtencion.EN_ESPERA), operador);

        AtencionEstadoCambiadoEvent evento = eventoPublicado();
        assertThat(evento.estado()).isEqualTo(EstadoAtencion.EN_ESPERA);
        assertThat(evento.estadoAnterior()).isEqualTo(EstadoAtencion.CONFIRMADA);
        assertThat(evento.actorId()).isEqualTo("oid-op");
        assertThat(evento.actorNombre()).isEqualTo("Olga Operadora");
        assertThat(evento.actorRol()).isEqualTo("Operador");
    }

    private AtencionEstadoCambiadoEvent eventoPublicado() {
        ArgumentCaptor<AtencionEstadoCambiadoEvent> captor = ArgumentCaptor.forClass(AtencionEstadoCambiadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }
}
