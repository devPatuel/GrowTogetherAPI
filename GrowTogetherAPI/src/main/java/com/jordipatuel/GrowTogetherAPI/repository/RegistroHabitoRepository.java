package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {
    List<RegistroHabito> findByUsuario_Id(Long usuarioId);
    Optional<RegistroHabito> findByHabito_IdAndUsuario_IdAndFecha(Integer habitoId, Long usuarioId, LocalDate fecha);
    List<RegistroHabito> findByHabito_IdAndUsuario_Id(Integer habitoId, Long usuarioId);
    List<RegistroHabito> findByUsuario_IdAndFechaBetween(Long usuarioId, LocalDate start, LocalDate end);
    long countByEstadoAndFecha(com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito estado, LocalDate fecha);

    List<RegistroHabito> findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(
            Integer habitoId, Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);
}
