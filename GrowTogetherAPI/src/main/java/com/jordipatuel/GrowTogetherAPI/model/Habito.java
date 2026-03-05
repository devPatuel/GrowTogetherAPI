package com.jordipatuel.GrowTogetherAPI.model;

import jakarta.persistence.*;
import java.util.List;
import jakarta.validation.constraints.*;
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
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private int rachaActual;

    @Column(nullable = false)
    private int rachaMaxima;

    @ToString.Exclude
    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroHabito> registroHabitos;

    @ToString.Exclude
    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacion> notificaciones;
}
