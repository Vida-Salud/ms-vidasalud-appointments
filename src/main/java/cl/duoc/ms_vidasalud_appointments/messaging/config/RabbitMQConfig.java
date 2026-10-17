package cl.duoc.ms_vidasalud_appointments.messaging.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configuración del lado productor de RabbitMQ.
 * <p>
 * Solo declara los exchanges a los que publica este servicio ({@code cmd.direct}, {@code cmd.topic}
 * y {@code cmd.fanout}), con los mismos parámetros que ms-vidasalud-notify (durables, sin auto-delete)
 * para que la declaración sea idempotente. Las colas y bindings pertenecen a notify y no se declaran aquí.
 * <p>
 * El {@link MessageConverter} JSON usa el {@link JsonMapper} de Spring Boot, el mismo con el que
 * notify deserializa el envelope, así fechas e instantes viajan en el mismo formato en ambos lados.
 */
@Configuration
public class RabbitMQConfig {

    public static final String DIRECT_EXCHANGE = "cmd.direct";
    public static final String TOPIC_EXCHANGE = "cmd.topic";
    public static final String FANOUT_EXCHANGE = "cmd.fanout";

    public static final String ADMISSION_TICKET_ROUTING_KEY = "admission.ticket";
    public static final String RECORD_GEN_ROUTING_KEY = "record.gen";

    public static final String EMAIL_CONFIRMADA_ROUTING_KEY = "email.confirmada";
    public static final String EMAIL_LLAMADO_BOX_ROUTING_KEY = "email.llamado-box";
    public static final String EMAIL_CIERRE_ROUTING_KEY = "email.cierre";

    public static final String TYPE_ATENCION_CANCELADA = "atencion.cancelada";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
