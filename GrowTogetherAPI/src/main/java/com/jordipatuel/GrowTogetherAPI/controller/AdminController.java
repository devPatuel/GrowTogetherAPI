package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.AuditLogDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoDTO;
import com.jordipatuel.GrowTogetherAPI.model.AuditLog;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
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
import java.util.stream.Collectors;
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> verMetricas() {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalUsuarios", usuarioService.obtenerTodos().size());
        metricas.put("habitosCompletadosHoy", registroHabitoService.contarCompletadosHoy());
        metricas.put("desafiosActivos", desafioService.contarDesafiosActivos());
        return ResponseEntity.ok(metricas);
    }
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/recursos")
    public ResponseEntity<ConsejoDTO> subirRecurso(
            @Valid @RequestBody ConsejoCreateDTO dto,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Consejo consejo = new Consejo();
        consejo.setTitulo(dto.getTitulo());
        consejo.setDescripcion(dto.getDescripcion());
        consejo.setFechaPublicacion(dto.getFechaPublicacion());
        if (dto.getActivo() != null) {
            consejo.setActivo(dto.getActivo());
        }
        consejo.setCreadorId(principal.getId());
        Consejo guardado = consejoService.crearConsejo(consejo);
        auditService.registrar("CREAR", "Consejo", Long.valueOf(guardado.getId()),
                principal.getId(), principal.getUsername(),
                "Recurso creado: " + guardado.getTitulo(), request.getRemoteAddr());
        return new ResponseEntity<>(mapToDTO(guardado), HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recursos")
    public ResponseEntity<List<ConsejoDTO>> listarTodosLosRecursos() {
        List<ConsejoDTO> recursos = consejoService.obtenerTodos().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(recursos);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/recursos/{id}")
    public ResponseEntity<ConsejoDTO> editarRecurso(
            @PathVariable Integer id,
            @Valid @RequestBody ConsejoCreateDTO dto,
            Authentication authentication,
            HttpServletRequest request) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Consejo consejo = new Consejo();
        consejo.setTitulo(dto.getTitulo());
        consejo.setDescripcion(dto.getDescripcion());
        consejo.setFechaPublicacion(dto.getFechaPublicacion());
        if (dto.getActivo() != null) {
            consejo.setActivo(dto.getActivo());
        }
        Consejo actualizado = consejoService.actualizarConsejo(id, consejo);
        auditService.registrar("EDITAR", "Consejo", Long.valueOf(id),
                principal.getId(), principal.getUsername(),
                "Recurso editado: " + actualizado.getTitulo(), request.getRemoteAddr());
        return ResponseEntity.ok(mapToDTO(actualizado));
    }
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    public ResponseEntity<List<AuditLogDTO>> verAuditLog() {
        List<AuditLogDTO> logs = auditService.obtenerUltimos().stream()
                .map(this::mapToAuditDTO).collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit/usuario/{usuarioId}")
    public ResponseEntity<List<AuditLogDTO>> verAuditPorUsuario(@PathVariable Long usuarioId) {
        List<AuditLogDTO> logs = auditService.obtenerPorUsuario(usuarioId).stream()
                .map(this::mapToAuditDTO).collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
    private AuditLogDTO mapToAuditDTO(AuditLog log) {
        return new AuditLogDTO(
                log.getId(), log.getAccion(), log.getEntidad(), log.getEntidadId(),
                log.getUsuarioId(), log.getUsuarioEmail(), log.getDetalle(),
                log.getIp(), log.getFecha());
    }
    private ConsejoDTO mapToDTO(Consejo consejo) {
        return new ConsejoDTO(
                consejo.getId(),
                consejo.getTitulo(),
                consejo.getDescripcion(),
                consejo.getFechaPublicacion(),
                consejo.isActivo(),
                consejo.getCreadorId()
        );
    }
}
