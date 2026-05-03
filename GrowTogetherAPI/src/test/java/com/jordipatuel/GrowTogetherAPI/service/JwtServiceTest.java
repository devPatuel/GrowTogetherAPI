package com.jordipatuel.GrowTogetherAPI.service;

import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests del servicio JwtService aislado de Spring.
 * Inyectamos secretKey y jwtExpiration con ReflectionTestUtils porque
 * la clase usa @Value para leerlos del entorno.
 */
class JwtServiceTest {

    // Base64 que decodifica a 64 bytes (suficiente para HS256).
    private static final String SECRET =
            "M2NmYTc2ZWYxNDkzN2MxYzBlYTUxOWY4ZmMwNTdhODBmY2QwNGE3NDIwZjhlOGJjZDBhNzU2N2MyNzJlMDA3Yg==";

    private JwtService jwtService;

    private AuthUserDetails buildUser(int tokenVersion) {
        return new AuthUserDetails(
                7L,
                "jordi@example.com",
                "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_STANDARD")),
                tokenVersion
        );
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateToken_yExtractUsername_devuelveElEmailDelPrincipal() {
        String token = jwtService.generateToken(buildUser(0));

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("jordi@example.com");
    }

    @Test
    void isTokenValid_devuelveTrueParaTokenReciénGenerado() {
        AuthUserDetails user = buildUser(3);
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
        assertThat(jwtService.extractTokenVersion(token)).isEqualTo(3);
    }

    @Test
    void isTokenValid_devuelveFalseSiElTokenVersionDelUsuarioCambia() {
        String token = jwtService.generateToken(buildUser(3));
        AuthUserDetails usuarioConVersionDistinta = buildUser(4);

        assertThat(jwtService.isTokenValid(token, usuarioConVersionDistinta)).isFalse();
    }

    @Test
    void isTokenValid_lanzaExpiredJwtExceptionConTokenCaducado() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String tokenCaducado = jwtService.generateToken(buildUser(0));

        assertThatThrownBy(() -> jwtService.isTokenValid(tokenCaducado, buildUser(0)))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
