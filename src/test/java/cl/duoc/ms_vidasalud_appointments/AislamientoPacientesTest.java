package cl.duoc.ms_vidasalud_appointments;

import cl.duoc.ms_vidasalud_appointments.model.Atencion;
import cl.duoc.ms_vidasalud_appointments.repository.AtencionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AislamientoPacientesTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AtencionRepository repository;

    @BeforeEach
    void datos() {
        repository.deleteAll();
        repository.save(atencionDe("oid-ana", "Ana"));
        repository.save(atencionDe("oid-beto", "Beto"));
    }

    @Test
    void clienteSoloVeSusAtenciones() throws Exception {
        mockMvc.perform(get("/api/atenciones").with(cliente("oid-ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].paciente").value("Ana"));
    }

    @Test
    void operadorVeTodas() throws Exception {
        mockMvc.perform(get("/api/atenciones")
                        .with(jwt().jwt(j -> j.claim("oid", "oid-op"))
                                .authorities(new SimpleGrantedAuthority("APPROLE_Operador"))))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void clienteNoCambiaEstados() throws Exception {
        mockMvc.perform(put("/api/atenciones/1/estado").with(cliente("oid-ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\": \"CONFIRMADA\"}"))
                .andExpect(status().isForbidden());
    }

    private static Atencion atencionDe(String oid, String nombre) {
        Atencion atencion = new Atencion(nombre, "Dental", null, LocalDateTime.now().plusDays(1));
        atencion.setPacienteId(oid);
        return atencion;
    }

    private static RequestPostProcessor cliente(String oid) {
        return jwt().jwt(j -> j.claim("oid", oid).claim("name", "Paciente de prueba"))
                .authorities(new SimpleGrantedAuthority("APPROLE_Cliente"));
    }
}
