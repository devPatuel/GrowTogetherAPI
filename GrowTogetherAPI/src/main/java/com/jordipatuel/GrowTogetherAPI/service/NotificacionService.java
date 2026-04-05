package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.Notificacion;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final HabitoRepository habitoRepository;
    @Autowired
    public NotificacionService(NotificacionRepository notificacionRepository, HabitoRepository habitoRepository) {
        this.notificacionRepository = notificacionRepository;
        this.habitoRepository = habitoRepository;
    }
    public Notificacion crearNotificacion(Notificacion notificacion, Integer habitoId) {
        Habito habito = habitoRepository.findById(habitoId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + habitoId));
        notificacion.setHabito(habito);
        return notificacionRepository.save(notificacion);
    }
    public List<Notificacion> obtenerPorHabito(Integer habitoId) {
        return notificacionRepository.findByHabitoId(habitoId);
    }
    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByHabitoUsuarioId(usuarioId);
    }
    public Notificacion obtenerPorId(Integer id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Notificación no encontrada con ID " + id));
    }
    public Notificacion actualizarNotificacion(Integer id, Notificacion datos) {
        Notificacion existente = obtenerPorId(id);
        if (datos.getMensaje() != null && !datos.getMensaje().isBlank()) {
            existente.setMensaje(datos.getMensaje());
        }
        if (datos.getHoraProgramada() != null) {
            existente.setHoraProgramada(datos.getHoraProgramada());
        }
        if (datos.getFrecuencia() != null && !datos.getFrecuencia().isBlank()) {
            existente.setFrecuencia(datos.getFrecuencia());
        }
        existente.setActiva(datos.isActiva());
        return notificacionRepository.save(existente);
    }
    public void eliminarNotificacion(Integer id) {
        if (!notificacionRepository.existsById(id)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Notificación no encontrada con ID " + id);
        }
        notificacionRepository.deleteById(id);
    }
    public boolean isOwner(Integer notificacionId, Long usuarioId) {
        return notificacionRepository.findById(notificacionId)
                .map(n -> n.getHabito().getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }
}
