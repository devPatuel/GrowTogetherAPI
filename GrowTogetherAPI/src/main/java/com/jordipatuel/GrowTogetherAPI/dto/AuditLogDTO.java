package com.jordipatuel.GrowTogetherAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
/**
 * DTO de respuesta con los datos de un log de auditoría.
 * Solo accesible por administradores en GET /api/v1/admin/audit.
 * No existe DTO de creación porque los logs los genera el sistema automáticamente
 * ante acciones sensibles, nunca el cliente directamente.
 */
public class AuditLogDTO {

    /** Identificador único del log. */
    private Long id;

    /** Acción realizada (ej: RESET_PASSWORD, DESACTIVAR_USUARIO). */
    private String accion;

    /** Nombre de la entidad afectada (ej: Usuario, Desafio). */
    private String entidad;

    /** ID de la entidad afectada. */
    private Long entidadId;

    /** ID del admin que realizó la acción. */
    private Long usuarioId;

    /** Email del admin que realizó la acción. */
    private String usuarioEmail;

    /** Información adicional sobre la acción realizada. */
    private String detalle;

    /** IP desde la que se realizó la acción. */
    private String ip;

    /** Fecha y hora en que ocurrió el evento. */
    private Date fecha;
}
