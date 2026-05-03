package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
/**
 * Entidad que representa un consejo de bienestar creado por un administrador.
 * No tiene relación JPA con Usuario: el creadorId se guarda como Long directo
 * para que el registro sea independiente del ciclo de vida del usuario admin.
 * Cada consejo tiene una fecha de publicación única: es el día en que se mostrará
 * a los usuarios. No puede haber dos consejos con la misma fechaPublicacion.
 */
@Entity
@Table(name = "consejos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Consejo {

    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /** Título del consejo. */
    @NotBlank(message = "El título del consejo es obligatorio")
    @Size(min = 2, max = 200, message = "El título debe tener entre 2 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    /** Contenido del consejo. */
    @NotBlank(message = "La descripción del consejo no puede estar vacía")
    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Fecha en la que se mostrará este consejo a los usuarios. Opcional.
     * Cuando tiene valor debe ser única (constraint UNIQUE a nivel de BD):
     * solo puede haber un consejo programado por día. Múltiples consejos
     * sin fecha asignada conviven sin restricción.
     */
    @Column(name = "fecha_publicacion", unique = true)
    private LocalDate fechaPublicacion;

    /** Indica si el consejo es visible para los usuarios. */
    @Column(nullable = false)
    private boolean activo = true;

    /** ID del admin que creó el consejo. Sin relación JPA. */
    @Column(name = "creador_id")
    private Long creadorId;
}
