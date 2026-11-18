package cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto;

/** Usuario que realizó la acción: id = claim oid de Azure AD, rol = Admin, Operador, Auditor o Cliente. */
public record ActorEvento(
        String id,
        String nombre,
        String rol) {
}
