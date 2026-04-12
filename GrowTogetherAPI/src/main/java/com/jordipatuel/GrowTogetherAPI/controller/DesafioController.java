package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.ParticipacionDesafio;
import com.jordipatuel.GrowTogetherAPI.service.DesafioService;
import com.jordipatuel.GrowTogetherAPI.service.ParticipacionDesafioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Controlador de gestión de desafíos.
 *
 * Permite crear desafíos, ver los disponibles, unirse a ellos,
 * consultar el ranking de participantes y eliminarlos.
 * Todos los endpoints requieren JWT. Solo el creador del desafío puede eliminarlo.
 */
@RestController
@RequestMapping(Config.API_URL + "/desafios")
public class DesafioController {
    private final DesafioService desafioService;
    private final ParticipacionDesafioService participacionDesafioService;
    @Autowired
    public DesafioController(DesafioService desafioService, ParticipacionDesafioService participacionDesafioService) {
        this.desafioService = desafioService;
        this.participacionDesafioService = participacionDesafioService;
    }

    /**
     * Crea un nuevo desafío con el usuario autenticado como creador.
     * POST /api/v1/desafios
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<DesafioDTO> crearDesafio(@Valid @RequestBody DesafioCreateDTO dto, Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Desafio desafio = new Desafio();
        desafio.setNombre(dto.getNombre());
        desafio.setObjetivo(dto.getObjetivo());
        desafio.setFechaInicio(dto.getFechaInicio());
        desafio.setFechaFin(dto.getFechaFin());
        Desafio saved = desafioService.crearDesafio(desafio, principal.getId());
        return new ResponseEntity<>(mapToDTO(saved), HttpStatus.CREATED);
    }
    /**
     * Devuelve los desafíos activos (fecha de fin posterior a ahora).
     * GET /api/v1/desafios/activos
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/activos")
    public ResponseEntity<List<DesafioDTO>> verRetosDisponibles() {
        List<DesafioDTO> desafios = desafioService.obtenerDesafiosActivos()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(desafios);
    }
    /**
     * Inscribe al usuario autenticado en el desafío indicado.
     * POST /api/v1/desafios/{id}/unirse
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/unirse")
    public ResponseEntity<ParticipacionDesafioDTO> unirseADesafio(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        ParticipacionDesafio participacion = participacionDesafioService.unirseADesafio(id, principal.getId());
        return new ResponseEntity<>(mapParticipacionToDTO(participacion), HttpStatus.CREATED);
    }
    /**
     * Devuelve el ranking de participantes del desafío ordenado por puntos.
     * GET /api/v1/desafios/{id}/ranking
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<ParticipacionDesafioDTO>> verRanking(@PathVariable Integer id) {
        List<ParticipacionDesafioDTO> ranking = participacionDesafioService.obtenerRanking(id)
                .stream()
                .map(this::mapParticipacionToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ranking);
    }
    /**
     * Elimina (soft delete) el desafío. Solo accesible por el creador.
     * DELETE /api/v1/desafios/{id}
     */
    @PreAuthorize("@desafioService.isCreator(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarDesafio(@PathVariable Integer id) {
        desafioService.eliminarDesafio(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Devuelve el detalle de un desafío concreto.
     * GET /api/v1/desafios/{id}
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DesafioDTO> verDesafio(@PathVariable Integer id) {
        Desafio desafio = desafioService.obtenerPorId(id);
        return ResponseEntity.ok(mapToDTO(desafio));
    }
    /**
     * Convierte una entidad {@link Desafio} al DTO de respuesta.
     */
    private DesafioDTO mapToDTO(Desafio desafio) {
        DesafioDTO dto = new DesafioDTO();
        dto.setId(desafio.getId());
        dto.setNombre(desafio.getNombre());
        dto.setObjetivo(desafio.getObjetivo());
        dto.setFechaInicio(desafio.getFechaInicio());
        dto.setFechaFin(desafio.getFechaFin());
        if (desafio.getCreador() != null) {
            dto.setCreadorId(desafio.getCreador().getId());
            dto.setCreadorNombre(desafio.getCreador().getNombre());
        }
        return dto;
    }
    /**
     * Convierte una entidad {@link ParticipacionDesafio} al DTO de respuesta.
     */
    private ParticipacionDesafioDTO mapParticipacionToDTO(ParticipacionDesafio p) {
        ParticipacionDesafioDTO dto = new ParticipacionDesafioDTO();
        dto.setId(p.getId());
        dto.setFechaInscripcion(p.getFechaInscripcion());
        dto.setEstadoProgreso(p.getEstadoProgreso());
        dto.setPuntosGanadosEnDesafio(p.getPuntosGanadosEnDesafio());
        dto.setUsuarioId(p.getUsuario().getId());
        dto.setUsuarioNombre(p.getUsuario().getNombre());
        dto.setDesafioId(p.getDesafio().getId());
        return dto;
    }
}
