package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
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

@RestController
@RequestMapping(Config.API_URL + "/auth")
public class AuthController {
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Autowired
    public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager,
                          JwtService jwtService, AuditService auditService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioDTO> registrarUsuario(@Valid @RequestBody UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setFoto(dto.getFoto());
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return new ResponseEntity<>(mapToDTO(nuevoUsuario), HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            String token = jwtService.generateToken(authentication);
            Usuario usuarioInfo = usuarioService.obtenerUsuarioPorEmail(loginRequest.getEmail());

            auditService.registrar("LOGIN_OK", "Usuario", usuarioInfo.getId(),
                    usuarioInfo.getId(), usuarioInfo.getEmail(),
                    "Login exitoso", request.getRemoteAddr());

            return ResponseEntity.ok(java.util.Map.of(
                "token", token,
                "message", "Login exitoso",
                "usuarioId", usuarioInfo.getId(),
                "nombre", usuarioInfo.getNombre(),
                "email", usuarioInfo.getEmail()
            ));
        } catch (org.springframework.security.core.AuthenticationException e) {
            auditService.registrar("LOGIN_FAIL", "Usuario", null,
                    0L, loginRequest.getEmail(),
                    "Intento de login fallido", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Credenciales inválidas"));
        }
    }
    @PostMapping("/recuperar")
    public ResponseEntity<String> recuperarContrasena(@RequestBody RecuperarRequest request) {
        return new ResponseEntity<>("Si el correo existe, enviaremos un email de recuperación", HttpStatus.OK);
    }
    private UsuarioDTO mapToDTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(), 
            usuario.getNombre(), 
            usuario.getEmail(), 
            usuario.getRol(), 
            usuario.getFechaRegistro(), 
            usuario.getPuntosTotales(), 
            usuario.getFoto()
        );
    }
    @Data
    public static class LoginRequest {
        @NotBlank(message = "El email es requerido")
        private String email;
        @NotBlank(message = "La contraseña es requerida")
        private String password;
    }
    @Data
    public static class RecuperarRequest {
        private String email;
    }
}
