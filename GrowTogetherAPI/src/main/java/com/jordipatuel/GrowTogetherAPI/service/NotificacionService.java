package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.NotificacionCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.NotificacionDTO;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.Notificacion;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de notificaciones de hábitos.
 *
 * Una notificación está siempre asociada a un hábito y define cuándo y con qué
 * frecuencia se debe recordar al usuario que lo complete. El CRUD de notificaciones
 * push al dispositivo no está conectado aún; este servicio gestiona solo la persistencia.
 *
 * Recibe y devuelve DTOs: los Controllers nunca ven la entidad {@link Notificacion}.
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
     * Crea una notificación a partir del DTO, asociándola al hábito indicado en {@code habitoId}.
     * Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si el hábito no existe.
     */
    public NotificacionDTO crearNotificacion(NotificacionCreateDTO dto) {
        Habito habito = habitoRepository.findById(dto.getHabitoId())
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + dto.getHabitoId()));
        Notificacion notificacion = toEntity(dto);
        notificacion.setHabito(habito);
        return toDTO(notificacionRepository.save(notificacion));
    }
    /**
     * Devuelve todas las notificaciones de un hábito concreto.
     */
    public List<NotificacionDTO> obtenerPorHabito(Integer habitoId) {
        return notificacionRepository.findByHabitoId(habitoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve todas las notificaciones de los hábitos de un usuario.
     */
    public List<NotificacionDTO> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByHabitoUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Busca una notificación por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public NotificacionDTO obtenerPorId(Integer id) {
        return toDTO(obtenerEntidadPorId(id));
    }
    /**
     * Actualiza los campos informados de una notificación existente.
     * El campo {@code activa} siempre se sobreescribe con el valor recibido.
     */
    public NotificacionDTO actualizarNotificacion(Integer id, NotificacionCreateDTO dto) {
        Notificacion existente = obtenerEntidadPorId(id);
        if (dto.getMensaje() != null && !dto.getMensaje().isBlank()) {
            existente.setMensaje(dto.getMensaje());
        }
        if (dto.getHoraProgramada() != null) {
            existente.setHoraProgramada(dto.getHoraProgramada());
        }
        if (dto.getFrecuencia() != null && !dto.getFrecuencia().isBlank()) {
            existente.setFrecuencia(dto.getFrecuencia());
        }
        existente.setActiva(dto.isActiva());
        return toDTO(notificacionRepository.save(existente));
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

    /**
     * Helper interno: recupera la entidad {@link Notificacion} para operaciones
     * internas del servicio que necesitan manipular la referencia JPA.
     */
    private Notificacion obtenerEntidadPorId(Integer id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Notificación no encontrada con ID " + id));
    }

    /**
     * Convierte la entidad {@link Notificacion} al DTO de respuesta.
     */
    private NotificacionDTO toDTO(Notificacion n) {
        return new NotificacionDTO(
                n.getId(),
                n.getMensaje(),
                n.getHoraProgramada(),
                n.getFrecuencia(),
                n.isActiva(),
                n.getHabito().getId()
        );
    }

    /**
     * Construye una nueva entidad {@link Notificacion} a partir del DTO de creación.
     * No asigna el hábito: eso se resuelve en el método que llama tras consultar el repositorio.
     */
    private Notificacion toEntity(NotificacionCreateDTO dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setHoraProgramada(dto.getHoraProgramada());
        notificacion.setFrecuencia(dto.getFrecuencia());
        notificacion.setActiva(dto.isActiva());
        return notificacion;
    }
}
