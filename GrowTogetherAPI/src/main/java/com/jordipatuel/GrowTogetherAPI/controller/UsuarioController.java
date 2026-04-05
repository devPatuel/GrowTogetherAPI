package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoDTO;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.service.UsuarioService;
import com.jordipatuel.GrowTogetherAPI.service.ConsejoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping(Config.API_URL + "/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final ConsejoService consejoService;
    @Autowired
    public UsuarioController(UsuarioService usuarioService, ConsejoService consejoService) {
        this.usuarioService = usuarioService;
        this.consejoService = consejoService;
    }
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @GetMapping("/perfil/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(mapToDTO(usuario));
    }
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @PutMapping("/perfil/{id}")
    public ResponseEntity<UsuarioDTO> editarPerfil(
            @PathVariable Long id,
            @RequestBody EditarPerfilRequest request) {
        Usuario usuarioEditado = usuarioService.editarPerfil(
                id, request.getNombre(), request.getEmail(), request.getFoto());
        return ResponseEntity.ok(mapToDTO(usuarioEditado));
    }
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @PutMapping("/perfil/{id}/contrasena")
    public ResponseEntity<?> cambiarContrasena(
            @PathVariable Long id,
            @RequestBody CambiarContrasenaRequest request) {
        try {
            usuarioService.cambiarContrasena(id, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(java.util.Map.of("message", "Contraseña actualizada con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(@RequestParam String q) {
        List<UsuarioDTO> resultados = usuarioService.buscarPorNombreOEmail(q)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultados);
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/amigos/{amigoId}")
    public ResponseEntity<String> agregarAmigo(
            @PathVariable Long amigoId,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        usuarioService.agregarAmigo(principal.getId(), amigoId);
        return ResponseEntity.ok("Amigo agregado correctamente");
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/amigos")
    public ResponseEntity<List<UsuarioDTO>> listarAmigos(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        List<UsuarioDTO> amigos = usuarioService.obtenerAmigos(principal.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(amigos);
    }
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/amigos/{amigoId}")
    public ResponseEntity<String> eliminarAmigo(
            @PathVariable Long amigoId,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        usuarioService.eliminarAmigo(principal.getId(), amigoId);
        return ResponseEntity.ok("Amigo eliminado correctamente");
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/consejos")
    public ResponseEntity<List<ConsejoDTO>> verConsejosPublicos() {
        List<ConsejoDTO> consejosList = consejoService.obtenerConsejosVisibles()
                .stream().map(c -> new ConsejoDTO(
                        c.getId(), c.getTitulo(), c.getDescripcion(),
                        c.getFechaPublicacion(), c.isActivo(), c.getCreadorId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(consejosList);
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
    public static class EditarPerfilRequest {
        private String nombre;
        private String email;
        private String foto;
    }
    @Data
    public static class CambiarContrasenaRequest {
        private String currentPassword;
        private String newPassword;
    }
}
