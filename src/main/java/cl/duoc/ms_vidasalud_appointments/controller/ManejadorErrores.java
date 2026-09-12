package cl.duoc.ms_vidasalud_appointments.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Único hueco que ResponseStatusException no cubre: los fallos de @Valid.
 *
 * Por sí solo, Spring responde 400 con detail "Invalid request content." y nada
 * más: no dice qué campo falló ni por qué. Este handler agrega ese detalle.
 *
 * Se devuelve ProblemDetail (RFC 9457) para que la forma del JSON sea idéntica
 * a la de los errores que Spring genera automáticamente, de modo que el cliente
 * parsea todos los errores igual. Requiere spring.mvc.problemdetails.enabled=true
 * en application.yaml.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "La petición tiene campos inválidos");

        // Se reportan TODOS los campos que fallaron, no solo el primero,
        // para que el cliente corrija el formulario en un solo viaje.
        Map<String, String> errores = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
        problema.setProperty("errores", errores);

        return problema;
    }
}
