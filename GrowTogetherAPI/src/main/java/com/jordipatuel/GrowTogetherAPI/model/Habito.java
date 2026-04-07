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
@Entity
@Table(name = "habitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Habito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @NotBlank(message = "El nombre del hábito no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Column(nullable = false)
    private int rachaActual;
    @Column(nullable = false)
    private int rachaMaxima;
    @Column(nullable = false)
    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Frecuencia frecuencia = Frecuencia.DIARIO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoHabito tipo = TipoHabito.POSITIVO;

    @Column(nullable = false)
    private LocalDate fechaInicio = LocalDate.now();

    @Size(max = 50, message = "El icono no puede superar los 50 caracteres")
    @Column(length = 50)
    private String icono;

    @ElementCollection(targetClass = DiaSemana.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "habito_dias", joinColumns = @JoinColumn(name = "habito_id"))
    @Column(name = "dia")
    private Set<DiaSemana> diasSemana = new HashSet<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroHabito> registroHabitos;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ToString.Exclude
    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacion> notificaciones;
}
