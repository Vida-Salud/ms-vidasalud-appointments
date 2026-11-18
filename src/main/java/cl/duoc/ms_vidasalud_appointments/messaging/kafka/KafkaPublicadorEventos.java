package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.config.TraceIdFilter;
import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;
import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.ActorEvento;
import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.DatosEventoAtencion;
import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.EventoAtencion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Arma el envelope {@link EventoAtencion} y lo publica en el tópico de eventos de Kafka.
 * <p>
 * La key es el {@code atencionId}: todos los eventos de una misma atención caen en la misma
 * partición y los consumidores los leen en el orden en que ocurrieron.
 * <p>
 * Es tolerante a fallas, igual que {@code RabbitPublicadorComandos}: se invoca después del commit,
 * cuando el cambio de estado ya está guardado, así que un error de Kafka se registra en el log y no
 * se relanza. El envío es asíncrono; su resultado se registra en el callback.
 * <p>
 * Con {@code vidasalud.kafka.habilitado=false} lo reemplaza {@link SinPublicacionEventos}.
 */
@Component
@ConditionalOnProperty(name = "vidasalud.kafka.habilitado", havingValue = "true", matchIfMissing = true)
public class KafkaPublicadorEventos implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(KafkaPublicadorEventos.class);

    private final KafkaTemplate<String, EventoAtencion> kafkaTemplate;
    private final Clock clock;
    private final OrigenPeticion origenPeticion;
    private final String topico;

    public KafkaPublicadorEventos(KafkaTemplate<String, EventoAtencion> kafkaTemplate, Clock clock,
                                  OrigenPeticion origenPeticion,
                                  @Value("${vidasalud.kafka.topico-eventos}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
        this.origenPeticion = origenPeticion;
        this.topico = topico;
    }

    @Override
    public void publicar(AtencionEstadoCambiadoEvent evento) {
        // Sin estado anterior, la atención se acaba de crear.
        String type = evento.estadoAnterior() == null
                ? EventoAtencion.TYPE_CREADA
                : EventoAtencion.TYPE_ESTADO_CAMBIADO;
        EventoAtencion mensaje = new EventoAtencion(
                EventoAtencion.VERSION_ACTUAL,
                UUID.randomUUID().toString(),
                type,
                Instant.now(clock),
                "atencion-" + evento.atencionId(),
                MDC.get(TraceIdFilter.MDC_KEY),
                new DatosEventoAtencion(
                        evento.atencionId(),
                        evento.servicio(),
                        evento.box(),
                        evento.fechaHora(),
                        evento.estado().name(),
                        evento.estadoAnterior() != null ? evento.estadoAnterior().name() : null,
                        new ActorEvento(evento.actorId(), evento.actorNombre(), evento.actorRol()),
                        origenPeticion.actual()));
        String key = String.valueOf(evento.atencionId());

        try {
            kafkaTemplate.send(topico, key, mensaje)
                    .whenComplete((resultado, error) -> registrarResultado(resultado, error, mensaje));
        } catch (KafkaException | org.apache.kafka.common.KafkaException e) {
            // Spring envuelve en su KafkaException los envíos que fallan de inmediato (p. ej. sin
            // metadata tras max.block.ms); si no se puede crear el productor, llega la de Apache.
            log.error("No se pudo publicar el evento [topic={}, atencionId={}, eventId={}]",
                    topico, evento.atencionId(), mensaje.eventId(), e);
        }
    }

    private void registrarResultado(SendResult<String, EventoAtencion> resultado, Throwable error,
                                    EventoAtencion mensaje) {
        Long atencionId = mensaje.payload().atencionId();
        if (error != null) {
            log.error("Kafka no confirmó el evento [topic={}, atencionId={}, eventId={}]",
                    topico, atencionId, mensaje.eventId(), error);
            return;
        }
        var metadata = resultado.getRecordMetadata();
        log.info("Evento publicado [topic={}, partition={}, offset={}, eventId={}, atencionId={}]",
                metadata.topic(), metadata.partition(), metadata.offset(), mensaje.eventId(), atencionId);
    }
}
