package com.jordipatuel.GrowTogetherAPI.service;
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
    public ParticipacionDesafio unirseADesafio(Integer desafioId, Long usuarioId) {
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
        return participacionDesafioRepository.save(participacion);
    }
    public List<ParticipacionDesafio> obtenerRanking(Integer desafioId) {
        return participacionDesafioRepository.findByDesafioIdOrderByPuntosGanadosEnDesafioDesc(desafioId);
    }
    public List<ParticipacionDesafio> obtenerPorUsuario(Long usuarioId) {
        return participacionDesafioRepository.findByUsuarioId(usuarioId);
    }
    public List<ParticipacionDesafio> obtenerPorDesafio(Integer desafioId) {
        return participacionDesafioRepository.findByDesafioId(desafioId);
    }
    public List<ParticipacionDesafio> obtenerTodos() {
        return participacionDesafioRepository.findAll();
    }
}
