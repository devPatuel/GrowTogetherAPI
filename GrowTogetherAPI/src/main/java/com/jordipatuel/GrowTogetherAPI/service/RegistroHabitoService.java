package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
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
 */
@Service
public class RegistroHabitoService {
    private final RegistroHabitoRepository registroHabitoRepository;
    @Autowired
    public RegistroHabitoService(RegistroHabitoRepository registroHabitoRepository) {
        this.registroHabitoRepository = registroHabitoRepository;
    }

    /**
     * Devuelve todos los registros de la base de datos sin filtrar.
     */
    public List<RegistroHabito> obtenerTodos() {
        return registroHabitoRepository.findAll();
    }
    /**
     * Devuelve todos los registros de un usuario.
     */
    public List<RegistroHabito> obtenerPorUsuario(Long usuarioId) {
        return registroHabitoRepository.findByUsuario_Id(usuarioId);
    }
    /**
     * Devuelve los registros de un hábito concreto para un usuario.
     */
    public List<RegistroHabito> obtenerPorHabitoYUsuario(Integer habitoId, Long usuarioId) {
        return registroHabitoRepository.findByHabito_IdAndUsuario_Id(habitoId, usuarioId);
    }
    /**
     * Devuelve el registro de un hábito en una fecha exacta, si existe.
     */
    public Optional<RegistroHabito> obtenerPorFecha(Integer habitoId, Long usuarioId, LocalDate fecha) {
        return registroHabitoRepository.findByHabito_IdAndUsuario_IdAndFecha(habitoId, usuarioId, fecha);
    }
    /**
     * Devuelve todos los registros de un usuario en un rango de fechas.
     */
    public List<RegistroHabito> obtenerPorRangoFechas(Long usuarioId, LocalDate start, LocalDate end) {
        return registroHabitoRepository.findByUsuario_IdAndFechaBetween(usuarioId, start, end);
    }
    /**
     * Devuelve el historial de un hábito en un rango de fechas ordenado descendente.
     * Si no se indica fechaFin, usa hoy. Si no se indica fechaInicio, usa los últimos 30 días.
     */
    public List<RegistroHabito> obtenerHistorialHabito(Integer habitoId, Long usuarioId,
                                                        LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }
        if (fechaInicio == null) {
            fechaInicio = fechaFin.minusDays(30);
        }
        return registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(habitoId, usuarioId, fechaInicio, fechaFin);
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
    public void rellenarNoCompletados(Habito habito) {
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
}
