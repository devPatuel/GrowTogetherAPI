package com.jordipatuel.GrowTogetherAPI.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en base de datos.
 * Se traduce a HTTP 404 en {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
