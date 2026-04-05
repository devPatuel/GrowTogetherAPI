package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Entity
@Table(name = "consejos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Consejo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    
    @NotBlank(message = "El título del consejo es obligatorio")
    @Size(min = 2, max = 200, message = "El título debe tener entre 2 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;
    @NotBlank(message = "La descripción del consejo no puede estar vacía")
    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;
    @Column(nullable = false)
    private boolean activo = true;
    @Column(name = "creador_id")
    private Long creadorId;
}
