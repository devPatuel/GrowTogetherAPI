package com.jordipatuel.GrowTogetherAPI.repository;
import com.jordipatuel.GrowTogetherAPI.model.ParticipacionDesafio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ParticipacionDesafioRepository extends JpaRepository<ParticipacionDesafio, Long> {
    List<ParticipacionDesafio> findByDesafioId(Integer desafioId);
    List<ParticipacionDesafio> findByUsuarioId(Long usuarioId);
    Optional<ParticipacionDesafio> findByDesafioIdAndUsuarioId(Integer desafioId, Long usuarioId);
    List<ParticipacionDesafio> findByDesafioIdOrderByPuntosGanadosEnDesafioDesc(Integer desafioId);
}
