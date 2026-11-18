package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica en Kafka cada cambio de estado de una atención, para ms-vidasalud-report y audit.
 * <p>
 * A diferencia de {@code NotificacionListener}, no filtra por estado: a report y audit les interesa
 * el historial completo. Escucha en {@code AFTER_COMMIT} por la misma razón: solo se publican estados
 * que ya están confirmados en Oracle.
 */
@Component
public class EventosAtencionListener {

    private final PublicadorEventos publicador;

    public EventosAtencionListener(PublicadorEventos publicador) {
        this.publicador = publicador;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarEstado(AtencionEstadoCambiadoEvent evento) {
        publicador.publicar(evento);
    }
}
