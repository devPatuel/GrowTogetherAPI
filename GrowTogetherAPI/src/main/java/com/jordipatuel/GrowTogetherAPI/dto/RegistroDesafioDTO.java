package com.jordipatuel.GrowTogetherAPI.dto;

import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta con un registro diario de un desafío para un participante.
 * Se devuelve en GET /api/v1/desafios/{id}/historial.
 * El cliente lo usa para pintar la gráfica multilínea de evolución de puntos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDesafioDTO {

    /** ID del usuario participante. */
    private Long usuarioId;

    /** Fecha del registro. */
    private LocalDate fecha;

    /** Estado del desafío ese día: COMPLETADO, PENDIENTE o NO_COMPLETADO. */
    private EstadoHabito estado;

    /** Puntos otorgados ese día con el bonus de racha aplicado. */
    private int puntosGanados;
}
