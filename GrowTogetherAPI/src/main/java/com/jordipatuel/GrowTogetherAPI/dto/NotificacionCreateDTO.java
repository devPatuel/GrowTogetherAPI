package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Time;
/**
 * DTO para la creación de una notificación de recordatorio.
 * Se recibe en el endpoint POST /api/v1/notificaciones.
 * Cada notificación debe estar asociada a un hábito existente del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionCreateDTO {

    /** Mensaje del recordatorio que verá el usuario. */
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    /** Hora a la que se enviará el recordatorio. Solo hora, sin fecha (HH:mm:ss). */
    @NotNull(message = "La hora programada no puede ser nula")
    private Time horaProgramada;

    /** Indica si la notificación está activa o pausada. */
    private boolean activa;

    /** ID del hábito al que pertenece esta notificación. */
    @NotNull(message = "La notificación debe estar asociada a un hábito")
    private Integer habitoId;
}
