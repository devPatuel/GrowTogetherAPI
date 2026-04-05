package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.sql.Time;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Column(nullable = false)
    private String mensaje;
    @NotNull(message = "La hora programada no puede ser nula")
    @Column(nullable = false)
    private Time horaProgramada;
    @NotBlank(message = "La frecuencia no puede estar vacía")
    @Column(nullable = false)
    private String frecuencia;
    @Column(nullable = false)
    private boolean activa;
    @NotNull(message = "La notificación debe estar asociada a un hábito")
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "habito_id", nullable = false)
    private Habito habito;
}
