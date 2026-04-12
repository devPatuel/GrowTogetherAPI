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
/**
 * Entidad que representa la participación de un usuario en un desafío.
 * Actúa como tabla intermedia entre Usuario y Desafio con campos adicionales.
 * Los puntos acumulados aquí son independientes de los puntosTotales del usuario,
 * y su integración está pendiente de revisión.
 */
@Entity
@Table(name = "participacion_desafios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipacionDesafio {
    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Fecha en la que el usuario se inscribió en el desafío. */
    @NotNull(message = "La fecha de inscripción no puede ser nula")
    @Column(nullable = false)
    private Date fechaInscripcion;

    /** Estado actual de la participación: ACTIVO, SUPERADO o ABANDONADO. */
    @NotNull(message = "El estado de progreso no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProgreso estadoProgreso;

    /** Puntos acumulados por el usuario dentro de este desafío. Usado para el ranking. */
    @Min(value = 0, message = "Los puntos ganados no pueden ser negativos")
    @Column(nullable = false)
    private int puntosGanadosEnDesafio;

    /** Usuario participante. */
    @NotNull(message = "El usuario asociado no puede ser nulo")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Desafío en el que participa. */
    @NotNull(message = "El desafío asociado no puede ser nulo")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "desafio_id", nullable = false)
    private Desafio desafio;
}
