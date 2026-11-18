package cl.duoc.ms_vidasalud_appointments.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsuarioActual {

    private static final String PREFIJO_ROL = "APPROLE_";
    private static final Set<String> ROLES_PERSONAL = Set.of("APPROLE_Admin", "APPROLE_Operador");

    /** Si el token trae varios roles, se informa el de mayor privilegio. */
    private static final List<String> ROLES_POR_PRIORIDAD = List.of("Admin", "Operador", "Auditor", "Cliente");

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
        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean personal = authorities.stream().anyMatch(ROLES_PERSONAL::contains);
        return new Usuario(oid.toString(), nombre != null ? nombre.toString() : oid.toString(), personal,
                rolPrincipal(authorities));
    }

    /** null si el token no trae ninguno de los roles conocidos. */
    private static String rolPrincipal(Set<String> authorities) {
        return ROLES_POR_PRIORIDAD.stream()
                .filter(rol -> authorities.contains(PREFIJO_ROL + rol))
                .findFirst()
                .orElse(null);
    }
}
