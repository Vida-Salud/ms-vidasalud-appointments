package cl.duoc.ms_vidasalud_appointments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiV1Test {

    @Autowired
    MockMvc mockMvc;

    @Test
    void versionNoSoportadaResponde400() throws Exception {
        mockMvc.perform(get("/api/v9/appointments").with(operador()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laRutaSinVersionYaNoExiste() throws Exception {
        mockMvc.perform(get("/api/atenciones").with(operador()))
                .andExpect(status().isNotFound());
    }

    @Test
    void bodyVacioDevuelveErroresPorCampo() throws Exception {
        mockMvc.perform(post("/api/v1/appointments").with(operador())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.paciente").exists())
                .andExpect(jsonPath("$.errores.fechaHora").exists());
    }

    private static RequestPostProcessor operador() {
        return jwt().jwt(j -> j.claim("oid", "oid-op"))
                .authorities(new SimpleGrantedAuthority("APPROLE_Operador"));
    }
}
