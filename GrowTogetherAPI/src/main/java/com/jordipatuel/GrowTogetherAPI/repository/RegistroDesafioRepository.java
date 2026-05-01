package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.RegistroDesafio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link RegistroDesafio}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface RegistroDesafioRepository extends JpaRepository<RegistroDesafio, Long> {

    /** Busca el registro de un desafío en una fecha exacta para un usuario. Usado al completar/descompletar. */
    Optional<RegistroDesafio> findByDesafio_IdAndUsuario_IdAndFecha(Integer desafioId, Long usuarioId, LocalDate fecha);

    /** Devuelve todos los registros de un desafío para un usuario sin filtro de fecha. */
    List<RegistroDesafio> findByDesafio_IdAndUsuario_Id(Integer desafioId, Long usuarioId);

    /** Devuelve todos los registros de un desafío de todos los participantes ordenados por fecha. Usado en la gráfica. */
    List<RegistroDesafio> findByDesafio_IdOrderByFechaAsc(Integer desafioId);

    /** Devuelve los registros de un desafío de un usuario en un rango de fechas ordenados descendente. */
    List<RegistroDesafio> findByDesafio_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(
            Integer desafioId, Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    /** Devuelve los registros de un desafío en un rango de fechas para todos los participantes. */
    List<RegistroDesafio> findByDesafio_IdAndFechaBetween(Integer desafioId, LocalDate fechaInicio, LocalDate fechaFin);
}
