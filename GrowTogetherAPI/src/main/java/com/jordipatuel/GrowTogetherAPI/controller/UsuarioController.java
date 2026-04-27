package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioPublicoDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoDTO;
import com.jordipatuel.GrowTogetherAPI.dto.SolicitudAmistadDTO;
import com.jordipatuel.GrowTogetherAPI.service.UsuarioService;
import com.jordipatuel.GrowTogetherAPI.service.ConsejoService;
import com.jordipatuel.GrowTogetherAPI.service.SolicitudAmistadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * Controlador de operaciones sobre usuarios autenticados.
 *
 * Gestiona perfil, contraseña, preferencias, amigos y consulta de consejos.
 * Todos los endpoints requieren JWT. Los endpoints de perfil y contraseña
 * usan @PreAuthorize para garantizar que el usuario solo puede modificar sus propios datos.
 *
 * Todos los endpoints consumen y devuelven DTOs: el mapeo a/desde las entidades
 * vive en los services correspondientes.
 */
@Validated
@RestController
@RequestMapping(Config.API_URL + "/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final ConsejoService consejoService;
    private final SolicitudAmistadService solicitudAmistadService;
    @Autowired
    public UsuarioController(UsuarioService usuarioService,
                             ConsejoService consejoService,
                             SolicitudAmistadService solicitudAmistadService) {
        this.usuarioService = usuarioService;
        this.consejoService = consejoService;
        this.solicitudAmistadService = solicitudAmistadService;
    }

    /**
     * Devuelve el perfil del usuario. Solo accesible por el propio usuario.
     * GET /api/v1/usuarios/perfil/{id}
     */
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @GetMapping("/perfil/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }
    /**
     * Actualiza nombre, email y/o foto del perfil. Solo accesible por el propio usuario.
     * PUT /api/v1/usuarios/perfil/{id}
     */
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @PutMapping("/perfil/{id}")
    public ResponseEntity<UsuarioDTO> editarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody EditarPerfilRequest request) {
        return ResponseEntity.ok(usuarioService.editarPerfil(
                id, request.getNombre(), request.getEmail(), request.getFoto()));
    }
    /**
     * Cambia la contraseña verificando la actual. Solo accesible por el propio usuario.
     * PUT /api/v1/usuarios/perfil/{id}/contrasena
     */
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @PutMapping("/perfil/{id}/contrasena")
    public ResponseEntity<?> cambiarContrasena(
            @PathVariable Long id,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        usuarioService.cambiarContrasena(id, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(java.util.Map.of("message", "Contraseña actualizada con éxito"));
    }
    /**
     * Busca usuarios por nombre o email. Requiere mínimo 1 carácter y máximo 100.
     * GET /api/v1/usuarios/buscar?q=
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(@RequestParam @Size(min = 1, max = 100) String q) {
        return ResponseEntity.ok(usuarioService.buscarPorNombreOEmail(q));
    }
    /**
     * Agrega al usuario autenticado como amigo del usuario indicado (relación bidireccional).
     * POST /api/v1/usuarios/amigos/{amigoId}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/amigos/{amigoId}")
    public ResponseEntity<String> agregarAmigo(
            @PathVariable Long amigoId,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        usuarioService.agregarAmigo(principal.getId(), amigoId);
        return ResponseEntity.ok("Amigo agregado correctamente");
    }
    /**
     * Devuelve la lista de amigos del usuario autenticado.
     * GET /api/v1/usuarios/amigos
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/amigos")
    public ResponseEntity<List<UsuarioDTO>> listarAmigos(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(usuarioService.obtenerAmigos(principal.getId()));
    }
    /**
     * Elimina la relación de amistad en ambas direcciones.
     * DELETE /api/v1/usuarios/amigos/{amigoId}
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/amigos/{amigoId}")
    public ResponseEntity<String> eliminarAmigo(
            @PathVariable Long amigoId,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        usuarioService.eliminarAmigo(principal.getId(), amigoId);
        return ResponseEntity.ok("Amigo eliminado correctamente");
    }
    /**
     * Devuelve los consejos activos publicados hasta hoy.
     * GET /api/v1/usuarios/consejos
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/consejos")
    public ResponseEntity<List<ConsejoDTO>> verConsejosPublicos() {
        return ResponseEntity.ok(consejoService.obtenerConsejosVisibles());
    }
    /**
     * Actualiza las preferencias de tema e idioma del usuario. Solo accesible por el propio usuario.
     * PUT /api/v1/usuarios/perfil/{id}/preferencias
     */
    @PreAuthorize("isAuthenticated() and #id == authentication.principal.id")
    @PutMapping("/perfil/{id}/preferencias")
    public ResponseEntity<UsuarioDTO> actualizarPreferencias(
            @PathVariable Long id,
            @RequestBody PreferenciasRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPreferencias(
                id, request.getTema(), request.getIdioma()));
    }

    /**
     * Devuelve los datos públicos de cualquier usuario por su ID.
     * Usado por el buscador de amigos para poder localizar a alguien conocido por su ID.
     * No expone email ni rol.
     * GET /api/v1/usuarios/publico/{id}
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/publico/{id}")
    public ResponseEntity<UsuarioPublicoDTO> obtenerPublico(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPublicoPorId(id));
    }

    /**
     * Envía una solicitud de amistad al usuario indicado (requiere autenticación).
     * POST /api/v1/usuarios/amistades/solicitudes/{destinatarioId}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/amistades/solicitudes/{destinatarioId}")
    public ResponseEntity<SolicitudAmistadDTO> enviarSolicitud(
            @PathVariable Long destinatarioId,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudAmistadService.enviarSolicitud(principal.getId(), destinatarioId));
    }

    /**
     * Devuelve las solicitudes de amistad pendientes recibidas por el usuario autenticado.
     * GET /api/v1/usuarios/amistades/solicitudes/recibidas
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/amistades/solicitudes/recibidas")
    public ResponseEntity<List<SolicitudAmistadDTO>> listarRecibidas(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudAmistadService.listarRecibidasPendientes(principal.getId()));
    }

    /**
     * Devuelve las solicitudes de amistad pendientes enviadas por el usuario autenticado.
     * GET /api/v1/usuarios/amistades/solicitudes/enviadas
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/amistades/solicitudes/enviadas")
    public ResponseEntity<List<SolicitudAmistadDTO>> listarEnviadas(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudAmistadService.listarEnviadasPendientes(principal.getId()));
    }

    /**
     * Acepta una solicitud de amistad pendiente. Solo el destinatario puede hacerlo.
     * Crea la relación de amistad bidireccional.
     * PUT /api/v1/usuarios/amistades/solicitudes/{id}/aceptar
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/amistades/solicitudes/{id}/aceptar")
    public ResponseEntity<SolicitudAmistadDTO> aceptarSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudAmistadService.aceptar(id, principal.getId()));
    }

    /**
     * Rechaza una solicitud de amistad pendiente. Solo el destinatario puede hacerlo.
     * PUT /api/v1/usuarios/amistades/solicitudes/{id}/rechazar
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/amistades/solicitudes/{id}/rechazar")
    public ResponseEntity<SolicitudAmistadDTO> rechazarSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(solicitudAmistadService.rechazar(id, principal.getId()));
    }

    /**
     * Cancela una solicitud pendiente enviada por el usuario autenticado (solo el remitente).
     * DELETE /api/v1/usuarios/amistades/solicitudes/{id}
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/amistades/solicitudes/{id}")
    public ResponseEntity<String> cancelarSolicitud(
            @PathVariable Long id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        solicitudAmistadService.cancelar(id, principal.getId());
        return ResponseEntity.ok("Solicitud cancelada correctamente");
    }

    // Usamos estos DTOs dentro del controlador para no tener que crear clases separadas
    // para peticiones sencillas que solo se usan aquí.

    /**
     * DTO interno para editar nombre, email y foto del perfil.
     */
    @Data
    public static class EditarPerfilRequest {
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String nombre;
        @Email(message = "El email debe ser válido")
        @Size(max = 200, message = "El email no puede superar los 200 caracteres")
        private String email;
        private String foto;
    }
    /**
     * DTO interno para cambio de contraseña (contraseña actual + nueva con validaciones).
     */
    @Data
    public static class CambiarContrasenaRequest {
        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula y un dígito")
        private String newPassword;
    }
    /**
     * DTO interno para actualizar tema e idioma.
     */
    @Data
    public static class PreferenciasRequest {
        private String tema;
        private String idioma;
    }
}
