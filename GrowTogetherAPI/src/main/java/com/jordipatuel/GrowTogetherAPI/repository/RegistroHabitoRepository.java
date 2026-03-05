package com.jordipatuel.GrowTogetherAPI.repository;

import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {
}
