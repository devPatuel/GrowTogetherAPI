package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.service.JwtService;
import com.jordipatuel.GrowTogetherAPI.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.jordipatuel.GrowTogetherAPI.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador de autenticación.
 *
 * Gestiona los endpoints públicos de registro, login y recuperación de contraseña.
 * No requiere JWT. El login registra en el audit log tanto los intentos exitosos
 * como los fallidos.
 *
 * Todos los endpoints consumen y devuelven DTOs: el mapeo a/desde la entidad
 * {@code Usuario} vive dentro de {@link UsuarioService}.
 */
@RestController
@RequestMapping(Config.API_URL + "/auth")
public class AuthController {
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    /**
     * Inyecta los servicios necesarios para autenticación y registro.
     *
     * @param usuarioService gestión y registro de usuarios
     * @param authenticationManager componente de Spring Security para validar credenciales
     * @param jwtService generación de tokens JWT firmados
     * @param auditService persistencia de los intentos de login (OK y fallidos)
     */
    @Autowired
    public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager,
                          JwtService jwtService, AuditService auditService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    /**
     * Registra un nuevo usuario. Valida el DTO de entrada y devuelve el DTO del usuario creado.
     * POST /api/v1/auth/registrar
     *
     * @param dto datos del nuevo usuario (nombre, email, password y foto opcional)
     * @return el usuario creado con código 201
     */
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioDTO> registrarUsuario(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioDTO nuevoUsuario = usuarioService.registrarUsuario(dto);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }
    /**
     * Autentica al usuario con email y contraseña y devuelve el JWT junto con
     * los datos básicos del usuario (id, nombre, email, rol, tema, idioma).
     * Registra el resultado en el audit log. Devuelve 401 si las credenciales son incorrectas.
     * POST /api/v1/auth/login
     *
     * @param loginRequest credenciales (email y password)
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return 200 con token y perfil básico, o 401 si las credenciales no son válidas
     */
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            String token = jwtService.generateToken(authentication);
            UsuarioDTO usuarioInfo = usuarioService.obtenerUsuarioPorEmail(loginRequest.getEmail());

            auditService.registrar("LOGIN_OK", "Usuario", usuarioInfo.getId(),
                    usuarioInfo.getId(), usuarioInfo.getEmail(),
                    "Login exitoso", request.getRemoteAddr());

            return ResponseEntity.ok(java.util.Map.of(
                "token", token,
                "message", "Login exitoso",
                "usuarioId", usuarioInfo.getId(),
                "nombre", usuarioInfo.getNombre(),
                "email", usuarioInfo.getEmail(),
                "rol", usuarioInfo.getRol().name(),
                "tema", usuarioInfo.getTema(),
                "idioma", usuarioInfo.getIdioma()
            ));
        } catch (org.springframework.security.core.AuthenticationException e) {
            auditService.registrar("LOGIN_FAIL", "Usuario", null,
                    0L, loginRequest.getEmail(),
                    "Intento de login fallido", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Credenciales inválidas"));
        }
    }
    /**
     * Stub de recuperación de contraseña. Siempre devuelve 200 con un mensaje genérico
     * independientemente de si el email existe, para no filtrar información.
     * POST /api/v1/auth/recuperar
     *
     * @param request cuerpo con el email del usuario que quiere recuperar la contraseña
     * @return 200 con mensaje genérico
     */
    @PostMapping("/recuperar")
    public ResponseEntity<String> recuperarContrasena(@RequestBody RecuperarRequest request) {
        return new ResponseEntity<>("Si el correo existe, enviaremos un email de recuperación", HttpStatus.OK);
    }
    /**
     * DTO interno para la petición de login (email + password).
     */
    @Data
    public static class LoginRequest {
        /** Constructor sin argumentos requerido por Jackson para deserializar el body. */
        public LoginRequest() {}
        @NotBlank(message = "El email es requerido")
        private String email;
        @NotBlank(message = "La contraseña es requerida")
        private String password;
    }
    /**
     * DTO interno para la petición de recuperación de contraseña (email).
     */
    @Data
    public static class RecuperarRequest {
        /** Constructor sin argumentos requerido por Jackson para deserializar el body. */
        public RecuperarRequest() {}
        /** Email del usuario que solicita recuperar la contraseña. */
        private String email;
    }
}
