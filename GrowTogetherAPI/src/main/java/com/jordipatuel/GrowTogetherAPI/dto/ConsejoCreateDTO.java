package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsejoCreateDTO {
    @NotBlank(message = "El título del consejo es obligatorio")
    @Size(min = 2, max = 200, message = "El título debe tener entre 2 y 200 caracteres")
    private String titulo;
    @NotBlank(message = "La descripción del consejo no puede estar vacía")
    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
    private String descripcion;
    private LocalDate fechaPublicacion;
    private Boolean activo;
}
