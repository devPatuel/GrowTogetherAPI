package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface ConsejoRepository extends JpaRepository<Consejo, Integer> {
    @Query("SELECT c FROM Consejo c WHERE c.activo = true AND (c.fechaPublicacion IS NULL OR c.fechaPublicacion <= :fechaHoy)")
    List<Consejo> findConsejosActivosYPublicados(LocalDate fechaHoy);
}
