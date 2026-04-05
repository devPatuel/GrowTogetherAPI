package com.jordipatuel.GrowTogetherAPI.dto;

import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHabitoHistorialDTO {
    private LocalDate fecha;
    private EstadoHabito estado;
}
