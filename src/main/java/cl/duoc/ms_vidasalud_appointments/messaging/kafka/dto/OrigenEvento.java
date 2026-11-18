package cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto;

/** Desde dónde: el servicio que emite el evento y la IP del cliente (informativa, no es un control de seguridad). */
public record OrigenEvento(String servicio, String ip) {
}
