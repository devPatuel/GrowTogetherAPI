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

    /**
     * Devuelve todas las notificaciones de un hábito concreto.
     *
     * @param habitoId ID del hábito
     * @return lista de notificaciones asociadas al hábito
     */
    List<Notificacion> findByHabitoId(Integer habitoId);

    /**
     * Devuelve todas las notificaciones de los hábitos de un usuario navegando por la relación notificacion → habito → usuario.
     *
     * @param usuarioId ID del usuario propietario de los hábitos
     * @return lista de notificaciones de todos los hábitos del usuario
     */
    List<Notificacion> findByHabitoUsuarioId(Long usuarioId);
}
