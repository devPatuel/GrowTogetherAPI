package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.SolicitudAmistad;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link SolicitudAmistad}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {

    /** Solicitudes recibidas por el usuario en el estado indicado. */
    List<SolicitudAmistad> findByDestinatarioIdAndEstado(Long destinatarioId, EstadoSolicitud estado);

    /** Solicitudes enviadas por el usuario en el estado indicado. */
    List<SolicitudAmistad> findByRemitenteIdAndEstado(Long remitenteId, EstadoSolicitud estado);

    /** Busca una solicitud concreta entre dos usuarios y estado. Usado para validar duplicados. */
    Optional<SolicitudAmistad> findByRemitenteIdAndDestinatarioIdAndEstado(
            Long remitenteId, Long destinatarioId, EstadoSolicitud estado);
}
