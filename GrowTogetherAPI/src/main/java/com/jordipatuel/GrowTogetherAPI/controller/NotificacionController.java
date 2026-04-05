package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.NotificacionCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.NotificacionDTO;
import com.jordipatuel.GrowTogetherAPI.model.Notificacion;
import com.jordipatuel.GrowTogetherAPI.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping(Config.API_URL + "/notificaciones")
public class NotificacionController {
    private final NotificacionService notificacionService;
    @Autowired
    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }
    @PreAuthorize("@habitoService.isOwner(#dto.habitoId, authentication.principal.id)")
    @PostMapping
    public ResponseEntity<NotificacionDTO> crearNotificacion(@Valid @RequestBody NotificacionCreateDTO dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setHoraProgramada(dto.getHoraProgramada());
        notificacion.setFrecuencia(dto.getFrecuencia());
        notificacion.setActiva(dto.isActiva());
        Notificacion saved = notificacionService.crearNotificacion(notificacion, dto.getHabitoId());
        return new ResponseEntity<>(mapToDTO(saved), HttpStatus.CREATED);
    }
    @PreAuthorize("@habitoService.isOwner(#habitoId, authentication.principal.id)")
    @GetMapping("/habito/{habitoId}")
    public ResponseEntity<List<NotificacionDTO>> listarPorHabito(@PathVariable Integer habitoId) {
        List<NotificacionDTO> notificaciones = notificacionService.obtenerPorHabito(habitoId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificaciones);
    }
    @PreAuthorize("@notificacionService.isOwner(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<NotificacionDTO> actualizarNotificacion(
            @PathVariable Integer id,
            @Valid @RequestBody NotificacionCreateDTO dto) {
        Notificacion datos = new Notificacion();
        datos.setMensaje(dto.getMensaje());
        datos.setHoraProgramada(dto.getHoraProgramada());
        datos.setFrecuencia(dto.getFrecuencia());
        datos.setActiva(dto.isActiva());
        Notificacion updated = notificacionService.actualizarNotificacion(id, datos);
        return ResponseEntity.ok(mapToDTO(updated));
    }
    @PreAuthorize("@notificacionService.isOwner(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Integer id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
    private NotificacionDTO mapToDTO(Notificacion n) {
        return new NotificacionDTO(
                n.getId(),
                n.getMensaje(),
                n.getHoraProgramada(),
                n.getFrecuencia(),
                n.isActiva(),
                n.getHabito().getId()
        );
    }
}
