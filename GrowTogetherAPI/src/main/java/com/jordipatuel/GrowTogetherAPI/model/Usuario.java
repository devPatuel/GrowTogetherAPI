package com.jordipatuel.GrowTogetherAPI.model;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.*;
import com.jordipatuel.GrowTogetherAPI.model.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Size(max = 200, message = "El email no puede superar los 200 caracteres")
    @Column(unique = true, nullable = false, length = 200)
    private String email;
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    private String password;
    @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres")
    @Column(length = 500)
    private String foto;
    @NotNull(message = "El rol no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles rol;
    @Column(nullable = false)
    private Date fechaRegistro;
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    @Column(nullable = false)
    private int puntosTotales;
    @Column(nullable = false)
    private int tokenVersion = 0;
    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, length = 20)
    private String tema = "CLARO";

    @Column(nullable = false, length = 5)
    private String idioma = "es";
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
        name = "usuario_amigos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "amigo_id")
    )
    private List<Usuario> amigos;
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroHabito> registroHabitos;
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacionDesafio> participacionDesafios;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habito> habitos;
}