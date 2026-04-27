package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
/**
 * DTO de respuesta para una solicitud de amistad.
 * Incluye los datos visuales del remitente y destinatario (id, nombre, foto)
 * para que el cliente pueda pintar la fila sin llamadas adicionales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAmistadDTO {

    /** Identificador único de la solicitud. */
    private Long id;

    /** ID del usuario que envió la solicitud. */
    private Long remitenteId;

    /** Nombre visible del usuario que envió la solicitud. */
    private String remitenteNombre;

    /** Foto de perfil (base64) del remitente. Puede ser null. */
    private String remitenteFoto;

    /** ID del usuario que recibió la solicitud. */
    private Long destinatarioId;

    /** Nombre visible del usuario que recibió la solicitud. */
    private String destinatarioNombre;

    /** Foto de perfil (base64) del destinatario. Puede ser null. */
    private String destinatarioFoto;

    /** Estado actual de la solicitud. */
    private EstadoSolicitud estado;

    /** Momento en que se envió la solicitud. */
    private LocalDateTime fechaEnvio;

    /** Momento en que se respondió. Null mientras la solicitud esté PENDIENTE. */
    private LocalDateTime fechaRespuesta;
}
