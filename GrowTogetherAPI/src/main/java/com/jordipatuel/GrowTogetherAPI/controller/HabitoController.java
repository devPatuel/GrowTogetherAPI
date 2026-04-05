package com.jordipatuel.GrowTogetherAPI.controller;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.Config;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoDTO;
import com.jordipatuel.GrowTogetherAPI.dto.RegistroHabitoHistorialDTO;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.DiaSemana;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.model.enums.TipoHabito;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@RestController
@RequestMapping(Config.API_URL + "/habitos")
public class HabitoController {
    private final HabitoService habitoService;
    private final RegistroHabitoService registroHabitoService;
    @Autowired
    public HabitoController(HabitoService habitoService, RegistroHabitoService registroHabitoService) {
        this.habitoService = habitoService;
        this.registroHabitoService = registroHabitoService;
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<HabitoDTO> crearHabito(
            @Valid @RequestBody HabitoCreateDTO dto,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habito = new Habito();
        habito.setNombre(dto.getNombre());
        habito.setDescripcion(dto.getDescripcion());
        if (dto.getFrecuencia() != null) {
            habito.setFrecuencia(Frecuencia.valueOf(dto.getFrecuencia()));
        }
        habito.setDiasSemana(parseDias(dto.getDiasSemana()));
        if (dto.getTipo() != null) {
            habito.setTipo(TipoHabito.valueOf(dto.getTipo()));
        }
        habito.setIcono(dto.getIcono());
        Habito nuevoHabito = habitoService.crearHabito(habito, principal.getId());
        return new ResponseEntity<>(mapToDTO(nuevoHabito, principal.getId()), HttpStatus.CREATED);
    }
    @PreAuthorize("isAuthenticated() and #usuarioId == authentication.principal.id")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HabitoDTO>> listarHabitosUsuario(@PathVariable Long usuarioId) {
        List<HabitoDTO> habitos = habitoService.obtenerHabitosPorUsuario(usuarioId)
                .stream().map(h -> mapToDTO(h, usuarioId)).collect(Collectors.toList());
        return ResponseEntity.ok(habitos);
    }
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<HabitoDTO> editarHabito(
            @PathVariable Integer id,
            @RequestBody HabitoCreateDTO dto,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habitoInfo = new Habito();
        habitoInfo.setNombre(dto.getNombre());
        habitoInfo.setDescripcion(dto.getDescripcion());
        if (dto.getFrecuencia() != null) {
            habitoInfo.setFrecuencia(Frecuencia.valueOf(dto.getFrecuencia()));
        }
        habitoInfo.setDiasSemana(parseDias(dto.getDiasSemana()));
        if (dto.getTipo() != null) {
            habitoInfo.setTipo(TipoHabito.valueOf(dto.getTipo()));
        }
        habitoInfo.setIcono(dto.getIcono());
        Habito actualizado = habitoService.editarHabito(id, habitoInfo);
        return ResponseEntity.ok(mapToDTO(actualizado, principal.getId()));
    }
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabito(@PathVariable Integer id) {
        habitoService.eliminarHabito(id);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PostMapping("/{id}/completar")
    public ResponseEntity<HabitoDTO> completarHabito(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habito = habitoService.completarHabito(id, principal.getId());
        return ResponseEntity.ok(mapToDTO(habito, principal.getId()));
    }
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PostMapping("/{id}/descompletar")
    public ResponseEntity<HabitoDTO> descompletarHabito(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habito = habitoService.descompletarHabito(id, principal.getId());
        return ResponseEntity.ok(mapToDTO(habito, principal.getId()));
    }
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}/progreso")
    public ResponseEntity<HabitoDTO> obtenerProgreso(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito progreso = habitoService.obtenerProgreso(id);
        return ResponseEntity.ok(mapToDTO(progreso, principal.getId()));
    }
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<RegistroHabitoHistorialDTO>> obtenerHistorial(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        List<RegistroHabitoHistorialDTO> historial = registroHabitoService
                .obtenerHistorialHabito(id, principal.getId(), fechaInicio, fechaFin)
                .stream()
                .map(r -> new RegistroHabitoHistorialDTO(r.getFecha(), r.getEstado()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(historial);
    }

    private HabitoDTO mapToDTO(Habito habito, Long usuarioId) {
        boolean completadoHoy = habitoService.estaCompletadoHoy(habito.getId(), usuarioId);
        HabitoDTO dto = new HabitoDTO();
        dto.setId(habito.getId());
        dto.setNombre(habito.getNombre());
        dto.setDescripcion(habito.getDescripcion());
        dto.setRachaActual(habito.getRachaActual());
        dto.setRachaMaxima(habito.getRachaMaxima());
        dto.setUsuarioId(habito.getUsuario() != null ? habito.getUsuario().getId() : null);
        dto.setCompletadoHoy(completadoHoy);
        dto.setFrecuencia(habito.getFrecuencia() != null ? habito.getFrecuencia().name() : "DIARIO");
        dto.setDiasSemana(habito.getDiasSemana() != null
                ? habito.getDiasSemana().stream().map(DiaSemana::name).collect(Collectors.toSet())
                : new HashSet<>());
        dto.setTipo(habito.getTipo() != null ? habito.getTipo().name() : "POSITIVO");
        dto.setIcono(habito.getIcono());
        return dto;
    }

    private Set<DiaSemana> parseDias(Set<String> dias) {
        if (dias == null || dias.isEmpty()) return new HashSet<>();
        return dias.stream()
                .map(DiaSemana::valueOf)
                .collect(Collectors.toSet());
    }
}
