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
 * <p>
 * La confianza en {@code X-Forwarded-For} viene del BFF: no reenvía la cabecera del cliente, sino que la
 * reemplaza por un único valor ya resuelto por él ({@code vidasalud.proxies-de-confianza}). El flujo del caso
 * es siempre API Gateway -> BFF -> appointments. Por eso appointments no debe quedar expuesto fuera de la red
 * interna: quien le hable directo puede poner en la cabecera la IP que quiera.
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

    /**
     * El BFF manda en X-Forwarded-For un único valor: la IP del cliente que él ya resolvió. Si no viene o no es
     * una IP válida, la IP de la conexión. (Si llegara una lista, se toma su primer valor.)
     */
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
