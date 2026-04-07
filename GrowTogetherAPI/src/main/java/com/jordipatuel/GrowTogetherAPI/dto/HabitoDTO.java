package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private int rachaActual;
    private int rachaMaxima;
    private Long usuarioId;
    private boolean completadoHoy;
    private String frecuencia;
    private Set<String> diasSemana;
    private String tipo;
    private String icono;
    private LocalDate fechaInicio;
    private double progresoMensual;
}
