package cl.duoc.ms_vidasalud_appointments.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class UsuarioActual {

    private static final Set<String> ROLES_PERSONAL = Set.of("APPROLE_Admin", "APPROLE_Operador");

    public Usuario obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AbstractOAuth2TokenAuthenticationToken<?> token)) {
            throw new AccessDeniedException("La petición no trae un token de Azure AD");
        }
        Map<String, Object> claims = token.getTokenAttributes();
        Object oid = claims.get("oid");
        if (oid == null) {
            throw new AccessDeniedException("El token no trae el claim oid"); // falla cerrado
        }
        Object nombre = claims.getOrDefault("name", claims.get("preferred_username"));
        boolean personal = auth.getAuthorities().stream()
                .anyMatch(a -> ROLES_PERSONAL.contains(a.getAuthority()));
        return new Usuario(oid.toString(), nombre != null ? nombre.toString() : oid.toString(), personal);
    }
}
