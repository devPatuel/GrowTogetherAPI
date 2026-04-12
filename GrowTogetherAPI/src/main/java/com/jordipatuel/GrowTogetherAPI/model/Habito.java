package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.*;
import com.jordipatuel.GrowTogetherAPI.model.enums.DiaSemana;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.model.enums.TipoHabito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * Entidad que representa un hábito creado por un usuario.
 * Usa soft delete: en vez de borrarse físicamente se pone activo = false.
 * Los días de la semana se almacenan en una tabla secundaria habito_dias
 * usando @ElementCollection, solo aplican cuando frecuencia es PERSONALIZADO.
 */
@Entity
@Table(name = "habitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Habito {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /** Nombre del hábito. */
    @NotBlank(message = "El nombre del hábito no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Descripción del hábito. */
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;

    /** Número de días consecutivos completando el hábito hasta hoy. Se recalcula al completar/descompletar. */
    @Column(nullable = false)
    private int rachaActual;

    /** Mayor racha consecutiva alcanzada históricamente. */
    @Column(nullable = false)
    private int rachaMaxima;

    /** Indica si el hábito está activo. En false no aparece en los listados del usuario. */
    @Column(nullable = false)
    private boolean activo = true;

    /** Frecuencia del hábito: DIARIO o PERSONALIZADO. Por defecto DIARIO. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Frecuencia frecuencia = Frecuencia.DIARIO;

    /** Tipo de hábito: POSITIVO (quiero adquirirlo) o NEGATIVO (quiero eliminarlo). Por defecto POSITIVO. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoHabito tipo = TipoHabito.POSITIVO;

    /** Fecha en que se creó el hábito. Se asigna automáticamente al crear. */
    @Column(nullable = false)
    private LocalDate fechaInicio = LocalDate.now();

    /** Identificador del icono seleccionado por el usuario en la app. */
    @Size(max = 50, message = "El icono no puede superar los 50 caracteres")
    @Column(length = 50)
    private String icono;

    /** Días de la semana asignados al hábito. Solo aplica cuando frecuencia es PERSONALIZADO. */
    @ElementCollection(targetClass = DiaSemana.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "habito_dias", joinColumns = @JoinColumn(name = "habito_id"))
    @Column(name = "dia")
    private Set<DiaSemana> diasSemana = new HashSet<>();

    /** Historial de registros diarios de este hábito. */
    @ToString.Exclude
    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroHabito> registroHabitos;

    /** Usuario propietario del hábito. */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Notificaciones de recordatorio asociadas a este hábito. */
    @ToString.Exclude
    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacion> notificaciones;
}
