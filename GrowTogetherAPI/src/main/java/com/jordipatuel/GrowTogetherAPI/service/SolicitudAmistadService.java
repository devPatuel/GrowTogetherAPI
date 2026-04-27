package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.SolicitudAmistadDTO;
import com.jordipatuel.GrowTogetherAPI.exception.BadRequestException;
import com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException;
import com.jordipatuel.GrowTogetherAPI.model.SolicitudAmistad;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoSolicitud;
import com.jordipatuel.GrowTogetherAPI.repository.SolicitudAmistadRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Servicio de gestión del flujo de solicitudes de amistad.
 *
 * Encapsula las reglas de negocio: no puedes enviarte solicitud a ti mismo,
 * no puedes tener dos pendientes con la misma persona, no puedes solicitar
 * a alguien que ya es tu amigo. Al aceptar una solicitud delega en
 * {@link UsuarioService#agregarAmigo} para crear la relación bidireccional.
 *
 * Expone una API basada en DTOs: los Controllers no ven la entidad JPA.
 */
@Service
public class SolicitudAmistadService {
    private final SolicitudAmistadRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public SolicitudAmistadService(
            SolicitudAmistadRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Envía una solicitud de amistad del remitente al destinatario.
     * Valida: no enviarse a sí mismo, no duplicar pendiente en cualquier dirección,
     * no enviar si ya son amigos.
     */
    @Transactional
    public SolicitudAmistadDTO enviarSolicitud(Long remitenteId, Long destinatarioId) {
        if (remitenteId.equals(destinatarioId)) {
            throw new BadRequestException("No puedes enviarte una solicitud a ti mismo");
        }
        Usuario remitente = obtenerEntidad(remitenteId);
        Usuario destinatario = obtenerEntidad(destinatarioId);

        if (remitente.getAmigos() != null && remitente.getAmigos().contains(destinatario)) {
            throw new BadRequestException("Ya sois amigos");
        }
        if (solicitudRepository.findByRemitenteIdAndDestinatarioIdAndEstado(
                remitenteId, destinatarioId, EstadoSolicitud.PENDIENTE).isPresent()) {
            throw new BadRequestException("Ya has enviado una solicitud a este usuario");
        }
        if (solicitudRepository.findByRemitenteIdAndDestinatarioIdAndEstado(
                destinatarioId, remitenteId, EstadoSolicitud.PENDIENTE).isPresent()) {
            throw new BadRequestException("Este usuario ya te ha enviado una solicitud pendiente");
        }

        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setRemitente(remitente);
        solicitud.setDestinatario(destinatario);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaEnvio(LocalDateTime.now());
        return toDTO(solicitudRepository.save(solicitud));
    }

    /**
     * Acepta una solicitud pendiente. Solo el destinatario puede aceptarla.
     * Crea la relación de amistad bidireccional vía {@link UsuarioService#agregarAmigo}.
     */
    @Transactional
    public SolicitudAmistadDTO aceptar(Long solicitudId, Long userActualId) {
        SolicitudAmistad solicitud = obtenerSolicitud(solicitudId);
        if (!solicitud.getDestinatario().getId().equals(userActualId)) {
            throw new BadRequestException("Solo el destinatario puede aceptar la solicitud");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud ya ha sido respondida");
        }
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        SolicitudAmistad guardada = solicitudRepository.save(solicitud);
        usuarioService.agregarAmigo(solicitud.getRemitente().getId(), solicitud.getDestinatario().getId());
        return toDTO(guardada);
    }

    /**
     * Rechaza una solicitud pendiente. Solo el destinatario puede rechazarla.
     */
    @Transactional
    public SolicitudAmistadDTO rechazar(Long solicitudId, Long userActualId) {
        SolicitudAmistad solicitud = obtenerSolicitud(solicitudId);
        if (!solicitud.getDestinatario().getId().equals(userActualId)) {
            throw new BadRequestException("Solo el destinatario puede rechazar la solicitud");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud ya ha sido respondida");
        }
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        return toDTO(solicitudRepository.save(solicitud));
    }

    /**
     * Cancela una solicitud que aún está pendiente. Solo el remitente puede cancelarla.
     * Se elimina la fila para permitir volver a enviarla más adelante.
     */
    @Transactional
    public void cancelar(Long solicitudId, Long userActualId) {
        SolicitudAmistad solicitud = obtenerSolicitud(solicitudId);
        if (!solicitud.getRemitente().getId().equals(userActualId)) {
            throw new BadRequestException("Solo el remitente puede cancelar la solicitud");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("Solo se pueden cancelar solicitudes pendientes");
        }
        solicitudRepository.delete(solicitud);
    }

    /** Devuelve las solicitudes pendientes recibidas por el usuario. */
    public List<SolicitudAmistadDTO> listarRecibidasPendientes(Long userId) {
        return solicitudRepository.findByDestinatarioIdAndEstado(userId, EstadoSolicitud.PENDIENTE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Devuelve las solicitudes pendientes enviadas por el usuario. */
    public List<SolicitudAmistadDTO> listarEnviadasPendientes(Long userId) {
        return solicitudRepository.findByRemitenteIdAndEstado(userId, EstadoSolicitud.PENDIENTE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Usuario obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private SolicitudAmistad obtenerSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));
    }

    /**
     * Convierte la entidad {@link SolicitudAmistad} al DTO incluyendo datos visuales
     * (nombre y foto) de remitente y destinatario para evitar llamadas adicionales.
     */
    private SolicitudAmistadDTO toDTO(SolicitudAmistad s) {
        Usuario r = s.getRemitente();
        Usuario d = s.getDestinatario();
        return new SolicitudAmistadDTO(
                s.getId(),
                r.getId(),
                r.getNombre(),
                r.getFoto(),
                d.getId(),
                d.getNombre(),
                d.getFoto(),
                s.getEstado(),
                s.getFechaEnvio(),
                s.getFechaRespuesta()
        );
    }
}
