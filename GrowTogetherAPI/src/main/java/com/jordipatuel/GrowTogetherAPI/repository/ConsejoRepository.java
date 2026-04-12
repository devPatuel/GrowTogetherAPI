package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
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
     */
    @Query("SELECT c FROM Consejo c WHERE c.activo = true AND (c.fechaPublicacion IS NULL OR c.fechaPublicacion <= :fechaHoy)")
    List<Consejo> findConsejosActivosYPublicados(LocalDate fechaHoy);
}
