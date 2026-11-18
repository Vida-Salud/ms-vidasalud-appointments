package cl.duoc.ms_vidasalud_appointments.controller;

import cl.duoc.ms_vidasalud_appointments.dto.ActualizarAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.dto.AtencionResponse;
import cl.duoc.ms_vidasalud_appointments.dto.CambiarEstadoRequest;
import cl.duoc.ms_vidasalud_appointments.dto.CrearAtencionRequest;
import cl.duoc.ms_vidasalud_appointments.model.EstadoAtencion;
import cl.duoc.ms_vidasalud_appointments.security.UsuarioActual;
import cl.duoc.ms_vidasalud_appointments.service.AtencionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/{version}/appointments")
@PreAuthorize("hasAnyAuthority('APPROLE_Admin', 'APPROLE_Operador', 'APPROLE_Cliente')")
public class AtencionController {

    private final AtencionService atencionService;
    private final UsuarioActual usuarioActual;

    public AtencionController(AtencionService atencionService, UsuarioActual usuarioActual) {
        this.atencionService = atencionService;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping
    public ResponseEntity<AtencionResponse> crear(
            @Valid @RequestBody CrearAtencionRequest request) {

        AtencionResponse creada = atencionService.crear(request, usuarioActual.obtener());

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();

        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<AtencionResponse>> listar(
            @RequestParam(required = false) EstadoAtencion status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(atencionService.listar(usuarioActual.obtener(), status, from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtencionResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(atencionService.buscarPorId(id, usuarioActual.obtener()));
    }

    @PreAuthorize("hasAnyAuthority('APPROLE_Admin', 'APPROLE_Operador')")
    @PutMapping("/{id}")
    public ResponseEntity<AtencionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarAtencionRequest request) {

        return ResponseEntity.ok(atencionService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyAuthority('APPROLE_Admin', 'APPROLE_Operador')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AtencionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        return ResponseEntity.ok(atencionService.cambiarEstado(id, request, usuarioActual.obtener()));
    }
}
