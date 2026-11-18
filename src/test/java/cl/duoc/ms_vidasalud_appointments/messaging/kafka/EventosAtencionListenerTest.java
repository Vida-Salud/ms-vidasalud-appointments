package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EventosAtencionListenerTest {

    @Mock
    PublicadorEventos publicador;

    @InjectMocks
    EventosAtencionListener listener;

    @ParameterizedTest
    @EnumSource(EstadoAtencion.class)
    void todosLosEstadosSePublican(EstadoAtencion estado) {
        var evento = new AtencionEstadoCambiadoEvent(42L, "Ana", "Dental", "Box 3",
                LocalDateTime.of(2026, 11, 20, 10, 30), estado);

        listener.alCambiarEstado(evento);

        verify(publicador).publicar(evento);
        verifyNoMoreInteractions(publicador);
    }
}
