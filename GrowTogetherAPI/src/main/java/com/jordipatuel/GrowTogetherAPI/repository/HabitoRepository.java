package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HabitoRepository extends JpaRepository<Habito, Integer> {
    List<Habito> findByUsuarioId(Long usuarioId);
    List<Habito> findByUsuarioIdAndActivoTrue(Long usuarioId);
    List<Habito> findByActivoTrue();
}
