package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link Consejo}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 * Incluye una consulta JPQL personalizada para el filtrado de consejos visibles.
 */
@Repository
public interface ConsejoRepository extends JpaRepository<Consejo, Integer> {

    /**
     * Devuelve los consejos activos cuya fechaPublicacion es nula o anterior/igual a hoy.
     * Usa @Query porque la condición combinada (activo + fecha opcional) no se puede
     * expresar con el nombre del método.
     *
     * @param fechaHoy fecha de referencia (típicamente {@code LocalDate.now()})
     * @return lista de consejos visibles en esa fecha
     */
    @Query("SELECT c FROM Consejo c WHERE c.activo = true AND (c.fechaPublicacion IS NULL OR c.fechaPublicacion <= :fechaHoy)")
    List<Consejo> findConsejosActivosYPublicados(LocalDate fechaHoy);

    /**
     * Busca el consejo asignado a una fecha concreta. Usado para validar unicidad
     * al crear/editar y para servir el consejo del día.
     *
     * @param fechaPublicacion fecha exacta a consultar
     * @return el consejo asignado a esa fecha si existe
     */
    Optional<Consejo> findByFechaPublicacion(LocalDate fechaPublicacion);

    /**
     * Busca el consejo activo asignado a una fecha concreta. Usado por el endpoint
     * público "consejo de hoy".
     *
     * @param fechaPublicacion fecha exacta a consultar
     * @return el consejo activo asignado a esa fecha si existe
     */
    Optional<Consejo> findByFechaPublicacionAndActivoTrue(LocalDate fechaPublicacion);
}
