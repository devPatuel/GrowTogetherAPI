package com.jordipatuel.GrowTogetherAPI.service;

import com.jordipatuel.GrowTogetherAPI.dto.AuditLogDTO;
import com.jordipatuel.GrowTogetherAPI.model.AuditLog;
import com.jordipatuel.GrowTogetherAPI.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de auditoría de acciones administrativas.
 *
 * Registra en base de datos las acciones sensibles realizadas por admins
 * (reset de contraseña, desactivación de usuarios, gestión de consejos).
 * Los logs son inmutables: solo se crean, nunca se modifican ni eliminan.
 * Las consultas están limitadas a los últimos 100 registros para evitar
 * sobrecargar la respuesta.
 *
 * Las consultas devuelven {@link AuditLogDTO} para que los Controllers no
 * vean la entidad. El método de registro sigue aceptando primitivos: es
 * infraestructura interna invocada desde otros services, nunca desde REST.
 */
@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Crea un nuevo registro de auditoría con la acción, entidad afectada,
     * usuario que la ejecutó, detalle y dirección IP de la petición.
     */
    public void registrar(String accion, String entidad, Long entidadId,
                          Long usuarioId, String usuarioEmail, String detalle, String ip) {
        AuditLog log = new AuditLog();
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setUsuarioId(usuarioId);
        log.setUsuarioEmail(usuarioEmail);
        log.setDetalle(detalle);
        log.setIp(ip);
        log.setFecha(new Date());
        auditLogRepository.save(log);
    }

    /**
     * Devuelve los últimos 100 registros de auditoría ordenados por fecha descendente.
     */
    public List<AuditLogDTO> obtenerUltimos() {
        return auditLogRepository.findTop100ByOrderByFechaDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve los últimos 100 registros de auditoría de un usuario concreto.
     */
    public List<AuditLogDTO> obtenerPorUsuario(Long usuarioId) {
        return auditLogRepository.findTop100ByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve todos los registros de auditoría de un tipo de entidad concreto.
     */
    public List<AuditLogDTO> obtenerPorEntidad(String entidad) {
        return auditLogRepository.findByEntidadOrderByFechaDesc(entidad).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte la entidad {@link AuditLog} al DTO de respuesta.
     */
    private AuditLogDTO toDTO(AuditLog log) {
        return new AuditLogDTO(
                log.getId(), log.getAccion(), log.getEntidad(), log.getEntidadId(),
                log.getUsuarioId(), log.getUsuarioEmail(), log.getDetalle(),
                log.getIp(), log.getFecha());
    }
}
