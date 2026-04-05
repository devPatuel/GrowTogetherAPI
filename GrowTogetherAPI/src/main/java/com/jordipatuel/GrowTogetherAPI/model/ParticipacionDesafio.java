package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.util.Date;
import jakarta.validation.constraints.*;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Entity
@Table(name = "participacion_desafios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipacionDesafio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotNull(message = "La fecha de inscripción no puede ser nula")
    @Column(nullable = false)
    private Date fechaInscripcion;
    @NotNull(message = "El estado de progreso no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProgreso estadoProgreso;
    @Min(value = 0, message = "Los puntos ganados no pueden ser negativos")
    @Column(nullable = false)
    private int puntosGanadosEnDesafio;
    @NotNull(message = "El usuario asociado no puede ser nulo")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @NotNull(message = "El desafío asociado no puede ser nulo")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "desafio_id", nullable = false)
    private Desafio desafio;
}
