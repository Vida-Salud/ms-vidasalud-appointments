package cl.duoc.ms_vidasalud_appointments.messaging;

import cl.duoc.ms_vidasalud_appointments.messaging.config.RabbitMQConfig;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Traduce cada cambio de estado de una atención en los comandos que debe ejecutar ms-vidasalud-notify.
 * <p>
 * Escucha en fase {@code AFTER_COMMIT}: solo se publica cuando el nuevo estado ya quedó confirmado
 * en Oracle. Si la transacción hace rollback no se envía nada, y notify nunca recibe un comando
 * sobre un estado que no existe en la base de datos.
 */
@Component
public class NotificacionListener {

    private final NotificacionPublisher publisher;

    public NotificacionListener(NotificacionPublisher publisher) {
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarEstado(AtencionEstadoCambiadoEvent evento) {
        switch (evento.estado()) {
            case CONFIRMADA -> publisher.publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                    RabbitMQConfig.EMAIL_CONFIRMADA_ROUTING_KEY, evento);
            case EN_ESPERA -> {
                publisher.publicar(RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.ADMISSION_TICKET_ROUTING_KEY, evento);
                publisher.publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                        RabbitMQConfig.EMAIL_LLAMADO_BOX_ROUTING_KEY, evento);
            }
            case CERRADA -> {
                publisher.publicar(RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.RECORD_GEN_ROUTING_KEY, evento);
                publisher.publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                        RabbitMQConfig.EMAIL_CIERRE_ROUTING_KEY, evento);
            }
            case CANCELADA -> publisher.publicar(RabbitMQConfig.TOPIC_EXCHANGE,
                    RabbitMQConfig.EMAIL_CANCELADA_ROUTING_KEY, evento);
            default -> {
                // SOLICITADA y EN_ATENCION no generan comandos.
            }
        }
    }
}
