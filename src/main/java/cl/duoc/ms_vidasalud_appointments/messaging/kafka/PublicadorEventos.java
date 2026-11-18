package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;

public interface PublicadorEventos {
    void publicar(AtencionEstadoCambiadoEvent evento);
}
