package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.AuditLogDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioAdminDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
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
    private final HabitoRepository habitoRepository;
    /**
     * Inyecta los servicios y repositorios necesarios para las operaciones administrativas.
     *
     * @param usuarioService gestión de usuarios y métricas relacionadas
     * @param consejoService CRUD de consejos publicados
     * @param registroHabitoService consultas agregadas sobre el historial de hábitos
     * @param desafioService consultas agregadas sobre desafíos activos
     * @param auditService persistencia del audit log
     * @param habitoRepository acceso directo para conteo total de hábitos
     */
    @Autowired
    public AdminController(UsuarioService usuarioService, ConsejoService consejoService,
                           RegistroHabitoService registroHabitoService, DesafioService desafioService,
                           AuditService auditService, HabitoRepository habitoRepository) {
        this.usuarioService = usuarioService;
        this.consejoService = consejoService;
        this.registroHabitoService = registroHabitoService;
        this.desafioService = desafioService;
        this.auditService = auditService;
        this.habitoRepository = habitoRepository;
    }

    /**
     * Devuelve las métricas globales del panel admin:
     * - totalUsuarios, usuariosActivos
     * - totalHabitos, habitosCompletadosHoy
     * - desafiosActivos
     * - usuarioMasVeterano (UsuarioAdminDTO o null)
     * - usuariosNuevosPorMes (lista YYYY-MM con conteo, últimos 6 meses)
     * GET /api/v1/admin/metricas
     *
     * @return mapa JSON con todas las métricas
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> verMetricas() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalUsuarios", usuarioService.obtenerTodos().size());
        metricas.put("usuariosActivos", usuarioService.contarUsuariosActivos());
        metricas.put("totalHabitos", habitoRepository.count());
        metricas.put("habitosCompletadosHoy", registroHabitoService.contarCompletadosHoy());
        metricas.put("desafiosActivos", desafioService.contarDesafiosActivos());
        metricas.put("usuarioMasVeterano", usuarioService.obtenerUsuarioMasVeterano().orElse(null));
        metricas.put("usuariosNuevosPorMes", usuarioService.contarUsuariosNuevosPorMes(6));
        return ResponseEntity.ok(metricas);
    }
    /**
     * Devuelve todos los usuarios para uso del panel admin con datos completos
     * (incluye estado de bloqueo). Ordenados activos primero, alfabéticamente.
     * GET /api/v1/admin/usuarios
     *
     * @return lista de {@link UsuarioAdminDTO} ordenada activos→bloqueados
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioAdminDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosAdmin());
    }

    /**
     * Crea un nuevo usuario con rol ADMIN. Solo otros admins pueden invocarlo.
     * Registra la acción en el audit log.
     * POST /api/v1/admin/usuarios
     *
     * @param dto datos del nuevo administrador (nombre, email, password)
     * @param authentication contexto de seguridad para extraer al admin que ejecuta la acción
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return el admin creado encapsulado en {@link UsuarioAdminDTO} con código 201
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioAdminDTO> crearAdmin(
            @Valid @RequestBody UsuarioCreateDTO dto,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        UsuarioAdminDTO creado = usuarioService.crearAdmin(dto);
        auditService.registrar("CREAR_ADMIN", "Usuario", creado.getId(),
                principal.getId(), principal.getUsername(),
                "Nuevo admin creado: " + creado.getEmail(), request.getRemoteAddr());
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    /**
     * Resetea la contraseña de un usuario sin verificar la actual.
     * Valida la nueva contraseña con la misma política que el registro.
     * Registra la acción en el audit log.
     * PUT /api/v1/admin/usuarios/{id}/resetear-contrasena
     *
     * @param id identificador del usuario al que se le resetea la contraseña
     * @param body cuerpo JSON con la clave {@code newPassword}
     * @param authentication contexto de seguridad del admin que ejecuta la acción
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return 200 con mensaje de éxito, o 400 si la contraseña no cumple la política
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
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$")) {
            return ResponseEntity.badRequest().body("La contraseña debe contener al menos una mayúscula, una minúscula, un dígito y un carácter especial");
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
     *
     * @param dto datos del consejo (título, descripción, fecha opcional, activo)
     * @param authentication contexto de seguridad para extraer al admin creador
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return el consejo creado encapsulado en {@link ConsejoDTO} con código 201
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
     *
     * @return lista completa de {@link ConsejoDTO}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recursos")
    public ResponseEntity<List<ConsejoDTO>> listarTodosLosRecursos() {
        return ResponseEntity.ok(consejoService.obtenerTodos());
    }
    /**
     * Edita un consejo existente. Registra la acción en el audit log.
     * PUT /api/v1/admin/recursos/{id}
     *
     * @param id identificador del consejo a editar
     * @param dto datos actualizados del consejo
     * @param authentication contexto de seguridad del admin que ejecuta la acción
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return el consejo actualizado
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
     *
     * @param id identificador del consejo a eliminar
     * @param authentication contexto de seguridad del admin que ejecuta la acción
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return 204 No Content
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
     * Bloquea (soft delete) un usuario guardando el motivo del bloqueo.
     * Invalida sus sesiones activas y registra la acción en el audit log.
     * Rechaza con 400 si un admin intenta bloquearse a sí mismo.
     * DELETE /api/v1/admin/usuarios/{id}
     * Body: { "motivo": "texto explicativo" }
     *
     * @param id identificador del usuario a bloquear
     * @param body cuerpo JSON con la clave {@code motivo} (obligatorio, ≤500 caracteres)
     * @param authentication contexto de seguridad del admin que ejecuta la acción
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return 204 No Content
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication,
            HttpServletRequest request) {
        String motivo = body.get("motivo");
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        if (principal.getId().equals(id)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                    "No puedes bloquearte a ti mismo");
        }
        usuarioService.eliminarUsuario(id, motivo);
        auditService.registrar("ELIMINAR", "Usuario", id,
                principal.getId(), principal.getUsername(),
                "Usuario bloqueado: " + motivo, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    /**
     * Desbloquea un usuario previamente bloqueado: lo reactiva y limpia
     * los metadatos del bloqueo. Registra la acción en el audit log.
     * PUT /api/v1/admin/usuarios/{id}/desbloquear
     *
     * @param id identificador del usuario a desbloquear
     * @param authentication contexto de seguridad del admin que ejecuta la acción
     * @param request petición HTTP, usada para registrar la IP en el audit log
     * @return 204 No Content
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/usuarios/{id}/desbloquear")
    public ResponseEntity<Void> desbloquearUsuario(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        usuarioService.desbloquearUsuario(id);
        auditService.registrar("DESBLOQUEAR", "Usuario", id,
                principal.getId(), principal.getUsername(),
                "Usuario desbloqueado por admin", request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
    /**
     * Devuelve los últimos 100 registros del audit log.
     * GET /api/v1/admin/audit
     *
     * @return lista de {@link AuditLogDTO} ordenada por fecha descendente
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    public ResponseEntity<List<AuditLogDTO>> verAuditLog() {
        return ResponseEntity.ok(auditService.obtenerUltimos());
    }
    /**
     * Devuelve los últimos 100 registros del audit log de un usuario concreto.
     * GET /api/v1/admin/audit/usuario/{usuarioId}
     *
     * @param usuarioId identificador del admin cuyas acciones se quieren listar
     * @return lista de {@link AuditLogDTO} ordenada por fecha descendente
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit/usuario/{usuarioId}")
    public ResponseEntity<List<AuditLogDTO>> verAuditPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(auditService.obtenerPorUsuario(usuarioId));
    }
}
