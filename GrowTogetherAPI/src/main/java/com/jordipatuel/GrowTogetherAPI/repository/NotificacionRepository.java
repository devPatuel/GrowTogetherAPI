package com.jordipatuel.GrowTogetherAPI.repository;

import com.jordipatuel.GrowTogetherAPI.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
}
