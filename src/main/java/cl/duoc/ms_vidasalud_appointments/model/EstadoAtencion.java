package cl.duoc.ms_vidasalud_appointments.model;

/**
 * Estados posibles de una atención médica.
 *
 * El orden de declaración NO define las transiciones válidas: eso se resuelve
 * en el service (paso e). Aquí solo se enumeran los valores del dominio.
 */
public enum EstadoAtencion {
    SOLICITADA,
    CONFIRMADA,
    EN_ESPERA,
    EN_ATENCION,
    CERRADA,
    CANCELADA
}
