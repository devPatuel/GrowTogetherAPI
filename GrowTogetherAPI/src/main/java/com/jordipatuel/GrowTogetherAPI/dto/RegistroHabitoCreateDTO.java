package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHabitoCreateDTO {
    @NotNull(message = "La fecha no puede ser nula")
    private LocalDate fecha;
    @NotNull(message = "El estado no puede ser nulo")
    private EstadoHabito estado;
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    @NotNull(message = "El hábito es obligatorio")
    private Integer habitoId;
}
