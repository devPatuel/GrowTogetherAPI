package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.util.Date;
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
 * Entidad que representa un desafío creado por un usuario.
 * Usa soft delete: en vez de borrarse físicamente se pone activo = false.
 * La validación de que fechaFin sea posterior a fechaInicio y a la fecha actual
 * se realiza en el servicio, no en la entidad.
 *
 * Comparte campos con {@link Habito} (icono, tipo, frecuencia, diasSemana) porque
 * los desafíos se ejecutan como un hábito compartido entre varios participantes.
 */
@Entity
@Table(name = "desafios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Desafio {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /** Nombre del desafío. */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Objetivo medible del desafío. Se mantiene por compatibilidad con datos seed. */
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres")
    @Column(length = 500)
    private String objetivo;

    /** Descripción del desafío que se muestra al usuario en cabecera y card. */
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    /** Fecha de inicio del desafío. */
    @NotNull(message = "La fecha de inicio no puede ser nula")
    @Column(nullable = false)
    private Date fechaInicio;

    /** Fecha de fin del desafío. Debe ser posterior a fechaInicio. */
    @NotNull(message = "La fecha de fin no puede ser nula")
    @Column(nullable = false)
    private Date fechaFin;

    /** Indica si el desafío está activo. En false no aparece en los listados. */
    @Column(nullable = false)
    private boolean activo = true;

    /** Frecuencia del desafío: DIARIO o PERSONALIZADO. Por defecto DIARIO. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Frecuencia frecuencia = Frecuencia.DIARIO;

    /** Tipo del desafío: POSITIVO o NEGATIVO. Por defecto POSITIVO. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoHabito tipo = TipoHabito.POSITIVO;

    /** Identificador del icono seleccionado por el usuario. Mismo set que en hábitos. */
    @Size(max = 50, message = "El icono no puede superar los 50 caracteres")
    @Column(length = 50)
    private String icono;

    /** Días de la semana asignados al desafío. Solo aplica cuando frecuencia es PERSONALIZADO. */
    @ElementCollection(targetClass = DiaSemana.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "desafio_dias", joinColumns = @JoinColumn(name = "desafio_id"))
    @Column(name = "dia")
    private Set<DiaSemana> diasSemana = new HashSet<>();

    /** Usuario que creó el desafío. Solo el creador puede eliminarlo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    @ToString.Exclude
    private Usuario creador;

    /** Lista de participaciones de usuarios en este desafío. */
    @ToString.Exclude
    @OneToMany(mappedBy = "desafio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacionDesafio> participacionDesafios;
}
