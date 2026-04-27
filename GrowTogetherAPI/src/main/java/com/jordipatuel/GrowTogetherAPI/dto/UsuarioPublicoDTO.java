package com.jordipatuel.GrowTogetherAPI.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO público de un usuario visible para cualquier otro usuario autenticado.
 *
 * Se usa en la búsqueda por ID y en los listados de amigos/solicitudes donde
 * se necesita mostrar un usuario distinto al autenticado. No expone email ni rol
 * para proteger la privacidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPublicoDTO {

    /** Identificador único del usuario. */
    private Long id;

    /** Nombre visible del usuario. */
    private String nombre;

    /** Foto de perfil en base64. Puede ser null. */
    private String foto;

    /** Puntos acumulados. Útil para mostrar el nivel del usuario en el buscador. */
    private int puntosTotales;
}
