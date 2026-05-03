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

    /** ID del usuario en base de datos. */
    private final Long id;
    /** Versión del token JWT activo cuando se emitió este UserDetails. */
    private final int tokenVersion;

    /**
     * Construye el UserDetails enriquecido a partir de los datos del usuario.
     *
     * @param id ID del usuario en base de datos
     * @param username email usado como nombre de usuario
     * @param password contraseña cifrada con BCrypt
     * @param authorities roles concedidos al usuario
     * @param tokenVersion versión actual del token JWT del usuario
     */
    public AuthUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, authorities);
        this.id = id;
        this.tokenVersion = tokenVersion;
    }

    /**
     * Devuelve el ID del usuario en base de datos.
     *
     * @return ID numérico del usuario
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve la versión del token JWT registrada al autenticar.
     *
     * @return versión del token (entero monotónico creciente)
     */
    public int getTokenVersion() {
        return tokenVersion;
    }
}
