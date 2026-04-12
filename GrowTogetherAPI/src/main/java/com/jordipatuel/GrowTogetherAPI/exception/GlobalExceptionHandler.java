package com.jordipatuel.GrowTogetherAPI.exception;
import com.jordipatuel.GrowTogetherAPI.dto.ErrorResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
/**
 * Manejador global de excepciones para toda la API.
 *
 * Intercepta las excepciones no controladas y las convierte en respuestas JSON
 * estructuradas usando {@link ErrorResponseDTO}. Garantiza que ningún stack trace
 * ni información interna llegue al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de validación de @Valid en el body (campo → mensaje). Devuelve 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> mapaErrores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            mapaErrores.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(mapaErrores);
    }
    /** Violaciones de restricciones de BD (unique, not null). Devuelve 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Error de integridad: Ya existe un registro con esos datos o faltan campos obligatorios";
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                message,
                System.currentTimeMillis(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    /** Excepciones con código HTTP explícito lanzadas con ResponseStatusException. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                ex.getStatusCode().value(),
                ex.getReason(),
                System.currentTimeMillis(),
                null
        );
        return new ResponseEntity<>(response, ex.getStatusCode());
    }
    /** Reglas de negocio incumplidas lanzadas con {@link BadRequestException}. Devuelve 400. */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(BadRequestException ex) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    /** Recursos no encontrados lanzados con {@link ResourceNotFoundException}. Devuelve 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                System.currentTimeMillis(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    /** Errores de validación de @Validated en parámetros de query/path (campo → mensaje). Devuelve 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> mapaErrores = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String field = v.getPropertyPath().toString();
            mapaErrores.put(field, v.getMessage());
        });
        return ResponseEntity.badRequest().body(mapaErrores);
    }
    /** Valores de enum inválidos u otros argumentos incorrectos. Devuelve 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Valor no válido: " + ex.getMessage(),
                System.currentTimeMillis(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    /**
     * Captura cualquier RuntimeException no controlada. Logea el stack trace en servidor
     * y devuelve un mensaje genérico al cliente para no filtrar información interna. Devuelve 500.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntime(RuntimeException ex) {
        logger.error("Error interno no controlado", ex);
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                System.currentTimeMillis(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
