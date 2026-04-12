package com.jordipatuel.GrowTogetherAPI.repository;

import com.jordipatuel.GrowTogetherAPI.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

/**
 * Repositorio de acceso a datos de {@link AuditLog}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 * Todas las consultas devuelven como máximo 100 registros para evitar sobrecargar la respuesta.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Devuelve los últimos 100 registros de auditoría de un usuario concreto ordenados por fecha descendente. */
    List<AuditLog> findTop100ByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /** Devuelve todos los registros de auditoría de un tipo de entidad concreto ordenados por fecha descendente. */
    List<AuditLog> findByEntidadOrderByFechaDesc(String entidad);

    /** Devuelve los registros de auditoría entre dos fechas ordenados por fecha descendente. */
    List<AuditLog> findByFechaBetweenOrderByFechaDesc(Date desde, Date hasta);

    /** Devuelve los últimos 100 registros de auditoría de toda la plataforma ordenados por fecha descendente. */
    List<AuditLog> findTop100ByOrderByFechaDesc();
}
