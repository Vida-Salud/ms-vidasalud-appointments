package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Con vidasalud.kafka.habilitado=false la aplicación arranca y no publica en Kafka. */
@SpringBootTest(properties = "vidasalud.kafka.habilitado=false")
@ActiveProfiles("test")
class KafkaDeshabilitadoTest {

    @Test
    void usaElPublicadorQueNoPublica(@Autowired ApplicationContext contexto) {
        assertThat(contexto.getBean(PublicadorEventos.class)).isInstanceOf(SinPublicacionEventos.class);
        assertThat(contexto.getBeansOfType(KafkaPublicadorEventos.class)).isEmpty();
    }
}
