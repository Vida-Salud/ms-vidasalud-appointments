package cl.duoc.ms_vidasalud_appointments.messaging;

import cl.duoc.ms_vidasalud_appointments.messaging.dto.DatosAtencion;
import cl.duoc.ms_vidasalud_appointments.messaging.dto.MensajeComando;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Arma el envelope {@link MensajeComando} que espera ms-vidasalud-notify y lo publica en RabbitMQ.
 * <p>
 * Es tolerante a fallas: se invoca después del commit, cuando el cambio de estado ya está guardado,
 * así que un error de RabbitMQ (broker caído, timeout de conexión) se registra en el log y no se
 * relanza. La petición HTTP responde con éxito igual; perder la notificación es preferible a
 * informar como fallido un cambio de estado que sí ocurrió.
 */
@Component
public class NotificacionPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificacionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(String exchange, String routingKey, AtencionEstadoCambiadoEvent evento) {
        publicar(exchange, routingKey, routingKey, evento);
    }

    /**
     * Variante con el {@code type} del envelope separado de la routing key. Se usa con exchanges
     * fanout, donde la routing key se ignora y el consumidor distingue el mensaje por su {@code type}.
     */
    public void publicar(String exchange, String routingKey, String type, AtencionEstadoCambiadoEvent evento) {
        MensajeComando mensaje = new MensajeComando(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                "atencion-" + evento.atencionId(),
                new DatosAtencion(
                        evento.atencionId(),
                        evento.paciente(),
                        evento.servicio(),
                        evento.box(),
                        evento.fechaHora(),
                        evento.estado().name()));

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);
            log.info("Mensaje publicado [exchange={}, routingKey={}, type={}, eventId={}, atencionId={}]",
                    exchange, routingKey, mensaje.type(), mensaje.eventId(), evento.atencionId());
        } catch (AmqpException e) {
            log.error("No se pudo publicar el mensaje [exchange={}, routingKey={}, atencionId={}]",
                    exchange, routingKey, evento.atencionId(), e);
        }
    }
}
