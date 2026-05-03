package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio de acceso a datos de {@link Habito}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface HabitoRepository extends JpaRepository<Habito, Integer> {

    /**
     * Devuelve todos los hábitos de un usuario (activos e inactivos).
     *
     * @param usuarioId ID del usuario propietario
     * @return lista completa de hábitos del usuario
     */
    List<Habito> findByUsuarioId(Long usuarioId);

    /**
     * Devuelve solo los hábitos activos de un usuario. Usado en el listado normal.
     *
     * @param usuarioId ID del usuario propietario
     * @return lista de hábitos activos del usuario
     */
    List<Habito> findByUsuarioIdAndActivoTrue(Long usuarioId);

    /**
     * Devuelve todos los hábitos activos de la plataforma. Usado por la tarea programada nocturna.
     *
     * @return lista global de hábitos activos
     */
    List<Habito> findByActivoTrue();
}
