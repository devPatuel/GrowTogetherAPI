package com.jordipatuel.GrowTogetherAPI.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Implementación personalizada de {@link org.springframework.security.core.userdetails.UserDetails}
 * que extiende {@link User} de Spring Security.
 *
 * Añade dos campos que el {@link User} estándar no tiene:
 * - {@code id}: el ID de base de datos del usuario, necesario en los controllers para
 *   comparar con el ID de la petición sin consultar la BD.
 * - {@code tokenVersion}: la versión del token JWT, necesaria en {@link JwtAuthenticationFilter}
 *   para invalidar tokens cuando el usuario cambia su contraseña o es desactivado.
 */
public class AuthUserDetails extends User {

    private final Long id;
    private final int tokenVersion;

    public AuthUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, authorities);
        this.id = id;
        this.tokenVersion = tokenVersion;
    }

    /** ID del usuario en base de datos. */
    public Long getId() {
        return id;
    }

    /** Versión del token JWT para detectar tokens revocados. */
    public int getTokenVersion() {
        return tokenVersion;
    }
}
