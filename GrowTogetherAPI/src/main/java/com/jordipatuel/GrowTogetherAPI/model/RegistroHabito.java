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
 * Entidad que registra el estado de un hábito en un día concreto.
 * Es la tabla central para calcular rachas, progreso e historial.
 * Los días sin registro se rellenan con NO_COMPLETADO de forma lazy
 * al consultar el historial, o automáticamente cada noche a las 00:01.
 */
@Entity
@Table(name = "registro_habitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistroHabito {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Fecha del registro. */
    @NotNull(message = "La fecha no puede ser nula")
    @Column(nullable = false)
    private LocalDate fecha;

    /** Estado del hábito ese día: COMPLETADO, PENDIENTE o NO_COMPLETADO. */
    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabito estado;

    /** Usuario propietario del hábito registrado. */
    @NotNull(message = "El usuario es obligatorio")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Hábito al que pertenece este registro. */
    @NotNull(message = "El hábito es obligatorio")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "habito_id", nullable = false)
    private Habito habito;
}
