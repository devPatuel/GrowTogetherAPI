package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO de respuesta de error estándar.
 *
 * Devuelto por {@link com.jordipatuel.GrowTogetherAPI.exception.GlobalExceptionHandler} en todas las respuestas de error
 * para garantizar un formato consistente en la API.
 *
 * - {@code status}: código HTTP (400, 404, 500...)
 * - {@code message}: mensaje legible para el cliente
 * - {@code timestamp}: momento del error en milisegundos (epoch)
 * - {@code details}: información adicional opcional (nombre del campo, recurso no encontrado...)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String message;
    private long timestamp;
    private String details;
}
