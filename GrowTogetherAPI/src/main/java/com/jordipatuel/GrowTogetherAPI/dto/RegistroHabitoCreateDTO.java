package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO para crear un registro diario del estado de un hábito.
 * Usado internamente por el servicio al completar o descompletar un hábito.
 * El cliente no envía este DTO directamente: usuarioId y habitoId
 * se extraen del token JWT y de la URL del endpoint.
 */
public class RegistroHabitoCreateDTO {

    /** Fecha del registro. Corresponde al día en que se completa o descompleta el hábito. */
    @NotNull(message = "La fecha no puede ser nula")
    private LocalDate fecha;

    /** Estado del hábito en esa fecha: COMPLETADO, PENDIENTE o NO_COMPLETADO. */
    @NotNull(message = "El estado no puede ser nulo")
    private EstadoHabito estado;

    /** ID del usuario propietario del hábito. Se extrae del token JWT. */
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    /** ID del hábito al que pertenece el registro. Se extrae de la URL. */
    @NotNull(message = "El hábito es obligatorio")
    private Integer habitoId;
}
