package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
/**
 * Repositorio de acceso a datos de {@link Desafio}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface DesafioRepository extends JpaRepository<Desafio, Integer> {

    /**
     * Devuelve todos los desafíos cuya fecha de fin es posterior a la indicada.
     *
     * @param fecha fecha de referencia
     * @return lista de desafíos en curso
     */
    List<Desafio> findByFechaFinAfter(Date fecha);

    /**
     * Cuenta los desafíos cuya fecha de fin es posterior a la indicada.
     *
     * @param fecha fecha de referencia
     * @return número de desafíos en curso
     */
    long countByFechaFinAfter(Date fecha);

    /**
     * Devuelve los desafíos creados por un usuario concreto.
     *
     * @param creadorId ID del usuario creador
     * @return lista de desafíos creados por el usuario
     */
    List<Desafio> findByCreadorId(Long creadorId);

    /**
     * Devuelve los desafíos activos cuya fecha de fin es posterior a la indicada. Usado en el listado público.
     *
     * @param fecha fecha de referencia
     * @return lista de desafíos activos en curso
     */
    List<Desafio> findByFechaFinAfterAndActivoTrue(Date fecha);

    /**
     * Cuenta los desafíos activos cuya fecha de fin es posterior a la indicada. Usado en métricas admin.
     *
     * @param fecha fecha de referencia
     * @return número de desafíos activos en curso
     */
    long countByFechaFinAfterAndActivoTrue(Date fecha);

    /**
     * Devuelve los desafíos en los que participa el usuario (como creador o participante).
     * Incluye finalizados para que el cliente pueda mostrar la pestaña "Finalizados".
     *
     * @param usuarioId ID del usuario consultante
     * @return lista de desafíos del usuario
     */
    @Query("SELECT DISTINCT d FROM Desafio d LEFT JOIN d.participacionDesafios p " +
           "WHERE d.activo = true AND (d.creador.id = :usuarioId OR p.usuario.id = :usuarioId)")
    List<Desafio> findMisDesafios(@Param("usuarioId") Long usuarioId);
}
