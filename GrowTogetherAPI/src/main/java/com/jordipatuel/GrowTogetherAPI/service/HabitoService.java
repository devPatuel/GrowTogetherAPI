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
        habito.setFechaInicio(LocalDate.now());
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

    public boolean estaCompletadoEnFecha(Integer habitoId, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        return registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(habitoId, usuarioId, fecha)
                .map(r -> r.getEstado() == EstadoHabito.COMPLETADO)
                .orElse(false);
    }

    @Transactional
    public Habito completarHabito(Integer id, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        Habito habito = obtenerPorId(id);
        if (!habito.getUsuario().getId().equals(usuarioId)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("No puedes completar un hábito de otro usuario");
        }

        final LocalDate fechaFinal = fecha;
        RegistroHabito registro = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, fecha)
                .orElseGet(() -> {
                    RegistroHabito nuevo = new RegistroHabito();
                    nuevo.setHabito(habito);
                    nuevo.setUsuario(habito.getUsuario());
                    nuevo.setFecha(fechaFinal);
                    return nuevo;
                });

        if (registro.getEstado() == EstadoHabito.COMPLETADO) {
            return habito;
        }

        registro.setEstado(EstadoHabito.COMPLETADO);
        registroHabitoRepository.save(registro);

        // Recalcular racha desde hoy hacia atras
        recalcularRacha(habito, id, usuarioId);

        return habitoRepository.save(habito);
    }

    @Transactional
    public Habito descompletarHabito(Integer id, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        Habito habito = obtenerPorId(id);
        if (!habito.getUsuario().getId().equals(usuarioId)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("No puedes modificar un hábito de otro usuario");
        }

        RegistroHabito registro = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, fecha)
                .orElse(null);

        if (registro == null || registro.getEstado() != EstadoHabito.COMPLETADO) {
            return habito;
        }

        registro.setEstado(EstadoHabito.PENDIENTE);
        registroHabitoRepository.save(registro);

        // Recalcular racha desde hoy hacia atras
        recalcularRacha(habito, id, usuarioId);

        return habitoRepository.save(habito);
    }

    private void recalcularRacha(Habito habito, Integer id, Long usuarioId) {
        // Contar dias consecutivos completados desde hoy hacia atras
        int racha = 0;
        LocalDate dia = LocalDate.now();
        // Si hoy no esta completado, empezar desde ayer
        Optional<RegistroHabito> hoyReg = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, dia);
        if (hoyReg.isEmpty() || hoyReg.get().getEstado() != EstadoHabito.COMPLETADO) {
            dia = dia.minusDays(1);
        }
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
        if (racha > habito.getRachaMaxima()) {
            habito.setRachaMaxima(racha);
        }
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
            if (habitoActualizado.getTipo() != null) {
                habito.setTipo(habitoActualizado.getTipo());
                modificado = true;
            }
            if (habitoActualizado.getIcono() != null) {
                habito.setIcono(habitoActualizado.getIcono());
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
