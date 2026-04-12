package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO de respuesta con los datos de un desafío.
 * Incluye el nombre del creador para que el cliente pueda mostrarlo
 * sin necesidad de hacer una segunda llamada al endpoint de perfil.
 */
public class DesafioDTO {

    /** Identificador único del desafío. */
    private Integer id;

    /** Nombre del desafío. */
    private String nombre;

    /** Descripción del objetivo del desafío. */
    private String objetivo;

    /** Fecha de inicio del desafío. */
    private Date fechaInicio;

    /** Fecha de fin del desafío. */
    private Date fechaFin;

    /** ID del usuario que creó el desafío. */
    private Long creadorId;

    /** Nombre del usuario que creó el desafío. */
    private String creadorNombre;
}
