package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * Entidad que registra el estado de un desafío para un participante en un día concreto.
 * Es la tabla central para calcular rachas, puntos y la gráfica de evolución del desafío.
 * Reutiliza el enum {@link EstadoHabito} para no duplicar nomenclatura.
 */
@Entity
@Table(name = "registro_desafios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistroDesafio {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Fecha del registro. */
    @NotNull(message = "La fecha no puede ser nula")
    @Column(nullable = false)
    private LocalDate fecha;

    /** Estado del desafío ese día para este participante. */
    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabito estado;

    /** Puntos otorgados ese día con el bonus de racha aplicado. 0 si NO_COMPLETADO. */
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    @Column(nullable = false)
    private int puntosGanados;

    /** Usuario participante al que pertenece el registro. */
    @NotNull(message = "El usuario es obligatorio")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Desafío al que pertenece este registro. */
    @NotNull(message = "El desafío es obligatorio")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "desafio_id", nullable = false)
    private Desafio desafio;
}
