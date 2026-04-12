package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.Notificacion;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
/**
 * Servicio de gestión de notificaciones de hábitos.
 *
 * Una notificación está siempre asociada a un hábito y define cuándo y con qué
 * frecuencia se debe recordar al usuario que lo complete. El CRUD de notificaciones
 * push al dispositivo no está conectado aún; este servicio gestiona solo la persistencia.
 */
@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final HabitoRepository habitoRepository;
    @Autowired
    public NotificacionService(NotificacionRepository notificacionRepository, HabitoRepository habitoRepository) {
        this.notificacionRepository = notificacionRepository;
        this.habitoRepository = habitoRepository;
    }

    /**
     * Crea una notificación asociándola al hábito indicado.
     * Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si el hábito no existe.
     */
    public Notificacion crearNotificacion(Notificacion notificacion, Integer habitoId) {
        Habito habito = habitoRepository.findById(habitoId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + habitoId));
        notificacion.setHabito(habito);
        return notificacionRepository.save(notificacion);
    }
    /**
     * Devuelve todas las notificaciones de un hábito concreto.
     */
    public List<Notificacion> obtenerPorHabito(Integer habitoId) {
        return notificacionRepository.findByHabitoId(habitoId);
    }
    /**
     * Devuelve todas las notificaciones de los hábitos de un usuario.
     */
    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByHabitoUsuarioId(usuarioId);
    }
    /**
     * Busca una notificación por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public Notificacion obtenerPorId(Integer id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Notificación no encontrada con ID " + id));
    }
    /**
     * Actualiza los campos informados de una notificación existente.
     * El campo {@code activa} siempre se sobreescribe con el valor recibido.
     */
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
    /**
     * Elimina físicamente la notificación. A diferencia de hábitos y usuarios,
     * las notificaciones no usan soft delete.
     */
    public void eliminarNotificacion(Integer id) {
        if (!notificacionRepository.existsById(id)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Notificación no encontrada con ID " + id);
        }
        notificacionRepository.deleteById(id);
    }
    /**
     * Verifica si el usuario indicado es el propietario de la notificación
     * navegando por la relación notificacion → habito → usuario.
     * Usado por {@code @PreAuthorize} en el controller para control de acceso.
     */
    public boolean isOwner(Integer notificacionId, Long usuarioId) {
        return notificacionRepository.findById(notificacionId)
                .map(n -> n.getHabito().getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }
}
