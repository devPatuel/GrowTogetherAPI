package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.AuditLogDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoDTO;
import com.jordipatuel.GrowTogetherAPI.service.AuditService;
import com.jordipatuel.GrowTogetherAPI.service.ConsejoService;
import com.jordipatuel.GrowTogetherAPI.service.DesafioService;
import com.jordipatuel.GrowTogetherAPI.service.RegistroHabitoService;
import com.jordipatuel.GrowTogetherAPI.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Controlador de operaciones administrativas.
 *
 * Todos los endpoints requieren rol ADMIN. Cualquier acción sensible
 * (reset de contraseña, eliminar usuario, gestión de consejos) queda registrada
 * en el audit log con la IP del admin que la ejecutó.
 *
 * Todos los endpoints consumen y devuelven DTOs: el mapeo a/desde las entidades
 * vive en los services correspondientes.
 */
@RestController
@RequestMapping(Config.API_URL + "/admin")
public class AdminController {
    private final UsuarioService usuarioService;
    private final ConsejoService consejoService;
    private final RegistroHabitoService registroHabitoService;
    private final DesafioService desafioService;
    private final AuditService auditService;
    @Autowired
    public AdminController(UsuarioService usuarioService, ConsejoService consejoService,
                           RegistroHabitoService registroHabitoService, DesafioService desafioService,
                           AuditService auditService) {
        this.usuarioService = usuarioService;
        this.consejoService = consejoService;
        this.registroHabitoService = registroHabitoService;
        this.desafioService = desafioService;
        this.auditService = auditService;
    }

    /**
     * Devuelve métricas globales de la plataforma: total de usuarios,
     * hábitos completados hoy y desafíos activos.
     * GET /api/v1/admin/metricas
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> verMetricas() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalUsuarios", usuarioService.obtenerTodos().size());
        metricas.put("habitosCompletadosHoy", registroHabitoService.contarCompletadosHoy());
        metricas.put("desafiosActivos", desafioService.contarDesafiosActivos());
        return ResponseEntity.ok(metricas);
    }
    /**
     * Resetea la contraseña de un usuario sin verificar la actual.
     * Valida la nueva contraseña con la misma política que el registro.
     * Registra la acción en el audit log.
     * PUT /api/v1/admin/usuarios/{id}/resetear-contrasena
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/usuarios/{id}/resetear-contrasena")
    public ResponseEntity<String> resetearContrasena(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication,
            HttpServletRequest request) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 8 caracteres");
        }
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            return ResponseEntity.badRequest().body("La contraseña debe contener al menos una mayúscula, una minúscula y un dígito");
        }
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        usuarioService.resetearContrasena(id, newPassword);
        auditService.registrar("RESET_PASSWORD", "Usuario", id,
                principal.getId(), principal.getUsername(),
                "Contraseña reseteada por admin", request.getRemoteAddr());
        return ResponseEntity.ok("Contraseña reseteada correctamente");
    }
    /**
     * Crea un nuevo consejo asignando el admin autenticado como creador.
     * Registra la acción en el audit log.
     * POST /api/v1/admin/recursos
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/recursos")
    public ResponseEntity<ConsejoDTO> subirRecurso(
            @Valid @RequestBody ConsejoCreateDTO dto,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        ConsejoDTO guardado = consejoService.crearConsejo(dto, principal.getId());
        auditService.registrar("CREAR", "Consejo", Long.valueOf(guardado.getId()),
                principal.getId(), principal.getUsername(),
                "Recurso creado: " + guardado.getTitulo(), request.getRemoteAddr());
        return new ResponseEntity<>(guardado, HttpStatus.CREATED);
    }
    /**
     * Devuelve todos los consejos sin filtrar (incluye inactivos y futuros).
     * GET /api/v1/admin/recursos
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recursos")
    public ResponseEntity<List<ConsejoDTO>> listarTodosLosRecursos() {
        return ResponseEntity.ok(consejoService.obtenerTodos());
    }
    /**
     * Edita un consejo existente. Registra la acción en el audit log.
     * PUT /api/v1/admin/recursos/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/recursos/{id}")
    public ResponseEntity<ConsejoDTO> editarRecurso(
            @PathVariable Integer id,
            @Valid @RequestBody ConsejoCreateDTO dto,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        ConsejoDTO actualizado = consejoService.actualizarConsejo(id, dto);
        auditService.registrar("EDITAR", "Consejo", Long.valueOf(id),
                principal.getId(), principal.getUsername(),
                "Recurso editado: " + actualizado.getTitulo(), request.getRemoteAddr());
        return ResponseEntity.ok(actualizado);
    }
    /**
     * Elimina un consejo. Registra la acción en el audit log antes de eliminar.
     * DELETE /api/v1/admin/recursos/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/recursos/{id}")
    public ResponseEntity<Void> eliminarRecurso(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        auditService.registrar("ELIMINAR", "Consejo", Long.valueOf(id),
                principal.getId(), principal.getUsername(),
                "Recurso eliminado", request.getRemoteAddr());
        consejoService.eliminarConsejo(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Desactiva un usuario (soft delete) e invalida sus sesiones activas.
     * Registra la acción en el audit log.
     * DELETE /api/v1/admin/usuarios/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        auditService.registrar("ELIMINAR", "Usuario", id,
                principal.getId(), principal.getUsername(),
                "Usuario eliminado por admin", request.getRemoteAddr());
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Devuelve los últimos 100 registros del audit log.
     * GET /api/v1/admin/audit
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    public ResponseEntity<List<AuditLogDTO>> verAuditLog() {
        return ResponseEntity.ok(auditService.obtenerUltimos());
    }
    /**
     * Devuelve los últimos 100 registros del audit log de un usuario concreto.
     * GET /api/v1/admin/audit/usuario/{usuarioId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit/usuario/{usuarioId}")
    public ResponseEntity<List<AuditLogDTO>> verAuditPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(auditService.obtenerPorUsuario(usuarioId));
    }
}
