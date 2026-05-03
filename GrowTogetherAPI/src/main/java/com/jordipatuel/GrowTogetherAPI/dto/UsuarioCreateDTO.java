package com.jordipatuel.GrowTogetherAPI.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO para el registro de un nuevo usuario.
 * Contiene los datos que manda el cliente al endpoint POST /api/v1/auth/registrar.
 * Las validaciones se aplican aquí antes de que el password se cifre con BCrypt,
 * ya que el hash resultante no pasaría la política de contraseñas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {

    /** Nombre visible del usuario. Entre 2 y 100 caracteres. */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /** Email único del usuario. Se usa como identificador de login. */
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Size(max = 200, message = "El email no puede superar los 200 caracteres")
    private String email;

    /**
     * Contraseña en texto plano. Se valida aquí antes de cifrarse.
     * Debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número
     * y un carácter especial (cualquier carácter no alfanumérico).
     */
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial")
    private String password;

    /** Foto de perfil en base64. Campo opcional. */
    private String foto;
}
