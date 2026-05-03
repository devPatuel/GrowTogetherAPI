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
/**
 * Entidad principal del sistema. Representa a un usuario registrado en la aplicación.
 * Implementa el contrato de Spring Security a través de AuthUserDetails.
 * Usa soft delete: en vez de borrar el registro se pone activo = false.
 * La relación de amigos es ManyToMany bidireccional gestionada en la tabla usuario_amigos.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {
    /** Identificador único autogenerado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Nombre visible del usuario en la aplicación. */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Email único. Actúa como nombre de usuario para el login. */
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Size(max = 200, message = "El email no puede superar los 200 caracteres")
    @Column(unique = true, nullable = false, length = 200)
    private String email;

    /** Contraseña cifrada con BCrypt. Nunca se almacena en texto plano. */
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    private String password;

    /** Foto de perfil codificada en base64. Puede ser null. */
    @Column(columnDefinition = "TEXT")
    private String foto;

    /** Rol del usuario: STANDARD para usuarios normales, ADMIN para administradores. */
    @NotNull(message = "El rol no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles rol;

    /** Fecha en que se registró el usuario. */
    @Column(nullable = false)
    private Date fechaRegistro;

    /** Puntos acumulados completando hábitos. Cada hábito completado suma 10 puntos. */
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    @Column(nullable = false)
    private int puntosTotales;

    /**
     * Versión del token JWT activo. Se incrementa al cambiar contraseña o desactivar la cuenta,
     * invalidando todos los tokens anteriores sin necesidad de una blacklist.
     */
    @Column(nullable = false)
    private int tokenVersion = 0;

    /** Indica si la cuenta está activa. En false, el usuario no puede autenticarse. */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Motivo registrado por el admin al bloquear la cuenta. Solo tiene valor cuando activo=false
     * tras un bloqueo manual. Se limpia al desbloquear.
     */
    @Size(max = 500, message = "El motivo de bloqueo no puede superar los 500 caracteres")
    @Column(name = "motivo_bloqueo", length = 500)
    private String motivoBloqueo;

    /**
     * Fecha en la que el admin bloqueó la cuenta. Se limpia al desbloquear.
     */
    @Column(name = "fecha_bloqueo")
    private Date fechaBloqueo;

    /** Tema visual de la app seleccionado por el usuario. Por defecto CLARO. */
    @Column(nullable = false, length = 20)
    private String tema = "CLARO";

    /** Idioma de la app seleccionado por el usuario. Por defecto español. */
    @Column(nullable = false, length = 5)
    private String idioma = "es";

    /** Lista de amigos del usuario. Relación bidireccional gestionada en tabla usuario_amigos. */
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
        name = "usuario_amigos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "amigo_id")
    )
    private List<Usuario> amigos;

    /** Historial de registros diarios de hábitos del usuario. */
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroHabito> registroHabitos;

    /** Participaciones del usuario en desafíos. */
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipacionDesafio> participacionDesafios;

    /** Hábitos creados por el usuario. */
    @ToString.Exclude
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habito> habitos;
}