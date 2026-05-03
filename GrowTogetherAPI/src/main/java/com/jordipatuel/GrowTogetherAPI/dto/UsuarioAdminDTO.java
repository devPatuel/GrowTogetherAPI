package com.jordipatuel.GrowTogetherAPI.dto;
import com.jordipatuel.GrowTogetherAPI.model.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * DTO de respuesta con los datos completos de un usuario para uso del panel admin.
 * Incluye los campos sensibles (activo, motivoBloqueo, fechaBloqueo) que el DTO
 * de cliente {@link UsuarioDTO} oculta. Solo se devuelve en endpoints de /admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdminDTO {

    /** Identificador único del usuario en la base de datos. */
    private Long id;

    /** Nombre visible del usuario. */
    private String nombre;

    /** Email del usuario, también username de login. */
    private String email;

    /** Rol del usuario: STANDARD o ADMIN. */
    private Roles rol;

    /** Fecha de registro del usuario. */
    private Date fechaRegistro;

    /** Puntos acumulados completando hábitos. */
    private int puntosTotales;

    /** Foto de perfil en base64. Puede ser null. */
    private String foto;

    /** Indica si la cuenta está activa. En false, está bloqueada. */
    private boolean activo;

    /** Motivo del bloqueo. Solo tiene valor cuando activo=false. */
    private String motivoBloqueo;

    /** Fecha del bloqueo. Solo tiene valor cuando activo=false. */
    private Date fechaBloqueo;
}
