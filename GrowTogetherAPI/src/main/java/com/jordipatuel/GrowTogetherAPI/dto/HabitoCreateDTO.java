package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO para la creación de un nuevo hábito.
 * Se recibe en el endpoint POST /api/v1/habitos.
 * Los campos frecuencia, diasSemana, tipo e icono son opcionales:
 * el servicio asigna DIARIO y POSITIVO por defecto si no se informan.
 */
public class HabitoCreateDTO {

    /** Nombre del hábito. Entre 2 y 100 caracteres. */
    @NotBlank(message = "El nombre del hábito no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /** Descripción del hábito. Máximo 500 caracteres. */
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    /** Frecuencia del hábito: DIARIO o PERSONALIZADO. Por defecto DIARIO. */
    private String frecuencia;

    /**
     * Días de la semana en los que se realiza el hábito.
     * Solo aplica cuando frecuencia es PERSONALIZADO.
     * Valores válidos: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO.
     */
    private Set<String> diasSemana;

    /** Tipo de hábito: POSITIVO (quiero adquirirlo) o NEGATIVO (quiero eliminarlo). */
    private String tipo;

    /** Identificador del icono seleccionado por el usuario en la app. Máximo 50 caracteres. */
    @Size(max = 50, message = "El icono no puede superar los 50 caracteres")
    private String icono;
}
