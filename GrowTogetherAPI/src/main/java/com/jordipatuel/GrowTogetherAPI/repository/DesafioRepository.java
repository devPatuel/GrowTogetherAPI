package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
/**
 * Repositorio de acceso a datos de {@link Desafio}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface DesafioRepository extends JpaRepository<Desafio, Integer> {

    /** Devuelve todos los desafíos cuya fecha de fin es posterior a la indicada. */
    List<Desafio> findByFechaFinAfter(Date fecha);

    /** Cuenta los desafíos cuya fecha de fin es posterior a la indicada. */
    long countByFechaFinAfter(Date fecha);

    /** Devuelve los desafíos creados por un usuario concreto. */
    List<Desafio> findByCreadorId(Long creadorId);

    /** Devuelve los desafíos activos cuya fecha de fin es posterior a la indicada. Usado en el listado público. */
    List<Desafio> findByFechaFinAfterAndActivoTrue(Date fecha);

    /** Cuenta los desafíos activos cuya fecha de fin es posterior a la indicada. Usado en métricas admin. */
    long countByFechaFinAfterAndActivoTrue(Date fecha);
}
