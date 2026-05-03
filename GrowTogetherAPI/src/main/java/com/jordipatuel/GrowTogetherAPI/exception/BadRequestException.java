package com.jordipatuel.GrowTogetherAPI.exception;

/**
 * Excepción lanzada cuando una petición contiene datos inválidos o incumple
 * una regla de negocio. Se traduce a HTTP 400 en {@link GlobalExceptionHandler}.
 */
public class BadRequestException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje que se devolverá al cliente.
     *
     * @param message texto explicativo del error de negocio
     */
    public BadRequestException(String message) {
        super(message);
    }
}
