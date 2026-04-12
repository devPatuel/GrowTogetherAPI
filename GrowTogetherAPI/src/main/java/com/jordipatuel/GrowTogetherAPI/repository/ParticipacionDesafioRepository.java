package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.ParticipacionDesafio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link ParticipacionDesafio}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface ParticipacionDesafioRepository extends JpaRepository<ParticipacionDesafio, Long> {

    /** Devuelve todas las participaciones de un desafío concreto. */
    List<ParticipacionDesafio> findByDesafioId(Integer desafioId);

    /** Devuelve todos los desafíos en los que participa un usuario. */
    List<ParticipacionDesafio> findByUsuarioId(Long usuarioId);

    /** Busca la participación de un usuario en un desafío concreto. Usado para evitar inscripciones duplicadas. */
    Optional<ParticipacionDesafio> findByDesafioIdAndUsuarioId(Integer desafioId, Long usuarioId);

    /** Devuelve las participaciones de un desafío ordenadas por puntos descendente. Usado para el ranking. */
    List<ParticipacionDesafio> findByDesafioIdOrderByPuntosGanadosEnDesafioDesc(Integer desafioId);
}
