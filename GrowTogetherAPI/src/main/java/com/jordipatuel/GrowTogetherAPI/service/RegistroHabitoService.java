package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroHabitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class RegistroHabitoService {
    private final RegistroHabitoRepository registroHabitoRepository;
    @Autowired
    public RegistroHabitoService(RegistroHabitoRepository registroHabitoRepository) {
        this.registroHabitoRepository = registroHabitoRepository;
    }
    public List<RegistroHabito> obtenerTodos() {
        return registroHabitoRepository.findAll();
    }
    public List<RegistroHabito> obtenerPorUsuario(Long usuarioId) {
        return registroHabitoRepository.findByUsuario_Id(usuarioId);
    }
    public List<RegistroHabito> obtenerPorHabitoYUsuario(Integer habitoId, Long usuarioId) {
        return registroHabitoRepository.findByHabito_IdAndUsuario_Id(habitoId, usuarioId);
    }
    public Optional<RegistroHabito> obtenerPorFecha(Integer habitoId, Long usuarioId, LocalDate fecha) {
        return registroHabitoRepository.findByHabito_IdAndUsuario_IdAndFecha(habitoId, usuarioId, fecha);
    }
    public List<RegistroHabito> obtenerPorRangoFechas(Long usuarioId, LocalDate start, LocalDate end) {
        return registroHabitoRepository.findByUsuario_IdAndFechaBetween(usuarioId, start, end);
    }
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

    public long contarCompletadosHoy() {
        return registroHabitoRepository.countByEstadoAndFecha(EstadoHabito.COMPLETADO, LocalDate.now());
    }

    @Transactional
    public void rellenarNoCompletados(Habito habito) {
        LocalDate desde = habito.getFechaInicio();
        if (desde == null) return;
        LocalDate ayer = LocalDate.now().minusDays(1);
        if (desde.isAfter(ayer)) return;

        // Obtener fechas que ya tienen registro
        List<RegistroHabito> existentes = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(
                        habito.getId(), habito.getUsuario().getId(), desde, ayer);
        Set<LocalDate> fechasConRegistro = existentes.stream()
                .map(RegistroHabito::getFecha)
                .collect(Collectors.toSet());

        // Crear NO_COMPLETADO para los días sin registro
        for (LocalDate dia = desde; !dia.isAfter(ayer); dia = dia.plusDays(1)) {
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
