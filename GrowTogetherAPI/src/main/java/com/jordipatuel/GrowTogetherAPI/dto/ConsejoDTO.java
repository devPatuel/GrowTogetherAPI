package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
/**
 * DTO de respuesta con los datos de un consejo de bienestar.
 * Se devuelve en los endpoints de listado de consejos para usuarios y admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsejoDTO {

    /** Identificador único del consejo. */
    private Integer id;

    /** Título del consejo. */
    private String titulo;

    /** Contenido del consejo. */
    private String descripcion;

    /** Fecha en la que se publicó el consejo. */
    private LocalDate fechaPublicacion;

    /** Indica si el consejo es visible para los usuarios. */
    private boolean activo;

    /** ID del admin que creó el consejo. */
    private Long creadorId;
}
