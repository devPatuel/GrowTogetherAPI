package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.RegistroHabitoDTO;
import com.jordipatuel.GrowTogetherAPI.dto.RegistroHabitoHistorialDTO;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroHabitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de registros de hábitos.
 *
 * Un {@link RegistroHabito} representa el estado de un hábito en un día concreto
 * (COMPLETADO, PENDIENTE o NO_COMPLETADO). Este servicio centraliza las consultas
 * sobre ese historial y la lógica de relleno de días sin registro.
 *
 * Las consultas expuestas a los controllers devuelven DTOs. {@code rellenarNoCompletados}
 * es una operación interna invocada desde otros services; acepta un ID de hábito
 * y resuelve la entidad internamente a través del repositorio.
 */
@Service
public class RegistroHabitoService {
    private final RegistroHabitoRepository registroHabitoRepository;
    private final HabitoRepository habitoRepository;
    @Autowired
    public RegistroHabitoService(RegistroHabitoRepository registroHabitoRepository,
                                  HabitoRepository habitoRepository) {
        this.registroHabitoRepository = registroHabitoRepository;
        this.habitoRepository = habitoRepository;
    }

    /**
     * Devuelve todos los registros de la base de datos sin filtrar.
     */
    public List<RegistroHabitoDTO> obtenerTodos() {
        return registroHabitoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve todos los registros de un usuario.
     */
    public List<RegistroHabitoDTO> obtenerPorUsuario(Long usuarioId) {
        return registroHabitoRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve los registros de un hábito concreto para un usuario.
     */
    public List<RegistroHabitoDTO> obtenerPorHabitoYUsuario(Integer habitoId, Long usuarioId) {
        return registroHabitoRepository.findByHabito_IdAndUsuario_Id(habitoId, usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve el registro de un hábito en una fecha exacta, si existe.
     */
    public Optional<RegistroHabitoDTO> obtenerPorFecha(Integer habitoId, Long usuarioId, LocalDate fecha) {
        return registroHabitoRepository.findByHabito_IdAndUsuario_IdAndFecha(habitoId, usuarioId, fecha)
                .map(this::toDTO);
    }
    /**
     * Devuelve todos los registros de un usuario en un rango de fechas.
     */
    public List<RegistroHabitoDTO> obtenerPorRangoFechas(Long usuarioId, LocalDate start, LocalDate end) {
        return registroHabitoRepository.findByUsuario_IdAndFechaBetween(usuarioId, start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve el historial de un hábito en un rango de fechas ordenado descendente.
     * Si no se indica fechaFin, usa hoy. Si no se indica fechaInicio, usa los últimos 30 días.
     * Devuelve el DTO simplificado (solo fecha y estado) usado por el heatmap.
     */
    public List<RegistroHabitoHistorialDTO> obtenerHistorialHabito(Integer habitoId, Long usuarioId,
                                                                    LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }
        if (fechaInicio == null) {
            fechaInicio = fechaFin.minusDays(30);
        }
        return registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(habitoId, usuarioId, fechaInicio, fechaFin)
                .stream()
                .map(this::toHistorialDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta cuántos hábitos están marcados como COMPLETADO hoy en toda la plataforma.
     * Usado por el endpoint de métricas del admin.
     */
    public long contarCompletadosHoy() {
        return registroHabitoRepository.countByEstadoAndFecha(EstadoHabito.COMPLETADO, LocalDate.now());
    }

    /**
     * Rellena con NO_COMPLETADO los días sin registro de un hábito,
     * desde su fechaInicio hasta ayer. Los días que ya tienen registro se ignoran.
     * Para hábitos PERSONALIZADO solo se marcan los días programados en diasSemana.
     * Llamado por {@link HabitoScheduledService} cada noche y de forma lazy al consultar historial.
     */
    @Transactional
    public void rellenarNoCompletados(Integer habitoId) {
        Habito habito = habitoRepository.findById(habitoId).orElse(null);
        if (habito == null) return;

        LocalDate desde = habito.getFechaInicio();
        if (desde == null) return;
        LocalDate ayer = LocalDate.now().minusDays(1);
        if (desde.isAfter(ayer)) return;

        // Para PERSONALIZADO: calcular los días de la semana que aplican
        Set<DayOfWeek> dowProgramados = null;
        if (habito.getFrecuencia() == Frecuencia.PERSONALIZADO
                && habito.getDiasSemana() != null
                && !habito.getDiasSemana().isEmpty()) {
            dowProgramados = habito.getDiasSemana().stream()
                    .map(d -> DayOfWeek.of(d.ordinal() + 1))
                    .collect(Collectors.toSet());
        }
        final Set<DayOfWeek> diasAplicables = dowProgramados;

        // Obtener fechas que ya tienen registro
        List<RegistroHabito> existentes = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(
                        habito.getId(), habito.getUsuario().getId(), desde, ayer);
        Set<LocalDate> fechasConRegistro = existentes.stream()
                .map(RegistroHabito::getFecha)
                .collect(Collectors.toSet());

        // Crear NO_COMPLETADO solo para días aplicables sin registro
        for (LocalDate dia = desde; !dia.isAfter(ayer); dia = dia.plusDays(1)) {
            if (diasAplicables != null && !diasAplicables.contains(dia.getDayOfWeek())) {
                continue; // Saltar días no programados en hábitos PERSONALIZADO
            }
            if (!fechasConRegistro.contains(dia)) {
                RegistroHabito registro = new RegistroHabito();
                registro.setHabito(habito);
                registro.setUsuario(habito.getUsuario());
                registro.setFecha(dia);
                registro.setEstado(EstadoHabito.NO_COMPLETADO);
                registroHabitoRepository.save(registro);
            }
        }
    }

    /**
     * Convierte la entidad {@link RegistroHabito} al DTO completo.
     */
    private RegistroHabitoDTO toDTO(RegistroHabito r) {
        return new RegistroHabitoDTO(
                r.getId(),
                r.getFecha(),
                r.getEstado(),
                r.getUsuario() != null ? r.getUsuario().getId() : null,
                r.getHabito() != null ? r.getHabito().getId() : null
        );
    }

    /**
     * Convierte la entidad {@link RegistroHabito} al DTO simplificado de historial
     * (solo fecha y estado, usado por el heatmap).
     */
    private RegistroHabitoHistorialDTO toHistorialDTO(RegistroHabito r) {
        return new RegistroHabitoHistorialDTO(r.getFecha(), r.getEstado());
    }
}
