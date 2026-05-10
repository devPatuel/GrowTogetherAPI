package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.NotificacionCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.NotificacionDTO;
import com.jordipatuel.GrowTogetherAPI.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * Controlador de gestión de notificaciones de hábitos.
 *
 * Todos los endpoints requieren JWT. La creación y listado verifican que el usuario
 * es propietario del hábito asociado. La edición y eliminación verifican que es
 * propietario de la notificación concreta navegando por notificacion → habito → usuario.
 *
 * Todos los endpoints consumen y devuelven DTOs: el mapeo a/desde la entidad
 * {@code Notificacion} vive en {@link NotificacionService}.
 */
@RestController
@RequestMapping(Config.API_URL + "/notificaciones")
public class NotificacionController {
    private final NotificacionService notificacionService;
    /**
     * Inyecta el servicio de notificaciones.
     *
     * @param notificacionService servicio de gestión de notificaciones
     */
    @Autowired
    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Crea una notificación para el hábito indicado en el DTO.
     * Verifica que el usuario autenticado es propietario del hábito.
     * POST /api/v1/notificaciones
     *
     * @param dto datos de la nueva notificación
     * @return la notificación creada con código 201
     */
    @PreAuthorize("@habitoService.isOwner(#dto.habitoId, authentication.principal.id)")
    @PostMapping
    public ResponseEntity<NotificacionDTO> crearNotificacion(@Valid @RequestBody NotificacionCreateDTO dto) {
        NotificacionDTO saved = notificacionService.crearNotificacion(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    /**
     * Lista las notificaciones de un hábito concreto.
     * Verifica que el usuario autenticado es propietario del hábito.
     * GET /api/v1/notificaciones/habito/{habitoId}
     *
     * @param habitoId ID del hábito
     * @return lista de notificaciones asociadas al hábito
     */
    @PreAuthorize("@habitoService.isOwner(#habitoId, authentication.principal.id)")
    @GetMapping("/habito/{habitoId}")
    public ResponseEntity<List<NotificacionDTO>> listarPorHabito(@PathVariable Integer habitoId) {
        return ResponseEntity.ok(notificacionService.obtenerPorHabito(habitoId));
    }
    /**
     * Lista todas las notificaciones del usuario autenticado, recorriendo todos sus
     * habitos. Pensado para que la app pueda sincronizar y reprogramar las notificaciones
     * locales tras login o reinstalacion sin tener que abrir cada habito uno a uno.
     * GET /api/v1/notificaciones/usuario
     *
     * @param authentication autenticacion de Spring Security con el usuario actual
     * @return lista de notificaciones del usuario
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/usuario")
    public ResponseEntity<List<NotificacionDTO>> listarDelUsuarioAutenticado(Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(principal.getId()));
    }
    /**
     * Actualiza una notificación. Verifica que el usuario es propietario de la notificación.
     * PUT /api/v1/notificaciones/{id}
     *
     * @param id ID de la notificación a actualizar
     * @param dto datos nuevos de la notificación
     * @return la notificación actualizada
     */
    @PreAuthorize("@notificacionService.isOwner(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<NotificacionDTO> actualizarNotificacion(
            @PathVariable Integer id,
            @Valid @RequestBody NotificacionCreateDTO dto) {
        return ResponseEntity.ok(notificacionService.actualizarNotificacion(id, dto));
    }
    /**
     * Elimina una notificación. Verifica que el usuario es propietario de la notificación.
     * DELETE /api/v1/notificaciones/{id}
     *
     * @param id ID de la notificación a eliminar
     * @return 204 No Content
     */
    @PreAuthorize("@notificacionService.isOwner(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Integer id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}
