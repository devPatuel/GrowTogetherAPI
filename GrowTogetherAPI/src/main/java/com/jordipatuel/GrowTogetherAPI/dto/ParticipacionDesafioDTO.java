package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
/**
 * DTO de respuesta con los datos de la participación de un usuario en un desafío.
 * Se usa en el endpoint de ranking y embebido en {@link DesafioDTO} para listar
 * a todos los participantes con sus métricas (puntos, racha, posición, foto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipacionDesafioDTO {

    /** Identificador único de la participación. */
    private Long id;

    /** Fecha en la que el usuario se inscribió en el desafío. */
    private Date fechaInscripcion;

    /** Estado actual de la participación: ACTIVO, SUPERADO o ABANDONADO. */
    private EstadoProgreso estadoProgreso;

    /** Puntos acumulados por el usuario dentro de este desafío. */
    private int puntosGanadosEnDesafio;

    /** Días consecutivos completados en el desafío. */
    private int rachaActual;

    /** Mejor racha histórica del participante en este desafío. */
    private int rachaMaxima;

    /** Indica si el participante ya completó el desafío hoy. */
    private boolean completadoHoy;

    /** Posición del participante en el ranking (1, 2, 3...). Null si no está calculada. */
    private Integer posicion;

    /** ID del usuario participante. */
    private Long usuarioId;

    /** Nombre del usuario participante. */
    private String usuarioNombre;

    /** Foto base64 del usuario participante para pintar el avatar sin llamada extra. */
    private String usuarioFoto;

    /** ID del desafío en el que participa. */
    private Integer desafioId;

    /**
     * Puntos que ganaría el participante si marcase hoy como completado.
     * Se calcula con la racha que tendría tras marcar (no con rachaActual + 1)
     * para que coincida con lo que aplicaría el backend en {@code completarDesafio}.
     */
    private int puntosSiguientes;

    /**
     * Multiplicador que se aplicaría al marcar hoy como completado.
     * Refleja el bonus real de racha consecutiva, no la mera predicción optimista.
     */
    private double multiplicadorSiguiente;
}
