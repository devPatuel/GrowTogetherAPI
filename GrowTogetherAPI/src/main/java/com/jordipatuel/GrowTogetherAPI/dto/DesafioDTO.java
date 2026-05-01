package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO de respuesta con los datos de un desafío.
 * Incluye el nombre del creador y la lista embebida de participantes
 * para que el cliente pueda pintar la card sin hacer llamadas adicionales.
 */
public class DesafioDTO {

    /** Identificador único del desafío. */
    private Integer id;

    /** Nombre del desafío. */
    private String nombre;

    /** Descripción del desafío. */
    private String descripcion;

    /** Objetivo medible (opcional, se mantiene por compatibilidad con datos antiguos). */
    private String objetivo;

    /** Fecha de inicio del desafío. */
    private Date fechaInicio;

    /** Fecha de fin del desafío. */
    private Date fechaFin;

    /** Indica si el desafío está activo (no eliminado). */
    private boolean activo;

    /** Frecuencia del desafío: DIARIO o PERSONALIZADO. */
    private String frecuencia;

    /** Días de la semana asignados al desafío si la frecuencia es PERSONALIZADO. */
    private Set<String> diasSemana;

    /** Tipo del desafío: POSITIVO o NEGATIVO. */
    private String tipo;

    /** Identificador del icono del desafío. */
    private String icono;

    /** ID del usuario que creó el desafío. */
    private Long creadorId;

    /** Nombre del usuario que creó el desafío. */
    private String creadorNombre;

    /** Lista de participantes con sus métricas (puntos, rachas, posición). */
    private List<ParticipacionDesafioDTO> participantes;
}
