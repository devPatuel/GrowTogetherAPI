package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Set;
/**
 * DTO para la creación de un nuevo desafío.
 * Se recibe en el endpoint POST /api/v1/desafios.
 * La validación de que fechaFin sea posterior a fechaInicio y a la fecha actual
 * se realiza en el servicio, ya que implica lógica de negocio.
 *
 * Permite invitar amigos en el mismo request mediante {@code participantesIds}:
 * el servicio creará una ParticipacionDesafio en estado ACTIVO por cada uno.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesafioCreateDTO {

    /** Nombre del desafío. Entre 2 y 100 caracteres. */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /** Descripción del desafío que se muestra al usuario. */
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    /** Objetivo medible opcional. */
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres")
    private String objetivo;

    /** Fecha de inicio del desafío. Obligatoria. */
    @NotNull(message = "La fecha de inicio no puede ser nula")
    private Date fechaInicio;

    /** Fecha de fin del desafío. Obligatoria. Debe ser posterior a fechaInicio. */
    @NotNull(message = "La fecha de fin no puede ser nula")
    private Date fechaFin;

    /** Frecuencia del desafío: DIARIO o PERSONALIZADO. Por defecto DIARIO. */
    private String frecuencia;

    /**
     * Días de la semana en los que aplica el desafío. Solo cuando frecuencia es PERSONALIZADO.
     * Valores válidos: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO.
     */
    private Set<String> diasSemana;

    /** Tipo de desafío: POSITIVO (quiero adquirirlo) o NEGATIVO (quiero eliminarlo). */
    private String tipo;

    /** Identificador del icono seleccionado por el usuario. Máximo 50 caracteres. */
    @Size(max = 50, message = "El icono no puede superar los 50 caracteres")
    private String icono;

    /** Lista opcional de IDs de usuarios a invitar como participantes al crear el desafío. */
    private List<Long> participantesIds;
}
