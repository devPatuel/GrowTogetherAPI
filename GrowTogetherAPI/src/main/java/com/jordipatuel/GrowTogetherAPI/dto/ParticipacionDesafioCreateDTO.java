package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipacionDesafioCreateDTO {
    @NotNull(message = "La fecha de inscripción no puede ser nula")
    private Date fechaInscripcion;
    @NotNull(message = "El estado de progreso no puede ser nulo")
    private EstadoProgreso estadoProgreso;
    @NotNull(message = "El usuario asociado no puede ser nulo")
    private Long usuarioId;
    @NotNull(message = "El desafío asociado no puede ser nulo")
    private Integer desafioId;
}
