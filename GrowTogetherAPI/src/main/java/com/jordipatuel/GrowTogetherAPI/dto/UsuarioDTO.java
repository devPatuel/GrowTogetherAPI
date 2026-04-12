package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO de respuesta con los datos públicos de un usuario.
 * Se devuelve en los endpoints de perfil y búsqueda.
 * No incluye password, tokenVersion ni activo: son detalles internos
 * que el cliente no necesita conocer.
 */
public class UsuarioDTO {

    /** Identificador único del usuario en la base de datos. */
    private Long id;

    /** Nombre visible del usuario. */
    private String nombre;

    /** Email del usuario. También actúa como nombre de usuario para el login. */
    private String email;

    /** Rol del usuario: STANDARD o ADMIN. */
    private Roles rol;

    /** Fecha en la que se registró el usuario. */
    private Date fechaRegistro;

    /** Puntos acumulados completando hábitos. Cada hábito completado suma 10 puntos. */
    private int puntosTotales;

    /** Foto de perfil en base64. Puede ser null si el usuario no ha subido ninguna. */
    private String foto;

    /** Tema visual de la app seleccionado por el usuario. */
    private String tema;

    /** Idioma de la app seleccionado por el usuario. */
    private String idioma;
}
