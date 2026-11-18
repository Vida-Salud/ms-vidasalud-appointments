package cl.duoc.ms_vidasalud_appointments.messaging.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara el tópico de eventos de atenciones. {@code KafkaAdmin} lo crea al arrancar si no existe;
 * si ya existe, la declaración no tiene efecto. La configuración de referencia está en
 * infra/kafka/topics.conf (kafka-init la aplica al levantar el clúster) y los valores coinciden.
 * Con {@code vidasalud.kafka.habilitado=false} no se declara.
 */
@Configuration
@ConditionalOnProperty(name = "vidasalud.kafka.habilitado", havingValue = "true", matchIfMissing = true)
public class KafkaTopicosConfig {

    @Bean
    public NewTopic topicoEventosAtencion(@Value("${vidasalud.kafka.topico-eventos}") String topico) {
        return TopicBuilder.name(topico)
                // 3 particiones: hasta 3 consumidores por grupo en paralelo. El orden se garantiza
                // por atención porque la key es el atencionId.
                .partitions(3)
                // 3 réplicas: una copia en cada broker del clúster.
                .replicas(3)
                // Los eventos se borran por antigüedad, no se compactan por key: report y audit
                // necesitan el historial completo de cambios, no solo el último estado.
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                // 7 días de retención: margen para que un consumidor caído se recupere y reprocese.
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                // Con acks=all, una escritura se confirma cuando está en al menos 2 réplicas: se
                // tolera la caída de un broker sin perder mensajes ni dejar de aceptar escrituras.
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build();
    }
}
