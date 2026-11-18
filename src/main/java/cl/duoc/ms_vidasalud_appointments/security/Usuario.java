package cl.duoc.ms_vidasalud_appointments.security;

/**
 * Quien hace la petición. personal = Admin u Operador, que atienden a todos los pacientes.
 * rol = el rol de mayor prioridad del token (Admin, Operador, Auditor o Cliente); viaja en los
 * eventos para que audit sepa con qué rol actuó cada usuario.
 */
public record Usuario(String id, String nombre, boolean personal, String rol) {
}
