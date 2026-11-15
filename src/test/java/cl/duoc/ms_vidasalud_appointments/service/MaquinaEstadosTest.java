package cl.duoc.ms_vidasalud_appointments.service;

import cl.duoc.ms_vidasalud_appointments.dto.AtencionResponse;
import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import cl.duoc.ms_vidasalud_appointments.repository.AtencionRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaquinaEstadosTest {

    /** Las 7 transiciones del caso; las otras 29 combinaciones se rechazan. */
    private static final Set<String> VALIDAS = Set.of(
            "SOLICITADA>CONFIRMADA", "SOLICITADA>CANCELADA",
            "CONFIRMADA>EN_ESPERA", "CONFIRMADA>CANCELADA",
            "EN_ESPERA>EN_ATENCION", "EN_ESPERA>CANCELADA",
            "EN_ATENCION>CERRADA");

    @Mock AtencionRepository atencionRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks AtencionService service;

    static Stream<Arguments> todosLosPares() {
        return Arrays.stream(EstadoAtencion.values()).flatMap(desde ->
                Arrays.stream(EstadoAtencion.values()).map(hacia -> Arguments.of(desde, hacia)));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("todosLosPares")
    void soloPasanLasTransicionesDelCaso(EstadoAtencion desde, EstadoAtencion hacia) {
        Atencion atencion = new Atencion("Ana", "Dental", "Box 1", LocalDateTime.now().plusDays(1));
        atencion.setEstado(desde);
        when(atencionRepository.findById(1L)).thenReturn(Optional.of(atencion));

        if (VALIDAS.contains(desde + ">" + hacia)) {
            when(atencionRepository.save(any(Atencion.class))).thenAnswer(inv -> inv.getArgument(0));
            AtencionResponse respuesta = service.cambiarEstado(1L, new CambiarEstadoRequest(hacia));
            assertThat(respuesta.estado()).isEqualTo(hacia);
            verify(eventPublisher).publishEvent(any(AtencionEstadoCambiadoEvent.class));
        } else {
            assertThatThrownBy(() -> service.cambiarEstado(1L, new CambiarEstadoRequest(hacia)))
                    .isInstanceOf(ResponseStatusException.class);
            verify(atencionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
