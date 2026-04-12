package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link Usuario}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por su email exacto. Usado en login y validación de email único. */
    Optional<Usuario> findByEmail(String email);

    /** Busca usuarios cuyo nombre o email contengan el texto indicado, ignorando mayúsculas. */
    List<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);
}
