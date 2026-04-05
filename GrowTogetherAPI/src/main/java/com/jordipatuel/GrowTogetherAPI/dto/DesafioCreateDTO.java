package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesafioCreateDTO {
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    @NotBlank(message = "El objetivo no puede estar vacío")
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres")
    private String objetivo;
    @NotNull(message = "La fecha de inicio no puede ser nula")
    private Date fechaInicio;
    @NotNull(message = "La fecha de fin no puede ser nula")
    private Date fechaFin;
}
