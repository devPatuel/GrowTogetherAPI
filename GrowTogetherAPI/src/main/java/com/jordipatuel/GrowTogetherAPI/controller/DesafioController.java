package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.RegistroDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.service.DesafioService;
import com.jordipatuel.GrowTogetherAPI.service.ParticipacionDesafioService;
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
 * Controlador de gestión de desafíos.
 *
 * Permite crear desafíos, listar los del usuario, ver el detalle con participantes,
 * marcar como hecho un día concreto, consultar el historial para la gráfica
 * y gestionar participaciones (unirse, abandonar, invitar).
 *
 * Todos los endpoints requieren JWT. Solo el creador puede editar o eliminar.
 */
@RestController
@RequestMapping(Config.API_URL + "/desafios")
public class DesafioController {
    private final DesafioService desafioService;
    private final ParticipacionDesafioService participacionDesafioService;

    /**
     * Inyecta los servicios necesarios para gestionar desafíos y participaciones.
     *
     * @param desafioService servicio principal de desafíos
     * @param participacionDesafioService servicio de participaciones, ranking e historial
     */
    @Autowired
    public DesafioController(DesafioService desafioService, ParticipacionDesafioService participacionDesafioService) {
        this.desafioService = desafioService;
        this.participacionDesafioService = participacionDesafioService;
    }

    /**
     * Crea un nuevo desafío con el usuario autenticado como creador.
     * El creador queda inscrito automáticamente como participante. Se pueden invitar
     * amigos en el mismo request mediante {@code participantesIds}.
     * POST /api/v1/desafios
     *
     * @param dto datos del nuevo desafío
     * @param authentication contexto de seguridad para extraer el creador
     * @return el desafío creado con código 201
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<DesafioDTO> crearDesafio(@Valid @RequestBody DesafioCreateDTO dto, Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        DesafioDTO saved = desafioService.crearDesafio(dto, principal.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Edita un desafío existente. Solo accesible por el creador.
     * PUT /api/v1/desafios/{id}
     *
     * @param id ID del desafío a editar
     * @param dto datos nuevos del desafío
     * @return el desafío actualizado
     */
    @PreAuthorize("@desafioService.isCreator(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<DesafioDTO> editarDesafio(@PathVariable Integer id, @Valid @RequestBody DesafioCreateDTO dto) {
        return ResponseEntity.ok(desafioService.editarDesafio(id, dto));
    }

    /**
     * Devuelve los desafíos en los que participa el usuario autenticado.
     * Incluye los que creó y aquellos a los que se ha unido. Activos y finalizados.
     * GET /api/v1/desafios/mios
     *
     * @param authentication contexto de seguridad para extraer el usuario
     * @return lista de desafíos del usuario
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mios")
    public ResponseEntity<List<DesafioDTO>> verMisDesafios(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(desafioService.obtenerMisDesafios(principal.getId()));
    }

    /**
     * Devuelve los desafíos activos (fecha de fin posterior a ahora).
     * Pensado para "explorar desafíos comunitarios" (uso futuro).
     * GET /api/v1/desafios/activos
     *
     * @return lista de desafíos activos
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/activos")
    public ResponseEntity<List<DesafioDTO>> verRetosDisponibles() {
        return ResponseEntity.ok(desafioService.obtenerDesafiosActivos());
    }

    /**
     * Inscribe al usuario autenticado en el desafío indicado.
     * POST /api/v1/desafios/{id}/unirse
     *
     * @param id ID del desafío al que unirse
     * @param authentication contexto de seguridad para extraer el usuario
     * @return la participación creada con código 201
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/unirse")
    public ResponseEntity<ParticipacionDesafioDTO> unirseADesafio(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        ParticipacionDesafioDTO participacion = participacionDesafioService.unirseADesafio(id, principal.getId());
        return new ResponseEntity<>(participacion, HttpStatus.CREATED);
    }

    /**
     * Marca como ABANDONADO al usuario autenticado en el desafío indicado.
     * El histórico de registros se conserva.
     * DELETE /api/v1/desafios/{id}/abandonar
     *
     * @param id ID del desafío a abandonar
     * @param authentication contexto de seguridad para extraer el usuario
     * @return la participación marcada como ABANDONADO
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/abandonar")
    public ResponseEntity<ParticipacionDesafioDTO> abandonarDesafio(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(participacionDesafioService.abandonarDesafio(id, principal.getId()));
    }

    /**
     * Marca el desafío como completado por el usuario autenticado en la fecha indicada
     * (por defecto hoy). Recalcula racha y puntos.
     * POST /api/v1/desafios/{id}/completar
     *
     * @param id ID del desafío a completar
     * @param fecha fecha del registro (opcional, por defecto hoy)
     * @param authentication contexto de seguridad para extraer el usuario
     * @return la participación con racha y puntos actualizados
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/completar")
    public ResponseEntity<ParticipacionDesafioDTO> completarDesafio(
            @PathVariable Integer id,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(participacionDesafioService.completarDesafio(id, principal.getId(), fecha));
    }

    /**
     * Revierte a PENDIENTE el día indicado para el usuario autenticado.
     * POST /api/v1/desafios/{id}/descompletar
     *
     * @param id ID del desafío a descompletar
     * @param fecha fecha del registro a revertir (opcional, por defecto hoy)
     * @param authentication contexto de seguridad para extraer el usuario
     * @return la participación con racha y puntos recalculados
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/descompletar")
    public ResponseEntity<ParticipacionDesafioDTO> descompletarDesafio(
            @PathVariable Integer id,
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(participacionDesafioService.descompletarDesafio(id, principal.getId(), fecha));
    }

    /**
     * Devuelve el ranking de participantes del desafío ordenado por puntos.
     * GET /api/v1/desafios/{id}/ranking
     *
     * @param id ID del desafío
     * @return lista de participaciones ordenadas por puntos descendente
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<ParticipacionDesafioDTO>> verRanking(@PathVariable Integer id) {
        return ResponseEntity.ok(participacionDesafioService.obtenerRanking(id));
    }

    /**
     * Devuelve el historial de registros diarios de todos los participantes.
     * Usado por la gráfica multilínea de evolución de puntos.
     * GET /api/v1/desafios/{id}/historial
     *
     * @param id ID del desafío
     * @param fechaInicio fecha inicial del rango (opcional)
     * @param fechaFin fecha final del rango (opcional)
     * @return lista de registros diarios para la gráfica
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<RegistroDesafioDTO>> verHistorial(
            @PathVariable Integer id,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(participacionDesafioService.obtenerHistorial(id, fechaInicio, fechaFin));
    }

    /**
     * Elimina (soft delete) el desafío. Solo accesible por el creador.
     * DELETE /api/v1/desafios/{id}
     *
     * @param id ID del desafío a eliminar
     * @return 204 No Content
     */
    @PreAuthorize("@desafioService.isCreator(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarDesafio(@PathVariable Integer id) {
        desafioService.eliminarDesafio(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Devuelve el detalle de un desafío concreto con participantes embebidos.
     * GET /api/v1/desafios/{id}
     *
     * @param id ID del desafío
     * @return el desafío con la lista de participantes
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DesafioDTO> verDesafio(@PathVariable Integer id) {
        return ResponseEntity.ok(desafioService.obtenerPorId(id));
    }
}
