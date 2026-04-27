package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ConsejoDTO;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
import com.jordipatuel.GrowTogetherAPI.repository.ConsejoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de consejos.
 *
 * Los consejos son recursos globales creados por el admin y publicados
 * en una fecha concreta. Los usuarios solo ven los consejos activos
 * cuya fechaPublicacion sea igual o anterior a hoy.
 *
 * Recibe y devuelve DTOs: los Controllers nunca ven la entidad {@link Consejo}.
 */
@Service
public class ConsejoService {
    private final ConsejoRepository consejoRepository;
    @Autowired
    public ConsejoService(ConsejoRepository consejoRepository) {
        this.consejoRepository = consejoRepository;
    }

    /**
     * Persiste un nuevo consejo a partir del DTO de creación.
     * Si no se indica fechaPublicacion, se asigna la fecha actual.
     * Si no se indica activo, se crea como activo.
     * El creadorId se recibe por parámetro (extraído del JWT del admin en el controller).
     */
    public ConsejoDTO crearConsejo(ConsejoCreateDTO dto, Long creadorId) {
        Consejo consejo = toEntity(dto);
        consejo.setCreadorId(creadorId);
        if (consejo.getFechaPublicacion() == null) {
            consejo.setFechaPublicacion(LocalDate.now());
        }
        return toDTO(consejoRepository.save(consejo));
    }
    /**
     * Devuelve todos los consejos sin filtrar (uso admin).
     */
    public List<ConsejoDTO> obtenerTodos() {
        return consejoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve los consejos activos con fechaPublicacion igual o anterior a hoy.
     * Es el endpoint que consume el usuario final.
     */
    public List<ConsejoDTO> obtenerConsejosVisibles() {
        return consejoRepository.findConsejosActivosYPublicados(LocalDate.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Busca un consejo por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public ConsejoDTO obtenerPorId(Integer id) {
        return toDTO(obtenerEntidadPorId(id));
    }
    /**
     * Actualiza los campos del consejo con los datos del DTO recibido.
     * Mantiene el creadorId original.
     */
    public ConsejoDTO actualizarConsejo(Integer id, ConsejoCreateDTO dto) {
        Consejo existente = obtenerEntidadPorId(id);
        existente.setTitulo(dto.getTitulo());
        existente.setDescripcion(dto.getDescripcion());
        if (dto.getFechaPublicacion() != null) {
            existente.setFechaPublicacion(dto.getFechaPublicacion());
        }
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        return toDTO(consejoRepository.save(existente));
    }
    /**
     * Elimina físicamente el consejo. Los consejos no usan soft delete.
     */
    public void eliminarConsejo(Integer id) {
        if (!consejoRepository.existsById(id)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Consejo no encontrado con ID " + id);
        }
        consejoRepository.deleteById(id);
    }

    /**
     * Helper interno: recupera la entidad {@link Consejo} desde el repositorio
     * para operaciones internas del servicio que necesitan la referencia JPA.
     */
    private Consejo obtenerEntidadPorId(Integer id) {
        return consejoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Consejo no encontrado con ID " + id));
    }

    /**
     * Convierte la entidad {@link Consejo} al DTO de respuesta.
     */
    private ConsejoDTO toDTO(Consejo consejo) {
        return new ConsejoDTO(
                consejo.getId(),
                consejo.getTitulo(),
                consejo.getDescripcion(),
                consejo.getFechaPublicacion(),
                consejo.isActivo(),
                consejo.getCreadorId()
        );
    }

    /**
     * Construye una nueva entidad {@link Consejo} a partir del DTO de creación.
     * No asigna id, creadorId ni activo por defecto: eso se resuelve en el método que llama.
     */
    private Consejo toEntity(ConsejoCreateDTO dto) {
        Consejo consejo = new Consejo();
        consejo.setTitulo(dto.getTitulo());
        consejo.setDescripcion(dto.getDescripcion());
        consejo.setFechaPublicacion(dto.getFechaPublicacion());
        if (dto.getActivo() != null) {
            consejo.setActivo(dto.getActivo());
        }
        return consejo;
    }
}
