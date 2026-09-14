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


@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    @Autowired
    private AtencionService atencionService;

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

    @GetMapping
    public ResponseEntity<List<AtencionResponse>> listar() {
        return ResponseEntity.ok(atencionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtencionResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(atencionService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtencionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarAtencionRequest request) {

        return ResponseEntity.ok(atencionService.actualizar(id, request));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<AtencionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        return ResponseEntity.ok(atencionService.cambiarEstado(id, request));
    }
}
