package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroHabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
public class HabitoService {
    private final HabitoRepository habitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    @Autowired
    public HabitoService(HabitoRepository habitoRepository,
                         UsuarioRepository usuarioRepository,
                         RegistroHabitoRepository registroHabitoRepository) {
        this.habitoRepository = habitoRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroHabitoRepository = registroHabitoRepository;
    }
    public Habito crearHabito(Habito habito, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID " + usuarioId));
        habito.setUsuario(usuario);
        habito.setRachaActual(0);
        habito.setRachaMaxima(0);
        Habito habitoGuardado = habitoRepository.save(habito);
        RegistroHabito registro = new RegistroHabito();
        registro.setUsuario(usuario);
        registro.setHabito(habitoGuardado);
        registro.setFecha(LocalDate.now());
        registro.setEstado(EstadoHabito.PENDIENTE);
        registroHabitoRepository.save(registro);
        return habitoGuardado;
    }
    public List<Habito> obtenerTodos() {
        return habitoRepository.findAll();
    }
    public Habito obtenerPorId(Integer id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + id));
    }
    public List<Habito> obtenerHabitosPorUsuario(Long usuarioId) {
        return habitoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
    }

    public boolean estaCompletadoHoy(Integer habitoId, Long usuarioId) {
        return registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(habitoId, usuarioId, LocalDate.now())
                .map(r -> r.getEstado() == EstadoHabito.COMPLETADO)
                .orElse(false);
    }

    @Transactional
    public Habito completarHabito(Integer id, Long usuarioId) {
        Habito habito = obtenerPorId(id);
        if (!habito.getUsuario().getId().equals(usuarioId)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("No puedes completar un hábito de otro usuario");
        }

        RegistroHabito registro = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, LocalDate.now())
                .orElseGet(() -> {
                    RegistroHabito nuevo = new RegistroHabito();
                    nuevo.setHabito(habito);
                    nuevo.setUsuario(habito.getUsuario());
                    nuevo.setFecha(LocalDate.now());
                    return nuevo;
                });

        // Si ya esta completado hoy, no hacer nada (evitar doble puntos)
        if (registro.getEstado() == EstadoHabito.COMPLETADO) {
            return habito;
        }

        registro.setEstado(EstadoHabito.COMPLETADO);
        registroHabitoRepository.save(registro);

        // Calcular racha
        Optional<RegistroHabito> ayer = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, LocalDate.now().minusDays(1));
        if (ayer.isEmpty() || ayer.get().getEstado() != EstadoHabito.COMPLETADO) {
            habito.setRachaActual(1);
        } else {
            habito.setRachaActual(habito.getRachaActual() + 1);
        }
        if (habito.getRachaActual() > habito.getRachaMaxima()) {
            habito.setRachaMaxima(habito.getRachaActual());
        }

        return habitoRepository.save(habito);
    }

    @Transactional
    public Habito descompletarHabito(Integer id, Long usuarioId) {
        Habito habito = obtenerPorId(id);
        if (!habito.getUsuario().getId().equals(usuarioId)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("No puedes modificar un hábito de otro usuario");
        }

        RegistroHabito registro = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, LocalDate.now())
                .orElse(null);

        // Si no estaba completado, no hacer nada
        if (registro == null || registro.getEstado() != EstadoHabito.COMPLETADO) {
            return habito;
        }

        registro.setEstado(EstadoHabito.PENDIENTE);
        registroHabitoRepository.save(registro);

        // Restaurar racha: recalcular desde ayer
        Optional<RegistroHabito> ayer = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, LocalDate.now().minusDays(1));
        if (ayer.isPresent() && ayer.get().getEstado() == EstadoHabito.COMPLETADO) {
            // Recalcular la racha contando dias consecutivos hacia atras desde ayer
            int racha = 0;
            LocalDate dia = LocalDate.now().minusDays(1);
            while (true) {
                Optional<RegistroHabito> reg = registroHabitoRepository
                        .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, dia);
                if (reg.isPresent() && reg.get().getEstado() == EstadoHabito.COMPLETADO) {
                    racha++;
                    dia = dia.minusDays(1);
                } else {
                    break;
                }
            }
            habito.setRachaActual(racha);
        } else {
            habito.setRachaActual(0);
        }

        return habitoRepository.save(habito);
    }

    public Habito obtenerProgreso(Integer id) {
        return obtenerPorId(id);
    }

    public Habito editarHabito(Integer id, Habito habitoActualizado) {
        return habitoRepository.findById(id).map(habito -> {
            boolean modificado = false;
            if (habitoActualizado.getNombre() != null && !habitoActualizado.getNombre().isBlank()) {
                habito.setNombre(habitoActualizado.getNombre());
                modificado = true;
            }
            if (habitoActualizado.getDescripcion() != null && !habitoActualizado.getDescripcion().isBlank()) {
                habito.setDescripcion(habitoActualizado.getDescripcion());
                modificado = true;
            }
            if (habitoActualizado.getFrecuencia() != null) {
                habito.setFrecuencia(habitoActualizado.getFrecuencia());
                modificado = true;
            }
            if (habitoActualizado.getDiasSemana() != null) {
                habito.setDiasSemana(habitoActualizado.getDiasSemana());
                modificado = true;
            }
            if (modificado) {
                return habitoRepository.save(habito);
            }
            return habito;
        }).orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + id));
    }
    public void eliminarHabito(Integer id) {
        Habito habito = obtenerPorId(id);
        habito.setActivo(false);
        habitoRepository.save(habito);
    }
    public boolean isOwner(Integer habitoId, Long usuarioId) {
        return habitoRepository.findById(habitoId)
                .map(habito -> habito.getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }
}
