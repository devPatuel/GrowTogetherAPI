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
 * DTO de respuesta completa de un registro de hábito.
 * Incluye todos los campos del registro. Usado en consultas internas del servicio.
 */
public class RegistroHabitoDTO {

    /** Identificador único del registro. */
    private Long id;

    /** Fecha del registro. */
    private LocalDate fecha;

    /** Estado del hábito en esa fecha: COMPLETADO, PENDIENTE o NO_COMPLETADO. */
    private EstadoHabito estado;

    /** ID del usuario propietario. */
    private Long usuarioId;

    /** ID del hábito al que pertenece el registro. */
    private Integer habitoId;
}
