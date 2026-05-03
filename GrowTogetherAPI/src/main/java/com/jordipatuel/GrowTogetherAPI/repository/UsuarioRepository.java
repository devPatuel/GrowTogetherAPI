package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link Usuario}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email exacto. Usado en login y validación de email único.
     *
     * @param email email del usuario
     * @return usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuarios cuyo nombre o email contengan el texto indicado, ignorando mayúsculas.
     *
     * @param nombre fragmento a buscar en el nombre
     * @param email fragmento a buscar en el email
     * @return lista de usuarios que coinciden
     */
    List<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);

    /**
     * Cuenta los usuarios actualmente activos. Usado en métricas admin.
     *
     * @return número de usuarios activos en la plataforma
     */
    long countByActivoTrue();

    /**
     * Devuelve el usuario activo con la fecha de registro más antigua.
     * Usado para destacar al "veterano" en el dashboard admin.
     *
     * @return usuario activo más antiguo si existe
     */
    Optional<Usuario> findFirstByActivoTrueOrderByFechaRegistroAsc();

    /**
     * Cuenta los usuarios registrados a partir de una fecha (incluida).
     * Usado para construir la serie de "nuevos usuarios por mes".
     *
     * @param desde fecha inicial inclusiva
     * @param hasta fecha final exclusiva
     * @return número de usuarios registrados en el rango
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro >= :desde AND u.fechaRegistro < :hasta")
    long countByFechaRegistroBetween(@Param("desde") Date desde, @Param("hasta") Date hasta);
}
