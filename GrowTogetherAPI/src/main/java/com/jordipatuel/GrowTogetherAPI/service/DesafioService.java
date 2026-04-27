package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioDTO;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.repository.DesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de desafíos.
 *
 * Un desafío es un reto con fecha de inicio y fin al que los usuarios pueden unirse.
 * Este servicio gestiona su ciclo de vida: creación con validación de fechas,
 * consulta y baja lógica.
 *
 * Recibe y devuelve DTOs: los Controllers nunca ven la entidad {@link Desafio}.
 */
@Service
public class DesafioService {
    private final DesafioRepository desafioRepository;
    private final UsuarioRepository usuarioRepository;
    @Autowired
    public DesafioService(DesafioRepository desafioRepository, UsuarioRepository usuarioRepository) {
        this.desafioRepository = desafioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un desafío a partir del DTO validando que fechaFin sea posterior
     * a fechaInicio y no esté en el pasado. Asigna el usuario indicado como creador.
     */
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
        return toDTO(desafioRepository.save(desafio));
    }
    /**
     * Devuelve todos los desafíos sin filtrar.
     */
    public List<DesafioDTO> obtenerTodos() {
        return desafioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Busca un desafío por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public DesafioDTO obtenerPorId(Integer id) {
        return toDTO(obtenerEntidadPorId(id));
    }
    /**
     * Devuelve los desafíos activos cuya fecha de fin es posterior a ahora.
     */
    public List<DesafioDTO> obtenerDesafiosActivos() {
        return desafioRepository.findByFechaFinAfterAndActivoTrue(new Date()).stream()
                .map(this::toDTO)
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

    /**
     * Helper interno: recupera la entidad {@link Desafio} para operaciones internas
     * del servicio que necesitan manipular la referencia JPA.
     */
    private Desafio obtenerEntidadPorId(Integer id) {
        return desafioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Desafío no encontrado con ID " + id));
    }

    /**
     * Convierte la entidad {@link Desafio} al DTO de respuesta.
     * Incluye el nombre del creador para que el cliente pueda mostrarlo
     * sin necesidad de hacer una segunda llamada al endpoint de perfil.
     */
    private DesafioDTO toDTO(Desafio desafio) {
        DesafioDTO dto = new DesafioDTO();
        dto.setId(desafio.getId());
        dto.setNombre(desafio.getNombre());
        dto.setObjetivo(desafio.getObjetivo());
        dto.setFechaInicio(desafio.getFechaInicio());
        dto.setFechaFin(desafio.getFechaFin());
        if (desafio.getCreador() != null) {
            dto.setCreadorId(desafio.getCreador().getId());
            dto.setCreadorNombre(desafio.getCreador().getNombre());
        }
        return dto;
    }

    /**
     * Construye una nueva entidad {@link Desafio} a partir del DTO de creación.
     * No asigna creador: eso se resuelve en el método que llama a partir del ID del usuario autenticado.
     */
    private Desafio toEntity(DesafioCreateDTO dto) {
        Desafio desafio = new Desafio();
        desafio.setNombre(dto.getNombre());
        desafio.setObjetivo(dto.getObjetivo());
        desafio.setFechaInicio(dto.getFechaInicio());
        desafio.setFechaFin(dto.getFechaFin());
        return desafio;
    }
}
