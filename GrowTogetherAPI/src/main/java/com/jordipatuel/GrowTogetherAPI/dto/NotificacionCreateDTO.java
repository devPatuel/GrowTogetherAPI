package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Time;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionCreateDTO {
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;
    @NotNull(message = "La hora programada no puede ser nula")
    private Time horaProgramada;
    @NotBlank(message = "La frecuencia no puede estar vacía")
    private String frecuencia;
    private boolean activa;
    @NotNull(message = "La notificación debe estar asociada a un hábito")
    private Integer habitoId;
}
