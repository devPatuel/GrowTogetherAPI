package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
import com.jordipatuel.GrowTogetherAPI.repository.ConsejoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
/**
 * Servicio de gestión de consejos.
 *
 * Los consejos son recursos globales creados por el admin y publicados
 * en una fecha concreta. Los usuarios solo ven los consejos activos
 * cuya fechaPublicacion sea igual o anterior a hoy.
 */
@Service
public class ConsejoService {
    private final ConsejoRepository consejoRepository;
    @Autowired
    public ConsejoService(ConsejoRepository consejoRepository) {
        this.consejoRepository = consejoRepository;
    }

    /**
     * Persiste un nuevo consejo. La validación de fechaPublicacion única
     * está pendiente de implementar.
     */
    public Consejo crearConsejo(Consejo consejo) {
        return consejoRepository.save(consejo);
    }
    /**
     * Devuelve todos los consejos sin filtrar (uso admin).
     */
    public List<Consejo> obtenerTodos() {
        return consejoRepository.findAll();
    }
    /**
     * Devuelve los consejos activos con fechaPublicacion igual o anterior a hoy.
     * Es el endpoint que consume el usuario final.
     */
    public List<Consejo> obtenerConsejosVisibles() {
        return consejoRepository.findConsejosActivosYPublicados(LocalDate.now());
    }
    /**
     * Busca un consejo por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public Consejo obtenerPorId(Integer id) {
        return consejoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Consejo no encontrado con ID " + id));
    }
    /**
     * Reemplaza todos los campos del consejo con los datos recibidos.
     */
    public Consejo actualizarConsejo(Integer id, Consejo consejoDatos) {
        Consejo existente = obtenerPorId(id);
        existente.setTitulo(consejoDatos.getTitulo());
        existente.setDescripcion(consejoDatos.getDescripcion());
        existente.setFechaPublicacion(consejoDatos.getFechaPublicacion());
        existente.setActivo(consejoDatos.isActivo());
        return consejoRepository.save(existente);
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
}
