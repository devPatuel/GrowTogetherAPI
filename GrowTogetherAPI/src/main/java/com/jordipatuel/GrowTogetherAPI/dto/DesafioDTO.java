package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesafioDTO {
    private Integer id;
    private String nombre;
    private String objetivo;
    private Date fechaInicio;
    private Date fechaFin;
    private Long creadorId;
    private String creadorNombre;
}
