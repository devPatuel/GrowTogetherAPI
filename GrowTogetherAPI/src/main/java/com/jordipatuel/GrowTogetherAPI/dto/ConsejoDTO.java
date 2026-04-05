package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsejoDTO {
    private Integer id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaPublicacion;
    private boolean activo;
    private Long creadorId;
}
