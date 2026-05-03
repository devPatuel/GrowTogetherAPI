package com.jordipatuel.GrowTogetherAPI.service;

import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de toda la lógica de tokens JWT.
 *
 * Genera tokens firmados con HMAC-SHA256 incluyendo el claim {@code tv} (tokenVersion)
 * para permitir la revocación. Valida tokens comprobando firma, expiración y tokenVersion.
 *
 * El secreto se inyecta desde la variable de entorno {@code JWT_SECRET} (obligatorio).
 * La expiración por defecto es 24h (86400000 ms), configurable via {@code jwt.expiration}.
 */
@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Extrae el email del usuario (campo {@code sub}) del token.
     *
     * @param token JWT firmado
     * @return email del usuario contenido en el token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el claim {@code tv} (tokenVersion) del token.
     *
     * @param token JWT firmado
     * @return versión del token, o null si no contiene el claim
     */
    public Integer extractTokenVersion(String token) {
        return extractClaim(token, claims -> claims.get("tv", Integer.class));
    }

    /**
     * Extrae cualquier claim del token aplicando la función indicada.
     *
     * @param <T> tipo de dato del claim
     * @param token JWT firmado
     * @param claimsResolver función que extrae el claim de los {@link Claims}
     * @return valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT a partir de un objeto {@link Authentication}.
     * Incluye el claim {@code tv} si el principal es un {@link AuthUserDetails}.
     *
     * @param authentication contexto de seguridad con el principal autenticado
     * @return JWT firmado listo para enviar al cliente
     */
    public String generateToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        if (authentication.getPrincipal() instanceof AuthUserDetails authUser) {
            claims.put("tv", authUser.getTokenVersion());
        }
        return buildToken(claims, authentication.getName(), jwtExpiration);
    }

    /**
     * Genera un token JWT a partir de un {@link UserDetails}.
     * Sobrecarga usada cuando no hay contexto de autenticación disponible.
     *
     * @param userDetails detalles del usuario para construir el token
     * @return JWT firmado
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof AuthUserDetails authUser) {
            claims.put("tv", authUser.getTokenVersion());
        }
        return buildToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Valida el token comprobando tres cosas:
     * que el email coincida con el usuario, que no haya expirado,
     * y que el {@code tv} del token coincida con el tokenVersion actual del usuario.
     *
     * @param token JWT a validar
     * @param userDetails detalles del usuario actual
     * @return true si el token es válido
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            return false;
        }
        if (userDetails instanceof AuthUserDetails authUser) {
            Integer tokenVersion = extractTokenVersion(token);
            if (tokenVersion != null && tokenVersion != authUser.getTokenVersion()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Comprueba si el token ha superado su fecha de expiración.
     *
     * @param token JWT a comprobar
     * @return true si el token ha expirado
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token JWT firmado
     * @return fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Construye y firma el token JWT con los claims, subject, fechas y clave HMAC.
     *
     * @param extraClaims claims adicionales a incluir (ej: tv)
     * @param username valor del subject (email del usuario)
     * @param expiration milisegundos hasta la expiración
     * @return JWT firmado
     */
    private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parsea y devuelve todos los claims del token verificando la firma.
     *
     * @param token JWT firmado
     * @return claims contenidos en el token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Decodifica el secreto en Base64 y devuelve la clave HMAC para firmar/verificar tokens.
     *
     * @return clave HMAC para firmar y verificar JWTs
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
