package cl.duoc.ms_vidasalud_appointments.messaging;

import cl.duoc.ms_vidasalud_appointments.messaging.event.AtencionEstadoCambiadoEvent;

public interface PublicadorComandos {
    void publicar(String exchange, String routingKey, AtencionEstadoCambiadoEvent evento);

    void publicar(String exchange, String routingKey, String type, AtencionEstadoCambiadoEvent evento);
}
