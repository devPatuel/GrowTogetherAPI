package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Time;
/**
 * DTO de respuesta con los datos de una notificación.
 * Se devuelve en los endpoints de listado y detalle de notificaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    /** Identificador único de la notificación. */
    private Integer id;

    /** Mensaje del recordatorio. */
    private String mensaje;

    /** Hora programada del recordatorio. */
    private Time horaProgramada;

    /** Frecuencia de repetición de la notificación. */
    private String frecuencia;

    /** Indica si la notificación está activa o pausada. */
    private boolean activa;

    /** ID del hábito al que pertenece esta notificación. */
    private Integer habitoId;
}
