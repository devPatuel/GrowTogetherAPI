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
    /**
     * Inyecta el repositorio de consejos.
     *
     * @param consejoRepository repositorio JPA de {@link Consejo}
     */
    @Autowired
    public ConsejoService(ConsejoRepository consejoRepository) {
        this.consejoRepository = consejoRepository;
    }

    /**
     * Persiste un nuevo consejo a partir del DTO de creación.
     * La fechaPublicacion es opcional: si se indica, se valida que no exista
     * otro consejo con esa misma fecha. Múltiples consejos sin fecha conviven
     * sin restricción.
     * El creadorId se recibe por parámetro (extraído del JWT del admin en el controller).
     *
     * @param dto datos del consejo a crear
     * @param creadorId ID del admin que crea el consejo
     * @return el consejo creado con su ID asignado
     */
    public ConsejoDTO crearConsejo(ConsejoCreateDTO dto, Long creadorId) {
        if (dto.getFechaPublicacion() != null) {
            validarFechaDisponible(dto.getFechaPublicacion(), null);
        }
        Consejo consejo = toEntity(dto);
        consejo.setCreadorId(creadorId);
        return toDTO(consejoRepository.save(consejo));
    }
    /**
     * Devuelve todos los consejos sin filtrar (uso admin).
     *
     * @return lista completa de {@link ConsejoDTO}
     */
    public List<ConsejoDTO> obtenerTodos() {
        return consejoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve los consejos activos con fechaPublicacion igual o anterior a hoy.
     * Es el endpoint que consume el usuario final.
     *
     * @return lista de {@link ConsejoDTO} visibles para el usuario final
     */
    public List<ConsejoDTO> obtenerConsejosVisibles() {
        return consejoRepository.findConsejosActivosYPublicados(LocalDate.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Busca un consejo por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     *
     * @param id identificador del consejo a buscar
     * @return el {@link ConsejoDTO} encontrado
     */
    public ConsejoDTO obtenerPorId(Integer id) {
        return toDTO(obtenerEntidadPorId(id));
    }
    /**
     * Actualiza los campos del consejo con los datos del DTO recibido.
     * Mantiene el creadorId original. Si se cambia la fechaPublicacion,
     * valida que no choque con otro consejo distinto.
     *
     * @param id identificador del consejo a actualizar
     * @param dto datos nuevos del consejo
     * @return el {@link ConsejoDTO} actualizado
     */
    public ConsejoDTO actualizarConsejo(Integer id, ConsejoCreateDTO dto) {
        Consejo existente = obtenerEntidadPorId(id);
        if (dto.getFechaPublicacion() != null
                && !dto.getFechaPublicacion().equals(existente.getFechaPublicacion())) {
            validarFechaDisponible(dto.getFechaPublicacion(), id);
        }
        existente.setTitulo(dto.getTitulo());
        existente.setDescripcion(dto.getDescripcion());
        existente.setFechaPublicacion(dto.getFechaPublicacion());
        if (dto.getActivo() != null) {
            existente.setActivo(dto.getActivo());
        }
        return toDTO(consejoRepository.save(existente));
    }

    /**
     * Devuelve el consejo activo asignado a la fecha de hoy. Si no hay,
     * devuelve un Optional vacío y el controller responde 204 No Content.
     *
     * @return Optional con el {@link ConsejoDTO} del día o vacío si no hay
     */
    public java.util.Optional<ConsejoDTO> obtenerConsejoDeHoy() {
        return consejoRepository.findByFechaPublicacionAndActivoTrue(LocalDate.now())
                .map(this::toDTO);
    }

    /**
     * Lanza BadRequestException si la fecha ya está ocupada por otro consejo.
     * El parámetro idActual se usa al editar para excluir el propio consejo de la comprobación.
     *
     * @param fecha fecha a validar
     * @param idActual id del consejo en edición, o {@code null} si se está creando
     */
    private void validarFechaDisponible(LocalDate fecha, Integer idActual) {
        consejoRepository.findByFechaPublicacion(fecha).ifPresent(c -> {
            if (idActual == null || !c.getId().equals(idActual)) {
                throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                        "Ya existe un consejo publicado en la fecha " + fecha);
            }
        });
    }
    /**
     * Elimina físicamente el consejo. Los consejos no usan soft delete.
     *
     * @param id identificador del consejo a eliminar
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
     *
     * @param id identificador del consejo
     * @return la entidad {@link Consejo} encontrada
     */
    private Consejo obtenerEntidadPorId(Integer id) {
        return consejoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Consejo no encontrado con ID " + id));
    }

    /**
     * Convierte la entidad {@link Consejo} al DTO de respuesta.
     *
     * @param consejo entidad origen
     * @return el {@link ConsejoDTO} equivalente
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
     * No asigna id ni creadorId: eso se resuelve en el método que llama.
     * fechaPublicacion puede quedarse null (consejo sin fecha asignada).
     *
     * @param dto DTO con los datos del nuevo consejo
     * @return la entidad {@link Consejo} sin persistir
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
