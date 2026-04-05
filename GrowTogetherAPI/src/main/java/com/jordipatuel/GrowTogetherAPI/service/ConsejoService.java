package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Consejo;
import com.jordipatuel.GrowTogetherAPI.repository.ConsejoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
@Service
public class ConsejoService {
    private final ConsejoRepository consejoRepository;
    @Autowired
    public ConsejoService(ConsejoRepository consejoRepository) {
        this.consejoRepository = consejoRepository;
    }
    public Consejo crearConsejo(Consejo consejo) {
        return consejoRepository.save(consejo);
    }
    public List<Consejo> obtenerTodos() {
        return consejoRepository.findAll();
    }
    public List<Consejo> obtenerConsejosVisibles() {
        return consejoRepository.findConsejosActivosYPublicados(LocalDate.now());
    }
    public Consejo obtenerPorId(Integer id) {
        return consejoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Consejo no encontrado con ID " + id));
    }
    public Consejo actualizarConsejo(Integer id, Consejo consejoDatos) {
        Consejo existente = obtenerPorId(id);
        existente.setTitulo(consejoDatos.getTitulo());
        existente.setDescripcion(consejoDatos.getDescripcion());
        existente.setFechaPublicacion(consejoDatos.getFechaPublicacion());
        existente.setActivo(consejoDatos.isActivo());
        return consejoRepository.save(existente);
    }
    public void eliminarConsejo(Integer id) {
        if (!consejoRepository.existsById(id)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Consejo no encontrado con ID " + id);
        }
        consejoRepository.deleteById(id);
    }
}
