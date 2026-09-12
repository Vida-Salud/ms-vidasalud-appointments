package cl.duoc.ms_vidasalud_appointments.config;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadResourceServerHttpSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad: valida los JWT emitidos por Azure Entra ID.
 *
 * Este servicio actúa como Resource Server, no como cliente: no emite tokens ni
 * redirige a un login. Solo recibe un token en la cabecera Authorization,
 * verifica su firma y su vigencia, y deja pasar o rechaza.
 *
 * @EnableMethodSecurity activa la evaluación de @PreAuthorize en los métodos.
 * Sin ella, Spring ignora esas anotaciones en silencio y el endpoint queda
 * abierto a cualquier usuario autenticado.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF protege formularios con sesión y cookies. Una API REST sin
                // estado, autenticada por token en una cabecera, no es vulnerable
                // a ese ataque: el navegador no adjunta el token automáticamente.
                .csrf(AbstractHttpConfigurer::disable)

                // Todo endpoint exige autenticación. La distinción por rol se
                // hace en cada método con @PreAuthorize.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())

                // Configura el Resource Server al estilo Azure AD. Internamente
                // llama a oauth2ResourceServer().jwt() igual que antes, pero
                // además registra el conversor de Azure que transforma los claims
                // del token en autoridades de Spring:
                //   roles -> APPROLE_<rol>   (usado por @PreAuthorize)
                //   scp   -> SCOPE_<scope>
                // Con jwt(Customizer.withDefaults()) solo se mapeaba scp, y los
                // roles de Azure se perdían. El JwtDecoder (firma, emisor,
                // audiencia y expiración) sigue siendo el bean del starter de Azure.
                .with(AadResourceServerHttpSecurityConfigurer.aadResourceServer(),
                        Customizer.withDefaults())

                .build();
    }
}
