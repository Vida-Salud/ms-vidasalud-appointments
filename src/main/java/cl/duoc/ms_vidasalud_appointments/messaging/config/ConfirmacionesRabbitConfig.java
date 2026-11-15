package cl.duoc.ms_vidasalud_appointments.messaging.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfirmacionesRabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(ConfirmacionesRabbitConfig.class);

    @Bean
    RabbitTemplateCustomizer confirmacionesYDevoluciones() {
        return template -> {
            template.setConfirmCallback((correlacion, ack, causa) -> {
                if (!ack) {
                    log.error("RabbitMQ no confirmó el mensaje [eventId={}, causa={}]",
                            correlacion != null ? correlacion.getId() : "?", causa);
                }
            });
            template.setReturnsCallback(devuelto -> log.error(
                    "Mensaje sin cola de destino [exchange={}, routingKey={}, motivo={}]",
                    devuelto.getExchange(), devuelto.getRoutingKey(), devuelto.getReplyText()));
        };
    }
}
