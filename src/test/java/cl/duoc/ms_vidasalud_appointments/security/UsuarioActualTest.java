package cl.duoc.ms_vidasalud_appointments.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioActualTest {

    private final UsuarioActual usuarioActual = new UsuarioActual();

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(delimiter = ';', value = {
            "APPROLE_Cliente;                                    Cliente",
            "APPROLE_Auditor,APPROLE_Cliente;                    Auditor",
            "APPROLE_Cliente,APPROLE_Operador,APPROLE_Auditor;   Operador",
            "APPROLE_Operador,APPROLE_Admin;                     Admin",
            "SCOPE_access_as_user;                               ''"
    })
    void elRolEsElDeMayorPrioridad(String authorities, String rolEsperado) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none")
                .claim("oid", "oid-1").claim("name", "Ana").build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities)));

        Usuario usuario = usuarioActual.obtener();

        assertThat(usuario.rol()).isEqualTo(rolEsperado.isEmpty() ? null : rolEsperado);
    }
}
