package com.jordipatuel.GrowTogetherAPI.exception;

import com.jordipatuel.GrowTogetherAPI.dto.ErrorResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del handler global. Como las excepciones se manejan método a método,
 * basta con instanciar la clase e invocar cada handler con la excepción correspondiente.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFound_devuelve404ConMensaje() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleNotFound(new ResourceNotFoundException("Hábito no existe"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Hábito no existe");
    }

    @Test
    void badRequest_devuelve400ConMensaje() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleBadRequest(new BadRequestException("Email duplicado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Email duplicado");
    }

    @Test
    void illegalArgument_devuelve400ConPrefijoDeValorNoValido() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Frecuencia desconocida"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("Frecuencia desconocida");
    }

    @Test
    void runtimeNoControlada_devuelve500ConMensajeGenérico() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleRuntime(new RuntimeException("Cualquier cosa interna"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage())
                .as("No debe filtrar el detalle interno al cliente")
                .isEqualTo("Error interno del servidor");
    }
}
