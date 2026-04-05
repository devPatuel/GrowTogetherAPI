package com.jordipatuel.GrowTogetherAPI.service;

import com.jordipatuel.GrowTogetherAPI.model.AuditLog;
import com.jordipatuel.GrowTogetherAPI.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

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

    public List<AuditLog> obtenerUltimos() {
        return auditLogRepository.findTop100ByOrderByFechaDesc();
    }

    public List<AuditLog> obtenerPorUsuario(Long usuarioId) {
        return auditLogRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public List<AuditLog> obtenerPorEntidad(String entidad) {
        return auditLogRepository.findByEntidadOrderByFechaDesc(entidad);
    }
}
