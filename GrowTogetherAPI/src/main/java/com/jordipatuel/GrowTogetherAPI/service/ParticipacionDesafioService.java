package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.ParticipacionDesafio;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import com.jordipatuel.GrowTogetherAPI.repository.DesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.ParticipacionDesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de participaciones en desafíos.
 *
 * Una participación vincula a un usuario con un desafío y registra su estado
 * (ACTIVO/SUPERADO/ABANDONADO) y los puntos ganados dentro de ese desafío.
 *
 * Devuelve {@link ParticipacionDesafioDTO} en todas sus consultas para que los
 * Controllers no vean la entidad JPA.
 */
@Service
public class ParticipacionDesafioService {
    private final ParticipacionDesafioRepository participacionDesafioRepository;
    private final DesafioRepository desafioRepository;
    private final UsuarioRepository usuarioRepository;
    @Autowired
    public ParticipacionDesafioService(
            ParticipacionDesafioRepository participacionDesafioRepository,
            DesafioRepository desafioRepository,
            UsuarioRepository usuarioRepository) {
        this.participacionDesafioRepository = participacionDesafioRepository;
        this.desafioRepository = desafioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Inscribe al usuario en el desafío validando que no esté ya apuntado
     * y que el desafío no haya finalizado. Inicializa el estado a ACTIVO y puntos a 0.
     */
    public ParticipacionDesafioDTO unirseADesafio(Integer desafioId, Long usuarioId) {
        if (participacionDesafioRepository.findByDesafioIdAndUsuarioId(desafioId, usuarioId).isPresent()) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("El usuario ya está participando en este desafío");
        }
        Desafio desafio = desafioRepository.findById(desafioId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Desafío no encontrado con ID " + desafioId));
        if (desafio.getFechaFin().before(new Date())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("El desafío ya ha finalizado");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID " + usuarioId));
        ParticipacionDesafio participacion = new ParticipacionDesafio();
        participacion.setDesafio(desafio);
        participacion.setUsuario(usuario);
        participacion.setFechaInscripcion(new Date());
        participacion.setEstadoProgreso(EstadoProgreso.ACTIVO);
        participacion.setPuntosGanadosEnDesafio(0);
        return toDTO(participacionDesafioRepository.save(participacion));
    }
    /**
     * Devuelve los participantes de un desafío ordenados por puntos de mayor a menor.
     * Usado para construir el ranking/leaderboard.
     */
    public List<ParticipacionDesafioDTO> obtenerRanking(Integer desafioId) {
        return participacionDesafioRepository.findByDesafioIdOrderByPuntosGanadosEnDesafioDesc(desafioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve todos los desafíos en los que participa un usuario.
     */
    public List<ParticipacionDesafioDTO> obtenerPorUsuario(Long usuarioId) {
        return participacionDesafioRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve todas las participaciones de un desafío concreto.
     */
    public List<ParticipacionDesafioDTO> obtenerPorDesafio(Integer desafioId) {
        return participacionDesafioRepository.findByDesafioId(desafioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve todas las participaciones sin filtrar.
     */
    public List<ParticipacionDesafioDTO> obtenerTodos() {
        return participacionDesafioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte la entidad {@link ParticipacionDesafio} al DTO de respuesta.
     * Incluye el nombre del usuario para evitar llamadas adicionales al endpoint de perfil.
     */
    private ParticipacionDesafioDTO toDTO(ParticipacionDesafio p) {
        ParticipacionDesafioDTO dto = new ParticipacionDesafioDTO();
        dto.setId(p.getId());
        dto.setFechaInscripcion(p.getFechaInscripcion());
        dto.setEstadoProgreso(p.getEstadoProgreso());
        dto.setPuntosGanadosEnDesafio(p.getPuntosGanadosEnDesafio());
        dto.setUsuarioId(p.getUsuario().getId());
        dto.setUsuarioNombre(p.getUsuario().getNombre());
        dto.setDesafioId(p.getDesafio().getId());
        return dto;
    }
}
