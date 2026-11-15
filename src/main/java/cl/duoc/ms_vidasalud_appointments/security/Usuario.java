package cl.duoc.ms_vidasalud_appointments.security;

/** Quien hace la petición. personal = Admin u Operador, que atienden a todos los pacientes. */
public record Usuario(String id, String nombre, boolean personal) {
}
