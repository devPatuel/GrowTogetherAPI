package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
/**
 * DTO para la creación de un consejo de bienestar.
 * Solo accesible por usuarios con rol ADMIN a través de POST /api/v1/admin/recursos.
 * Si no se indica fechaPublicacion, el servicio asigna la fecha actual.
 * Si no se indica activo, el servicio lo crea como activo por defecto.
 * El creadorId se extrae del token JWT del admin, no se recibe en el DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsejoCreateDTO {

    /** Título del consejo. Entre 2 y 200 caracteres. */
    @NotBlank(message = "El título del consejo es obligatorio")
    @Size(min = 2, max = 200, message = "El título debe tener entre 2 y 200 caracteres")
    private String titulo;

    /** Contenido del consejo. Máximo 5000 caracteres. */
    @NotBlank(message = "La descripción del consejo no puede estar vacía")
    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
    private String descripcion;

    /** Fecha de publicación. Opcional: si no se indica, se usa la fecha actual. */
    private LocalDate fechaPublicacion;

    /** Indica si el consejo es visible para los usuarios. Opcional: por defecto true. */
    private Boolean activo;
}
