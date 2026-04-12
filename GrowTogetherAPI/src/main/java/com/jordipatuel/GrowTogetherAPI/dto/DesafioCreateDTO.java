package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO para la creación de un nuevo desafío.
 * Se recibe en el endpoint POST /api/v1/desafios.
 * La validación de que fechaFin sea posterior a fechaInicio y a la fecha actual
 * se realiza en el servicio, ya que implica lógica de negocio.
 */
public class DesafioCreateDTO {

    /** Nombre del desafío. Entre 2 y 100 caracteres. */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /** Descripción del objetivo a conseguir. Máximo 500 caracteres. */
    @NotBlank(message = "El objetivo no puede estar vacío")
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres")
    private String objetivo;

    /** Fecha de inicio del desafío. Obligatoria. */
    @NotNull(message = "La fecha de inicio no puede ser nula")
    private Date fechaInicio;

    /** Fecha de fin del desafío. Obligatoria. Debe ser posterior a fechaInicio. */
    @NotNull(message = "La fecha de fin no puede ser nula")
    private Date fechaFin;
}
