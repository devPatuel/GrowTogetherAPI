package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoDTO;
import com.jordipatuel.GrowTogetherAPI.dto.RegistroHabitoHistorialDTO;
import com.jordipatuel.GrowTogetherAPI.service.HabitoService;
import com.jordipatuel.GrowTogetherAPI.service.RegistroHabitoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
/**
 * Controlador de gestión de hábitos.
 *
 * Expone el CRUD de hábitos, las acciones de completar/descompletar,
 * el progreso y el historial. Todos los endpoints requieren JWT.
 * Los endpoints que modifican o consultan un hábito concreto verifican via @PreAuthorize
 * que el usuario autenticado es el propietario del hábito antes de ejecutar la acción.
 *
 * Todos los endpoints consumen y devuelven DTOs: el mapeo a/desde la entidad
 * {@code Habito} y los cálculos derivados (completadoHoy, progresoMensual)
 * viven en {@link HabitoService}.
 */
@RestController
@RequestMapping(Config.API_URL + "/habitos")
public class HabitoController {
    private final HabitoService habitoService;
    private final RegistroHabitoService registroHabitoService;
    /**
     * Inyecta los servicios necesarios para la gestión de hábitos y su historial.
     *
     * @param habitoService servicio principal de hábitos
     * @param registroHabitoService servicio de registros y rellenos automáticos
     */
    @Autowired
    public HabitoController(HabitoService habitoService, RegistroHabitoService registroHabitoService) {
        this.habitoService = habitoService;
        this.registroHabitoService = registroHabitoService;
    }

    /**
     * Crea un nuevo hábito para el usuario autenticado.
     * POST /api/v1/habitos
     *
     * @param dto datos del nuevo hábito
     * @param authentication contexto de seguridad para extraer el propietario
     * @return el hábito creado con código 201
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<HabitoDTO> crearHabito(
            @Valid @RequestBody HabitoCreateDTO dto,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        HabitoDTO nuevoHabito = habitoService.crearHabito(dto, principal.getId());
        return new ResponseEntity<>(nuevoHabito, HttpStatus.CREATED);
    }
    /**
     * Lista los hábitos activos del usuario. Acepta fecha opcional para saber
     * si el hábito estaba completado en un día concreto (por defecto hoy).
     * GET /api/v1/habitos/usuario/{usuarioId}
     *
     * @param usuarioId ID del usuario propietario
     * @param fecha fecha de referencia para calcular completadoHoy (opcional, por defecto hoy)
     * @return lista de hábitos activos del usuario
     */
    @PreAuthorize("isAuthenticated() and #usuarioId == authentication.principal.id")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HabitoDTO>> listarHabitosUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(habitoService.obtenerHabitosPorUsuario(usuarioId, fecha));
    }
    /**
     * Edita los campos del hábito. Solo accesible por el propietario.
     * PUT /api/v1/habitos/{id}
     *
     * @param id ID del hábito a editar
     * @param dto datos nuevos del hábito
     * @param authentication contexto de seguridad para extraer el propietario
     * @return el hábito actualizado
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<HabitoDTO> editarHabito(
            @PathVariable Integer id,
            @Valid @RequestBody HabitoCreateDTO dto,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(habitoService.editarHabito(id, dto, principal.getId()));
    }
    /**
     * Elimina (soft delete) el hábito. Solo accesible por el propietario.
     * DELETE /api/v1/habitos/{id}
     *
     * @param id ID del hábito a eliminar
     * @return 204 No Content
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabito(@PathVariable Integer id) {
        habitoService.eliminarHabito(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Marca el hábito como completado en la fecha indicada (por defecto hoy).
     * Solo accesible por el propietario.
     * POST /api/v1/habitos/{id}/completar
     *
     * @param id ID del hábito a completar
     * @param fecha fecha del registro (opcional, por defecto hoy)
     * @param authentication contexto de seguridad para extraer el propietario
     * @return el hábito con racha y métricas actualizadas
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PostMapping("/{id}/completar")
    public ResponseEntity<HabitoDTO> completarHabito(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(habitoService.completarHabito(id, principal.getId(), fecha));
    }
    /**
     * Revierte el hábito a PENDIENTE en la fecha indicada (por defecto hoy).
     * Solo accesible por el propietario.
     * POST /api/v1/habitos/{id}/descompletar
     *
     * @param id ID del hábito a descompletar
     * @param fecha fecha del registro a revertir (opcional, por defecto hoy)
     * @param authentication contexto de seguridad para extraer el propietario
     * @return el hábito con racha y métricas recalculadas
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PostMapping("/{id}/descompletar")
    public ResponseEntity<HabitoDTO> descompletarHabito(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(habitoService.descompletarHabito(id, principal.getId(), fecha));
    }
    /**
     * Devuelve el progreso actual del hábito (rachaActual y rachaMaxima).
     * Solo accesible por el propietario.
     * GET /api/v1/habitos/{id}/progreso
     *
     * @param id ID del hábito
     * @param authentication contexto de seguridad para extraer el propietario
     * @return el hábito con las métricas de progreso
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}/progreso")
    public ResponseEntity<HabitoDTO> obtenerProgreso(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(habitoService.obtenerProgreso(id, principal.getId()));
    }
    /**
     * Devuelve el historial del hábito en un rango de fechas.
     * Antes de devolver los datos, rellena los días sin registro como NO_COMPLETADO (lazy fill).
     * Solo accesible por el propietario.
     * GET /api/v1/habitos/{id}/historial
     *
     * @param id ID del hábito
     * @param fechaInicio fecha inicial del rango (opcional)
     * @param fechaFin fecha final del rango (opcional)
     * @param authentication contexto de seguridad para extraer el propietario
     * @return lista de registros del hábito en el rango
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<RegistroHabitoHistorialDTO>> obtenerHistorial(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        registroHabitoService.rellenarNoCompletados(id);
        return ResponseEntity.ok(registroHabitoService
                .obtenerHistorialHabito(id, principal.getId(), fechaInicio, fechaFin));
    }
}
