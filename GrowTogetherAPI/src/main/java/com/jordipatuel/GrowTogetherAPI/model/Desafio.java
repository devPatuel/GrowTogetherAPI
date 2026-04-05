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
@Entity
@Table(name = "desafios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Desafio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;
    @NotBlank(message = "El objetivo no puede estar vacío")
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres")
    @Column(nullable = false, length = 500)
    private String objetivo;
    @NotNull(message = "La fecha de inicio no puede ser nula")
    @Column(nullable = false)
    private Date fechaInicio;
    @NotNull(message = "La fecha de fin no puede ser nula")
    @Column(nullable = false)
    private Date fechaFin;
    @Column(nullable = false)
    private boolean activo = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    @ToString.Exclude
    private Usuario creador;

    @ToString.Exclude
    @OneToMany(mappedBy = "desafio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacionDesafio> participacionDesafios;
}
