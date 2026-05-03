package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
/**
 * DTO para registrar la participación de un usuario en un desafío.
 * Usado internamente por el servicio al procesar POST /api/v1/desafios/{id}/unirse.
 * El usuarioId se extrae del token JWT y el desafioId de la URL,
 * por lo que el cliente no necesita enviar estos campos explícitamente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipacionDesafioCreateDTO {

    /** Fecha en la que el usuario se une al desafío. */
    @NotNull(message = "La fecha de inscripción no puede ser nula")
    private Date fechaInscripcion;

    /** Estado inicial de la participación. Al unirse siempre es ACTIVO. */
    @NotNull(message = "El estado de progreso no puede ser nulo")
    private EstadoProgreso estadoProgreso;

    /** ID del usuario que se une al desafío. Se extrae del token JWT. */
    @NotNull(message = "El usuario asociado no puede ser nulo")
    private Long usuarioId;

    /** ID del desafío al que se une el usuario. Se extrae de la URL. */
    @NotNull(message = "El desafío asociado no puede ser nulo")
    private Integer desafioId;
}
