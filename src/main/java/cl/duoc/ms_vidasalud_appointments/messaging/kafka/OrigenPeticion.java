package cl.duoc.ms_vidasalud_appointments.messaging.kafka;

import cl.duoc.ms_vidasalud_appointments.messaging.kafka.dto.OrigenEvento;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

/**
 * Responde "desde dónde" para el evento: el nombre de este servicio y la IP del cliente de la petición
 * HTTP en curso. El listener corre en AFTER_COMMIT dentro del mismo hilo de la petición, así que la
 * petición sigue disponible. Nunca lanza excepciones: sin petición, la IP es null.
 */
@Component
public class OrigenPeticion {

    private static final String CABECERA_IP = "X-Forwarded-For";
    /** IPv4 o IPv6. Un valor arbitrario de la cabecera terminaría escrito en los logs y en la auditoría. */
    private static final Pattern IP_VALIDA = Pattern.compile("[0-9a-fA-F:.]{2,45}");

    private final String servicio;

    public OrigenPeticion(@Value("${spring.application.name}") String servicio) {
        this.servicio = servicio;
    }

    public OrigenEvento actual() {
        return new OrigenEvento(servicio, ipDelCliente());
    }

    /** El BFF reenvía X-Forwarded-For; su primer valor es el cliente original. Si no viene, la IP de la conexión. */
    private static String ipDelCliente() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos)) {
            return null;
        }
        HttpServletRequest request = atributos.getRequest();
        String reenviada = request.getHeader(CABECERA_IP);
        if (reenviada != null) {
            String primera = reenviada.split(",")[0].trim();
            if (IP_VALIDA.matcher(primera).matches()) {
                return primera;
            }
        }
        return request.getRemoteAddr();
    }
}
