package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
/**
 * DTO de respuesta con los datos de un hábito.
 * Se devuelve en los endpoints de listado y detalle de hábitos.
 * Incluye campos calculados en el servicio que el cliente usa directamente
 * para pintar el estado del hábito sin necesidad de cálculos en el cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitoDTO {

    /** Identificador único del hábito. */
    private Integer id;

    /** Nombre del hábito. */
    private String nombre;

    /** Descripción del hábito. */
    private String descripcion;

    /** Número de días consecutivos completando el hábito hasta hoy. */
    private int rachaActual;

    /** Mayor racha consecutiva alcanzada históricamente. */
    private int rachaMaxima;

    /** ID del usuario propietario del hábito. */
    private Long usuarioId;

    /** Indica si el hábito ya ha sido completado hoy. Calculado en el servicio. */
    private boolean completadoHoy;

    /** Frecuencia del hábito: DIARIO o PERSONALIZADO. */
    private String frecuencia;

    /** Días de la semana asignados al hábito si la frecuencia es PERSONALIZADO. */
    private Set<String> diasSemana;

    /** Tipo de hábito: POSITIVO o NEGATIVO. */
    private String tipo;

    /** Identificador del icono del hábito. */
    private String icono;

    /** Fecha en la que se creó el hábito. */
    private LocalDate fechaInicio;

    /** Porcentaje de días completados en el mes actual. Calculado en el servicio. */
    private double progresoMensual;
}
