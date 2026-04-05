package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
@Repository
public interface DesafioRepository extends JpaRepository<Desafio, Integer> {
    List<Desafio> findByFechaFinAfter(Date fecha);
    long countByFechaFinAfter(Date fecha);
    List<Desafio> findByCreadorId(Long creadorId);
    List<Desafio> findByFechaFinAfterAndActivoTrue(Date fecha);
    long countByFechaFinAfterAndActivoTrue(Date fecha);
}
