package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.repository.DesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
/**
 * Servicio de gestión de desafíos.
 *
 * Un desafío es un reto con fecha de inicio y fin al que los usuarios pueden unirse.
 * Este servicio gestiona su ciclo de vida: creación con validación de fechas,
 * consulta y baja lógica.
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
     * Crea un desafío validando que fechaFin sea posterior a fechaInicio y no esté en el pasado.
     * Asigna el usuario indicado como creador.
     */
    public Desafio crearDesafio(Desafio desafio, Long creadorId) {
        if (desafio.getFechaFin().before(desafio.getFechaInicio())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (desafio.getFechaFin().before(new Date())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La fecha de fin no puede ser en el pasado");
        }
        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID " + creadorId));
        desafio.setCreador(creador);
        return desafioRepository.save(desafio);
    }
    /**
     * Devuelve todos los desafíos sin filtrar.
     */
    public List<Desafio> obtenerTodos() {
        return desafioRepository.findAll();
    }
    /**
     * Busca un desafío por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public Desafio obtenerPorId(Integer id) {
        return desafioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Desafío no encontrado con ID " + id));
    }
    /**
     * Devuelve los desafíos activos cuya fecha de fin es posterior a ahora.
     */
    public List<Desafio> obtenerDesafiosActivos() {
        return desafioRepository.findByFechaFinAfterAndActivoTrue(new Date());
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
        Desafio desafio = obtenerPorId(id);
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
}
