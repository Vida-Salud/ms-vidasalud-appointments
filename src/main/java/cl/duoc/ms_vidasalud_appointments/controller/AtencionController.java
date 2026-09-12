package cl.duoc.ms_vidasalud_appointments.controller;

import cl.duoc.ms_vidasalud_appointments.dto.ActualizarAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.dto.AtencionResponse;
import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.dto.CrearAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.service.AtencionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST de atenciones médicas.
 *
 * No contiene lógica de negocio: solo traduce HTTP a llamadas al service y
 * elige el status code. Las excepciones que el service lanza las traduce el
 * ManejadorErrores, así que aquí no hay try/catch.
 *
 * No existe endpoint DELETE a propósito: en este sistema no hay borrado físico.
 * Anular una atención es transicionarla a CANCELADA (paso e).
 */
@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    @Autowired
    private AtencionService atencionService;

    /**
     * Agenda una atención.
     *
     * Devuelve 201 Created (no 200) con la cabecera Location apuntando al
     * recurso nuevo, que es la convención REST para creaciones.
     *
     * @Valid es lo que activa las anotaciones del DTO; sin él, los @NotBlank
     * quedan decorativos y nunca se evalúan.
     */
    @PostMapping
    public ResponseEntity<AtencionResponse> crear(
            @Valid @RequestBody CrearAtencionRequest request) {

        AtencionResponse creada = atencionService.crear(request);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();

        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** Lista todas las atenciones. Pendiente paginar (ver nota en el service). */
    @GetMapping
    public ResponseEntity<List<AtencionResponse>> listar() {
        return ResponseEntity.ok(atencionService.listar());
    }

    /** Obtiene una atención por id. Responde 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<AtencionResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(atencionService.buscarPorId(id));
    }

    /** Modifica servicio, box y fechaHora. Responde 404 si el id no existe. */
    @PutMapping("/{id}")
    public ResponseEntity<AtencionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarAtencionRequest request) {

        return ResponseEntity.ok(atencionService.actualizar(id, request));
    }

    /**
     * Mueve la atención a otro estado según el grafo de transiciones.
     *
     * Es un endpoint aparte del PUT general porque cambiar el estado es una
     * operación de negocio con sus propias reglas, no una edición de datos.
     * Responde 404 si el id no existe y 400 si la transición no está permitida.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<AtencionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        return ResponseEntity.ok(atencionService.cambiarEstado(id, request));
    }
}
