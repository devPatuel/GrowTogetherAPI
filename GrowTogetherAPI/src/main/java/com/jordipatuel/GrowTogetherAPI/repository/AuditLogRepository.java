package com.jordipatuel.GrowTogetherAPI.repository;

import com.jordipatuel.GrowTogetherAPI.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<AuditLog> findByEntidadOrderByFechaDesc(String entidad);
    List<AuditLog> findByFechaBetweenOrderByFechaDesc(Date desde, Date hasta);
    List<AuditLog> findTop100ByOrderByFechaDesc();
}
