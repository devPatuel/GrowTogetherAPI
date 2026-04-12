package com.jordipatuel.GrowTogetherAPI.dto;

import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO simplificado para el historial de un hábito.
 * Solo contiene fecha y estado, que es lo que necesita el cliente
 * para pintar el heatmap en la pantalla de estadísticas.
 * Se devuelve en GET /api/v1/habitos/{id}/historial.
 */
public class RegistroHabitoHistorialDTO {

    /** Fecha del registro. */
    private LocalDate fecha;

    /** Estado del hábito ese día: COMPLETADO, PENDIENTE o NO_COMPLETADO. */
    private EstadoHabito estado;
}
