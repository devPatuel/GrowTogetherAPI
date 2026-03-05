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
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El objetivo no puede estar vacío")
    @Column(nullable = false)
    private String objetivo; // Descripcion del evento, por ejemplo: "Quien vaya al gym mas veces en 30 dias gana"

    @NotNull(message = "La fecha de inicio no puede ser nula")
    @Column(nullable = false)
    private Date fechaInicio;

    @NotNull(message = "La fecha de fin no puede ser nula")
    @Column(nullable = false)
    private Date fechaFin;

    @ToString.Exclude
    @OneToMany(mappedBy = "desafio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacionDesafio> participacionDesafios;
}
