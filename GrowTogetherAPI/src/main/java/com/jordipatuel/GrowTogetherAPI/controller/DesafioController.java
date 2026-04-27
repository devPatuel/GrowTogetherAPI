package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
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
/**
 * Controlador de gestión de desafíos.
 *
 * Permite crear desafíos, ver los disponibles, unirse a ellos,
 * consultar el ranking de participantes y eliminarlos.
 * Todos los endpoints requieren JWT. Solo el creador del desafío puede eliminarlo.
 *
 * Todos los endpoints consumen y devuelven DTOs: el mapeo a/desde las entidades
 * vive en {@link DesafioService} y {@link ParticipacionDesafioService}.
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
        DesafioDTO saved = desafioService.crearDesafio(dto, principal.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    /**
     * Devuelve los desafíos activos (fecha de fin posterior a ahora).
     * GET /api/v1/desafios/activos
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/activos")
    public ResponseEntity<List<DesafioDTO>> verRetosDisponibles() {
        return ResponseEntity.ok(desafioService.obtenerDesafiosActivos());
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
        ParticipacionDesafioDTO participacion = participacionDesafioService.unirseADesafio(id, principal.getId());
        return new ResponseEntity<>(participacion, HttpStatus.CREATED);
    }
    /**
     * Devuelve el ranking de participantes del desafío ordenado por puntos.
     * GET /api/v1/desafios/{id}/ranking
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<ParticipacionDesafioDTO>> verRanking(@PathVariable Integer id) {
        return ResponseEntity.ok(participacionDesafioService.obtenerRanking(id));
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
        return ResponseEntity.ok(desafioService.obtenerPorId(id));
    }
}
