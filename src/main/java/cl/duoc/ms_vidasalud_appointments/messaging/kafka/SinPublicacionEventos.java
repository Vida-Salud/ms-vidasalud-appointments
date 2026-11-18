package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Reemplaza a {@link KafkaPublicadorEventos} con {@code vidasalud.kafka.habilitado=false}
 * (despliegues sin clúster Kafka): appointments funciona igual, solo que no publica eventos.
 */
@Component
@ConditionalOnProperty(name = "vidasalud.kafka.habilitado", havingValue = "false")
public class SinPublicacionEventos implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(SinPublicacionEventos.class);

    @Override
    public void publicar(AtencionEstadoCambiadoEvent evento) {
        log.debug("Kafka deshabilitado: no se publica el evento de la atención {}", evento.atencionId());
    }
}
