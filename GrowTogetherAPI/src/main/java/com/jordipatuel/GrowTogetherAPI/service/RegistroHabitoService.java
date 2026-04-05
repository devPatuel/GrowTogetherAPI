package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroHabitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
}
