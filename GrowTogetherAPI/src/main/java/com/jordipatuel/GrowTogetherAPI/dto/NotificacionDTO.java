package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Time;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private Integer id;
    private String mensaje;
    private Time horaProgramada;
    private String frecuencia;
    private boolean activa;
    private Integer habitoId;
}
