package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipacionDesafioDTO {
    private Long id;
    private Date fechaInscripcion;
    private EstadoProgreso estadoProgreso;
    private int puntosGanadosEnDesafio;
    private Long usuarioId;
    private String usuarioNombre;
    private Integer desafioId;
}
