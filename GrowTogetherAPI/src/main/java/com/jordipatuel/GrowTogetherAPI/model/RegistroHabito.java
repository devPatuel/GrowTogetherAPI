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
@Entity
@Table(name = "registro_habitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistroHabito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotNull(message = "La fecha no puede ser nula")
    @Column(nullable = false)
    private LocalDate fecha;
    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabito estado;
    @NotNull(message = "El usuario es obligatorio")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @NotNull(message = "El hábito es obligatorio")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "habito_id", nullable = false)
    private Habito habito;
}
