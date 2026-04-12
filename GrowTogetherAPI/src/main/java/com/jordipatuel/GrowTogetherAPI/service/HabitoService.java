package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.DiaSemana;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroHabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de hábitos.
 *
 * Centraliza la lógica de creación, edición, eliminación y seguimiento de hábitos.
 * Contiene la lógica más compleja de la aplicación: el cálculo de rachas,
 * que difiere según el hábito sea DIARIO o PERSONALIZADO (días de la semana concretos).
 */
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

    /**
     * Crea un nuevo hábito para el usuario, inicializa rachas a 0,
     * asigna la fecha de inicio a hoy y crea el primer registro del día como PENDIENTE.
     */
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
    /**
     * Devuelve todos los hábitos sin filtrar (uso interno/admin).
     */
    public List<Habito> obtenerTodos() {
        return habitoRepository.findAll();
    }
    /**
     * Busca un hábito por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public Habito obtenerPorId(Integer id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + id));
    }
    /**
     * Devuelve los hábitos activos de un usuario.
     */
    public List<Habito> obtenerHabitosPorUsuario(Long usuarioId) {
        return habitoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
    }

    /**
     * Comprueba si un hábito está completado en una fecha concreta (por defecto hoy).
     */
    public boolean estaCompletadoEnFecha(Integer habitoId, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        return registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(habitoId, usuarioId, fecha)
                .map(r -> r.getEstado() == EstadoHabito.COMPLETADO)
                .orElse(false);
    }

    /**
     * Marca el hábito como COMPLETADO en la fecha indicada (por defecto hoy).
     * Si ya estaba COMPLETADO no hace nada. Recalcula la racha tras el cambio.
     */
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

    /**
     * Revierte el hábito a PENDIENTE en la fecha indicada (por defecto hoy).
     * Si ya era PENDIENTE o no existe registro, no hace nada. Recalcula la racha.
     */
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

        // Solo se puede "descompletar" si hay un registro (COMPLETADO o NO_COMPLETADO).
        // Si ya es PENDIENTE o no existe, no hay nada que cambiar.
        if (registro == null || registro.getEstado() == EstadoHabito.PENDIENTE) {
            return habito;
        }

        registro.setEstado(EstadoHabito.PENDIENTE);
        registroHabitoRepository.save(registro);

        // Recalcular racha desde hoy hacia atras
        recalcularRacha(habito, id, usuarioId);

        return habitoRepository.save(habito);
    }

    /**
     * Delega el recálculo de racha al método correcto según la frecuencia del hábito.
     */
    private void recalcularRacha(Habito habito, Integer id, Long usuarioId) {
        if (habito.getFrecuencia() == Frecuencia.PERSONALIZADO
                && habito.getDiasSemana() != null
                && !habito.getDiasSemana().isEmpty()) {
            recalcularRachaPersonalizado(habito, id, usuarioId);
        } else {
            recalcularRachaDiario(habito, id, usuarioId);
        }
    }

    /**
     * Recalcula la racha para hábitos DIARIOS contando días consecutivos
     * completados hacia atrás desde hoy (o desde ayer si hoy no está completado).
     */
    private void recalcularRachaDiario(Habito habito, Integer id, Long usuarioId) {
        int racha = 0;
        LocalDate dia = LocalDate.now();
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
        if (racha > habito.getRachaMaxima()) habito.setRachaMaxima(racha);
    }

    /**
     * Recalcula la racha para hábitos PERSONALIZADOS contando solo los días
     * programados (diasSemana) completados de forma consecutiva hacia atrás.
     * Los días no programados se saltan sin romper la racha.
     * No usa fechaInicio como límite inferior para permitir marcados retroactivos.
     */
    private void recalcularRachaPersonalizado(Habito habito, Integer id, Long usuarioId) {
        // DiaSemana.ordinal(): LUNES=0..DOMINGO=6 → DayOfWeek.of(ordinal+1): MONDAY=1..SUNDAY=7
        Set<DayOfWeek> dowProgramados = habito.getDiasSemana().stream()
                .map(d -> DayOfWeek.of(d.ordinal() + 1))
                .collect(Collectors.toSet());

        int racha = 0;
        LocalDate cursor = LocalDate.now();
        // Límite de seguridad amplio para no iterar indefinidamente.
        // No se usa fechaInicio para permitir contar marcados retroactivos.
        LocalDate limiteSeguridad = LocalDate.now().minusDays(730);

        // Encontrar el día programado más reciente (empezando hoy)
        while (cursor.isAfter(limiteSeguridad) && !dowProgramados.contains(cursor.getDayOfWeek())) {
            cursor = cursor.minusDays(1);
        }

        // Si el día programado más reciente no está completado, retroceder un día
        // (el bucle de conteo saltará automáticamente los días no programados)
        if (!cursor.isBefore(limiteSeguridad)) {
            Optional<RegistroHabito> ultimo = registroHabitoRepository
                    .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, cursor);
            if (ultimo.isEmpty() || ultimo.get().getEstado() != EstadoHabito.COMPLETADO) {
                cursor = cursor.minusDays(1);
            }
        }

        // Contar días programados consecutivos completados hacia atrás
        while (cursor.isAfter(limiteSeguridad)) {
            if (!dowProgramados.contains(cursor.getDayOfWeek())) {
                cursor = cursor.minusDays(1);
                continue;
            }
            Optional<RegistroHabito> reg = registroHabitoRepository
                    .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, cursor);
            if (reg.isPresent() && reg.get().getEstado() == EstadoHabito.COMPLETADO) {
                racha++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }

        habito.setRachaActual(racha);
        if (racha > habito.getRachaMaxima()) habito.setRachaMaxima(racha);
    }

    /**
     * Devuelve el hábito con sus datos de progreso (racha actual y máxima).
     */
    public Habito obtenerProgreso(Integer id) {
        return obtenerPorId(id);
    }

    /**
     * Actualiza los campos del hábito que vengan informados en el objeto recibido.
     * Solo modifica y guarda si hubo algún cambio real.
     */
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
    /**
     * Desactiva el hábito (soft delete).
     */
    public void eliminarHabito(Integer id) {
        Habito habito = obtenerPorId(id);
        habito.setActivo(false);
        habitoRepository.save(habito);
    }
    /**
     * Verifica si el usuario indicado es el propietario del hábito.
     * Usado por {@code @PreAuthorize} en el controller para control de acceso.
     */
    public boolean isOwner(Integer habitoId, Long usuarioId) {
        return habitoRepository.findById(habitoId)
                .map(habito -> habito.getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }
}
