package com.jordipatuel.GrowTogetherAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String accion;
    private String entidad;
    private Long entidadId;
    private Long usuarioId;
    private String usuarioEmail;
    private String detalle;
    private String ip;
    private Date fecha;
}
