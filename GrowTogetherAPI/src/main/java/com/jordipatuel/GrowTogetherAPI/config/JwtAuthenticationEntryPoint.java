package com.jordipatuel.GrowTogetherAPI.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordipatuel.GrowTogetherAPI.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * EntryPoint que se invoca cuando una request anónima accede a un recurso protegido.
 *
 * Por defecto Spring Security 6 devuelve 403 (Forbidden) en este caso, lo que confunde
 * a los clientes (web y app móvil) que distinguen entre 401 (sin sesión, hay que reloguear)
 * y 403 (sesión válida pero sin permisos suficientes).
 *
 * Este EntryPoint fuerza el 401 + JSON con {@link ErrorResponseDTO} para que el
 * AuthInterceptor del cliente pueda borrar el token y redirigir al login.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDTO body = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "No autenticado",
                System.currentTimeMillis(),
                authException.getMessage()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
