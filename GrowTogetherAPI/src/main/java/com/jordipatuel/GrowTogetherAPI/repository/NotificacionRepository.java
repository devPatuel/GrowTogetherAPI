package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * Repositorio de acceso a datos de {@link Notificacion}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    /** Devuelve todas las notificaciones de un hábito concreto. */
    List<Notificacion> findByHabitoId(Integer habitoId);

    /** Devuelve todas las notificaciones de los hábitos de un usuario navegando por la relación notificacion → habito → usuario. */
    List<Notificacion> findByHabitoUsuarioId(Long usuarioId);
}
