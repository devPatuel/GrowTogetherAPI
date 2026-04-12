package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO de respuesta con los datos de la participación de un usuario en un desafío.
 * Se usa en el endpoint de ranking para mostrar la clasificación de participantes.
 * Incluye el nombre del usuario para evitar llamadas adicionales al endpoint de perfil.
 */
public class ParticipacionDesafioDTO {

    /** Identificador único de la participación. */
    private Long id;

    /** Fecha en la que el usuario se inscribió en el desafío. */
    private Date fechaInscripcion;

    /** Estado actual de la participación: ACTIVO, SUPERADO o ABANDONADO. */
    private EstadoProgreso estadoProgreso;

    /** Puntos acumulados por el usuario dentro de este desafío. Usado para el ranking. */
    private int puntosGanadosEnDesafio;

    /** ID del usuario participante. */
    private Long usuarioId;

    /** Nombre del usuario participante. */
    private String usuarioNombre;

    /** ID del desafío en el que participa. */
    private Integer desafioId;
}
