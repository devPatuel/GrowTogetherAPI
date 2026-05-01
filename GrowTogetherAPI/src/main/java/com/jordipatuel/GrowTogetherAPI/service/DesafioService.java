package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.DiaSemana;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.model.enums.TipoHabito;
import com.jordipatuel.GrowTogetherAPI.repository.DesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de desafíos.
 *
 * Un desafío es un reto compartido entre amigos con fecha de inicio y fin,
 * frecuencia (DIARIO/PERSONALIZADO), tipo (POSITIVO/NEGATIVO), icono y descripción.
 *
 * Este servicio gestiona el ciclo de vida del desafío (CRUD) y delega el seguimiento
 * diario y el ranking en {@link ParticipacionDesafioService}.
 *
 * Recibe y devuelve DTOs: los Controllers nunca ven la entidad {@link Desafio}.
 */
@Service
public class DesafioService {
    private final DesafioRepository desafioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipacionDesafioService participacionDesafioService;

    @Autowired
    public DesafioService(DesafioRepository desafioRepository,
                          UsuarioRepository usuarioRepository,
                          @Lazy ParticipacionDesafioService participacionDesafioService) {
        this.desafioRepository = desafioRepository;
        this.usuarioRepository = usuarioRepository;
        this.participacionDesafioService = participacionDesafioService;
    }

    /**
     * Crea un desafío validando fechas y asigna el creador.
     * Inscribe automáticamente al creador como participante e invita a todos los
     * usuarios indicados en {@code participantesIds}.
     */
    @Transactional
    public DesafioDTO crearDesafio(DesafioCreateDTO dto, Long creadorId) {
        if (dto.getFechaFin().before(dto.getFechaInicio())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (dto.getFechaFin().before(new Date())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La fecha de fin no puede ser en el pasado");
        }
        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID " + creadorId));
        Desafio desafio = toEntity(dto);
        desafio.setCreador(creador);
        Desafio guardado = desafioRepository.save(desafio);

        participacionDesafioService.crearParticipacionSiNoExiste(guardado, creadorId);
        if (dto.getParticipantesIds() != null) {
            for (Long invitadoId : dto.getParticipantesIds()) {
                if (!invitadoId.equals(creadorId)) {
                    participacionDesafioService.crearParticipacionSiNoExiste(guardado, invitadoId);
                }
            }
        }
        return obtenerPorId(guardado.getId());
    }

    /**
     * Edita los campos del desafío con los valores del DTO recibido.
     * Solo el creador puede editar (controlado en el controller con {@code @PreAuthorize}).
     */
    @Transactional
    public DesafioDTO editarDesafio(Integer id, DesafioCreateDTO dto) {
        Desafio desafio = obtenerEntidadPorId(id);
        boolean modificado = false;
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            desafio.setNombre(dto.getNombre());
            modificado = true;
        }
        if (dto.getDescripcion() != null && !dto.getDescripcion().isBlank()) {
            desafio.setDescripcion(dto.getDescripcion());
            modificado = true;
        }
        if (dto.getObjetivo() != null) {
            desafio.setObjetivo(dto.getObjetivo());
            modificado = true;
        }
        if (dto.getFechaInicio() != null) {
            desafio.setFechaInicio(dto.getFechaInicio());
            modificado = true;
        }
        if (dto.getFechaFin() != null) {
            desafio.setFechaFin(dto.getFechaFin());
            modificado = true;
        }
        if (dto.getFrecuencia() != null) {
            desafio.setFrecuencia(Frecuencia.valueOf(dto.getFrecuencia()));
            modificado = true;
        }
        if (dto.getDiasSemana() != null) {
            desafio.setDiasSemana(parseDias(dto.getDiasSemana()));
            modificado = true;
        }
        if (dto.getTipo() != null) {
            desafio.setTipo(TipoHabito.valueOf(dto.getTipo()));
            modificado = true;
        }
        if (dto.getIcono() != null) {
            desafio.setIcono(dto.getIcono());
            modificado = true;
        }
        if (modificado) {
            desafio = desafioRepository.save(desafio);
        }
        if (dto.getParticipantesIds() != null) {
            for (Long invitadoId : dto.getParticipantesIds()) {
                participacionDesafioService.crearParticipacionSiNoExiste(desafio, invitadoId);
            }
        }
        return toDTO(desafio, true);
    }

    /**
     * Devuelve todos los desafíos sin filtrar.
     */
    public List<DesafioDTO> obtenerTodos() {
        return desafioRepository.findAll().stream()
                .map(d -> toDTO(d, false))
                .collect(Collectors.toList());
    }

    /**
     * Busca un desafío por ID. Devuelve el detalle completo con participantes embebidos.
     */
    public DesafioDTO obtenerPorId(Integer id) {
        return toDTO(obtenerEntidadPorId(id), true);
    }

    /**
     * Devuelve los desafíos activos cuya fecha de fin es posterior a ahora.
     * Usado para listar desafíos públicos (futuro: explorar desafíos comunitarios).
     */
    public List<DesafioDTO> obtenerDesafiosActivos() {
        return desafioRepository.findByFechaFinAfterAndActivoTrue(new Date()).stream()
                .map(d -> toDTO(d, false))
                .collect(Collectors.toList());
    }

    /**
     * Devuelve los desafíos en los que participa el usuario (como creador o participante).
     * Incluye finalizados.
     */
    public List<DesafioDTO> obtenerMisDesafios(Long usuarioId) {
        return desafioRepository.findMisDesafios(usuarioId).stream()
                .map(d -> toDTO(d, true))
                .collect(Collectors.toList());
    }

    /**
     * Cuenta los desafíos activos. Usado por el endpoint de métricas del admin.
     */
    public long contarDesafiosActivos() {
        return desafioRepository.countByFechaFinAfterAndActivoTrue(new Date());
    }

    /**
     * Desactiva el desafío (soft delete).
     */
    public void eliminarDesafio(Integer id) {
        Desafio desafio = obtenerEntidadPorId(id);
        desafio.setActivo(false);
        desafioRepository.save(desafio);
    }

    /**
     * Verifica si el usuario indicado es el creador del desafío.
     * Usado por {@code @PreAuthorize} en el controller para control de acceso.
     */
    public boolean isCreator(Integer desafioId, Long usuarioId) {
        Desafio desafio = desafioRepository.findById(desafioId).orElse(null);
        if (desafio == null) {
            return false;
        }
        return desafio.getCreador().getId().equals(usuarioId);
    }

    private Desafio obtenerEntidadPorId(Integer id) {
        return desafioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Desafío no encontrado con ID " + id));
    }

    /**
     * Convierte la entidad {@link Desafio} al DTO de respuesta.
     * Cuando {@code conParticipantes} es true, incluye la lista ordenada por puntos
     * con posición, racha, foto y completadoHoy de cada participante.
     */
    private DesafioDTO toDTO(Desafio desafio, boolean conParticipantes) {
        DesafioDTO dto = new DesafioDTO();
        dto.setId(desafio.getId());
        dto.setNombre(desafio.getNombre());
        dto.setDescripcion(desafio.getDescripcion());
        dto.setObjetivo(desafio.getObjetivo());
        dto.setFechaInicio(desafio.getFechaInicio());
        dto.setFechaFin(desafio.getFechaFin());
        dto.setActivo(desafio.isActivo());
        dto.setFrecuencia(desafio.getFrecuencia() != null ? desafio.getFrecuencia().name() : "DIARIO");
        dto.setDiasSemana(desafio.getDiasSemana() != null
                ? desafio.getDiasSemana().stream().map(DiaSemana::name).collect(Collectors.toSet())
                : new HashSet<>());
        dto.setTipo(desafio.getTipo() != null ? desafio.getTipo().name() : "POSITIVO");
        dto.setIcono(desafio.getIcono());
        if (desafio.getCreador() != null) {
            dto.setCreadorId(desafio.getCreador().getId());
            dto.setCreadorNombre(desafio.getCreador().getNombre());
        }
        if (conParticipantes) {
            List<ParticipacionDesafioDTO> participantes = participacionDesafioService.obtenerPorDesafio(desafio.getId());
            dto.setParticipantes(participantes);
        }
        return dto;
    }

    /**
     * Construye una nueva entidad {@link Desafio} a partir del DTO de creación.
     */
    private Desafio toEntity(DesafioCreateDTO dto) {
        Desafio desafio = new Desafio();
        desafio.setNombre(dto.getNombre());
        desafio.setDescripcion(dto.getDescripcion());
        desafio.setObjetivo(dto.getObjetivo() != null ? dto.getObjetivo() : dto.getDescripcion());
        desafio.setFechaInicio(dto.getFechaInicio());
        desafio.setFechaFin(dto.getFechaFin());
        if (dto.getFrecuencia() != null) {
            desafio.setFrecuencia(Frecuencia.valueOf(dto.getFrecuencia()));
        }
        desafio.setDiasSemana(parseDias(dto.getDiasSemana()));
        if (dto.getTipo() != null) {
            desafio.setTipo(TipoHabito.valueOf(dto.getTipo()));
        }
        desafio.setIcono(dto.getIcono());
        return desafio;
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
