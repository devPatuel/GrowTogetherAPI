package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Repositorio de acceso a datos de {@link RegistroHabito}.
 * Spring Data JPA genera la implementación automáticamente a partir de los nombres de los métodos.
 */
@Repository
public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {

    /**
     * Devuelve todos los registros de hábitos de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return lista completa de registros del usuario
     */
    List<RegistroHabito> findByUsuario_Id(Long usuarioId);

    /**
     * Busca el registro de un hábito en una fecha exacta para un usuario. Usado al completar/descompletar.
     *
     * @param habitoId ID del hábito
     * @param usuarioId ID del usuario
     * @param fecha fecha del registro
     * @return registro si existe
     */
    Optional<RegistroHabito> findByHabito_IdAndUsuario_IdAndFecha(Integer habitoId, Long usuarioId, LocalDate fecha);

    /**
     * Devuelve todos los registros de un hábito para un usuario sin filtro de fecha.
     *
     * @param habitoId ID del hábito
     * @param usuarioId ID del usuario
     * @return lista de registros del usuario para el hábito
     */
    List<RegistroHabito> findByHabito_IdAndUsuario_Id(Integer habitoId, Long usuarioId);

    /**
     * Devuelve todos los registros de hábitos de un usuario en un rango de fechas.
     *
     * @param usuarioId ID del usuario
     * @param start fecha inicial del rango (inclusive)
     * @param end fecha final del rango (inclusive)
     * @return registros del usuario en el rango
     */
    List<RegistroHabito> findByUsuario_IdAndFechaBetween(Long usuarioId, LocalDate start, LocalDate end);

    /**
     * Cuenta los registros con un estado concreto en una fecha exacta. Usado para métricas admin.
     *
     * @param estado estado del registro a contar
     * @param fecha fecha exacta a consultar
     * @return número de registros que cumplen el filtro
     */
    long countByEstadoAndFecha(com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito estado, LocalDate fecha);

    /**
     * Devuelve los registros de un hábito en un rango de fechas ordenados por fecha descendente. Usado en historial y relleno de NO_COMPLETADO.
     *
     * @param habitoId ID del hábito
     * @param usuarioId ID del usuario
     * @param fechaInicio fecha inicial del rango (inclusive)
     * @param fechaFin fecha final del rango (inclusive)
     * @return registros del hábito en el rango
     */
    List<RegistroHabito> findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(
            Integer habitoId, Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
}
