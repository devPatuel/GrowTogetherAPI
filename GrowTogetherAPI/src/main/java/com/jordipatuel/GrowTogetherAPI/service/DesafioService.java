package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.repository.DesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
@Service
public class DesafioService {
    private final DesafioRepository desafioRepository;
    private final UsuarioRepository usuarioRepository;
    @Autowired
    public DesafioService(DesafioRepository desafioRepository, UsuarioRepository usuarioRepository) {
        this.desafioRepository = desafioRepository;
        this.usuarioRepository = usuarioRepository;
    }
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
    public List<Desafio> obtenerTodos() {
        return desafioRepository.findAll();
    }
    public Desafio obtenerPorId(Integer id) {
        return desafioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Desafío no encontrado con ID " + id));
    }
    public List<Desafio> obtenerDesafiosActivos() {
        return desafioRepository.findByFechaFinAfterAndActivoTrue(new Date());
    }
    public long contarDesafiosActivos() {
        return desafioRepository.countByFechaFinAfterAndActivoTrue(new Date());
    }
    public void eliminarDesafio(Integer id) {
        Desafio desafio = obtenerPorId(id);
        desafio.setActivo(false);
        desafioRepository.save(desafio);
    }
    public boolean isCreator(Integer desafioId, Long usuarioId) {
        Desafio desafio = desafioRepository.findById(desafioId).orElse(null);
        if (desafio == null) {
            return false;
        }
        return desafio.getCreador().getId().equals(usuarioId);
    }
}
