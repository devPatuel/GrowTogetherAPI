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
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Controlador de gestión de hábitos.
 *
 * Expone el CRUD de hábitos, las acciones de completar/descompletar,
 * el progreso y el historial. Todos los endpoints requieren JWT.
 * Los endpoints que modifican o consultan un hábito concreto verifican via @PreAuthorize
 * que el usuario autenticado es el propietario del hábito antes de ejecutar la acción.
 */
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

    /**
     * Crea un nuevo hábito para el usuario autenticado.
     * POST /api/v1/habitos
     */
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
    /**
     * Lista los hábitos activos del usuario. Acepta fecha opcional para saber
     * si el hábito estaba completado en un día concreto (por defecto hoy).
     * GET /api/v1/habitos/usuario/{usuarioId}
     */
    @PreAuthorize("isAuthenticated() and #usuarioId == authentication.principal.id")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HabitoDTO>> listarHabitosUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<HabitoDTO> habitos = habitoService.obtenerHabitosPorUsuario(usuarioId)
                .stream().map(h -> mapToDTO(h, usuarioId, fecha)).collect(Collectors.toList());
        return ResponseEntity.ok(habitos);
    }
    /**
     * Edita los campos del hábito. Solo accesible por el propietario.
     * PUT /api/v1/habitos/{id}
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<HabitoDTO> editarHabito(
            @PathVariable Integer id,
            @Valid @RequestBody HabitoCreateDTO dto,
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
    /**
     * Elimina (soft delete) el hábito. Solo accesible por el propietario.
     * DELETE /api/v1/habitos/{id}
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
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PostMapping("/{id}/completar")
    public ResponseEntity<HabitoDTO> completarHabito(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habito = habitoService.completarHabito(id, principal.getId(), fecha);
        return ResponseEntity.ok(mapToDTO(habito, principal.getId(), fecha));
    }
    /**
     * Revierte el hábito a PENDIENTE en la fecha indicada (por defecto hoy).
     * Solo accesible por el propietario.
     * POST /api/v1/habitos/{id}/descompletar
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @PostMapping("/{id}/descompletar")
    public ResponseEntity<HabitoDTO> descompletarHabito(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habito = habitoService.descompletarHabito(id, principal.getId(), fecha);
        return ResponseEntity.ok(mapToDTO(habito, principal.getId(), fecha));
    }
    /**
     * Devuelve el progreso actual del hábito (rachaActual y rachaMaxima).
     * Solo accesible por el propietario.
     * GET /api/v1/habitos/{id}/progreso
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}/progreso")
    public ResponseEntity<HabitoDTO> obtenerProgreso(
            @PathVariable Integer id,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito progreso = habitoService.obtenerProgreso(id);
        return ResponseEntity.ok(mapToDTO(progreso, principal.getId()));
    }
    /**
     * Devuelve el historial del hábito en un rango de fechas.
     * Antes de devolver los datos, rellena los días sin registro como NO_COMPLETADO (lazy fill).
     * Solo accesible por el propietario.
     * GET /api/v1/habitos/{id}/historial
     */
    @PreAuthorize("@habitoService.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<RegistroHabitoHistorialDTO>> obtenerHistorial(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
        Habito habito = habitoService.obtenerPorId(id);
        registroHabitoService.rellenarNoCompletados(habito);
        List<RegistroHabitoHistorialDTO> historial = registroHabitoService
                .obtenerHistorialHabito(id, principal.getId(), fechaInicio, fechaFin)
                .stream()
                .map(r -> new RegistroHabitoHistorialDTO(r.getFecha(), r.getEstado()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(historial);
    }

    /**
     * Convierte un hábito a DTO usando hoy como fecha de referencia para completadoHoy.
     */
    private HabitoDTO mapToDTO(Habito habito, Long usuarioId) {
        return mapToDTO(habito, usuarioId, null);
    }

    /**
     * Convierte un hábito a DTO indicando la fecha de referencia para completadoHoy
     * y calculando el progreso mensual real.
     */
    private HabitoDTO mapToDTO(Habito habito, Long usuarioId, LocalDate fecha) {
        boolean completadoHoy = habitoService.estaCompletadoEnFecha(habito.getId(), usuarioId, fecha);
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
        dto.setFechaInicio(habito.getFechaInicio());
        dto.setProgresoMensual(calcularProgresoMensual(habito, usuarioId));
        return dto;
    }

    /**
     * Calcula el porcentaje de días completados en el mes actual respecto a los días esperados.
     * Para hábitos DIARIO cuenta todos los días del mes hasta hoy.
     * Para hábitos PERSONALIZADO cuenta solo los días programados en diasSemana.
     */
    private double calcularProgresoMensual(Habito habito, Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        // Dias esperados este mes hasta hoy
        int diasEsperados = 0;
        if (habito.getFrecuencia() == Frecuencia.DIARIO) {
            diasEsperados = hoy.getDayOfMonth();
        } else {
            Set<DiaSemana> dias = habito.getDiasSemana();
            if (dias == null || dias.isEmpty()) return 0;
            for (LocalDate d = inicioMes; !d.isAfter(hoy); d = d.plusDays(1)) {
                DayOfWeek dow = d.getDayOfWeek();
                String diaEnum = switch (dow) {
                    case MONDAY -> "LUNES";
                    case TUESDAY -> "MARTES";
                    case WEDNESDAY -> "MIERCOLES";
                    case THURSDAY -> "JUEVES";
                    case FRIDAY -> "VIERNES";
                    case SATURDAY -> "SABADO";
                    case SUNDAY -> "DOMINGO";
                };
                if (dias.stream().anyMatch(ds -> ds.name().equals(diaEnum))) {
                    diasEsperados++;
                }
            }
        }

        if (diasEsperados == 0) return 0;

        // Dias completados reales
        List<RegistroHabito> registros = registroHabitoService
                .obtenerHistorialHabito(habito.getId(), usuarioId, inicioMes, hoy);
        long completados = registros.stream()
                .filter(r -> r.getEstado() == EstadoHabito.COMPLETADO)
                .count();

        return Math.min((double) completados / diasEsperados, 1.0);
    }

    /**
     * Convierte un Set de Strings con nombres de días a un Set de {@link DiaSemana}.
     */
    private Set<DiaSemana> parseDias(Set<String> dias) {
        if (dias == null || dias.isEmpty()) return new HashSet<>();
        return dias.stream()
                .map(DiaSemana::valueOf)
                .collect(Collectors.toSet());
    }
}
