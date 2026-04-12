package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.*;
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

    /** Descripción del objetivo a conseguir. */
    @NotBlank(message = "El objetivo no puede estar vacío")
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres")
    @Column(nullable = false, length = 500)
    private String objetivo;

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
