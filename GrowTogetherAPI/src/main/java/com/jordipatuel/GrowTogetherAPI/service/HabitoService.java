package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoDTO;
import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.model.RegistroHabito;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.DiaSemana;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.model.enums.TipoHabito;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroHabitoRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
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
 *
 * Recibe y devuelve DTOs: los Controllers nunca ven la entidad {@link Habito}.
 * Incluye los mappers y los cálculos derivados (completadoHoy, progresoMensual)
 * que antes estaban en el controller.
 */
@Service
public class HabitoService {
    private final HabitoRepository habitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    /**
     * Inyecta los repositorios necesarios para hábitos, usuarios y registros.
     *
     * @param habitoRepository repositorio de hábitos
     * @param usuarioRepository repositorio de usuarios
     * @param registroHabitoRepository repositorio de registros diarios
     */
    @Autowired
    public HabitoService(HabitoRepository habitoRepository,
                         UsuarioRepository usuarioRepository,
                         RegistroHabitoRepository registroHabitoRepository) {
        this.habitoRepository = habitoRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroHabitoRepository = registroHabitoRepository;
    }

    /**
     * Crea un nuevo hábito para el usuario a partir del DTO, inicializa rachas a 0,
     * asigna la fecha de inicio a hoy y crea el primer registro del día como PENDIENTE.
     *
     * @param dto datos del nuevo hábito
     * @param usuarioId ID del usuario propietario
     * @return el hábito creado con sus métricas iniciales
     */
    public HabitoDTO crearHabito(HabitoCreateDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID " + usuarioId));
        Habito habito = toEntity(dto);
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
        return toDTO(habitoGuardado, usuarioId);
    }
    /**
     * Devuelve todos los hábitos sin filtrar (uso interno/admin).
     * El contexto de usuario para completadoHoy/progresoMensual se toma del propietario de cada hábito.
     *
     * @return lista completa de hábitos
     */
    public List<HabitoDTO> obtenerTodos() {
        return habitoRepository.findAll().stream()
                .map(h -> toDTO(h, h.getUsuario() != null ? h.getUsuario().getId() : null))
                .collect(Collectors.toList());
    }
    /**
     * Busca un hábito por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     *
     * @param id ID del hábito
     * @return el hábito encontrado
     */
    public HabitoDTO obtenerPorId(Integer id) {
        Habito habito = obtenerEntidadPorId(id);
        return toDTO(habito, habito.getUsuario() != null ? habito.getUsuario().getId() : null);
    }
    /**
     * Devuelve los hábitos activos de un usuario, calculando {@code completadoHoy}
     * para la fecha indicada (por defecto hoy).
     *
     * @param usuarioId ID del usuario propietario
     * @param fecha fecha de referencia (puede ser null para usar hoy)
     * @return lista de hábitos activos del usuario
     */
    public List<HabitoDTO> obtenerHabitosPorUsuario(Long usuarioId, LocalDate fecha) {
        return habitoRepository.findByUsuarioIdAndActivoTrue(usuarioId).stream()
                .map(h -> toDTO(h, usuarioId, fecha))
                .collect(Collectors.toList());
    }

    /**
     * Comprueba si un hábito está completado en una fecha concreta (por defecto hoy).
     *
     * @param habitoId ID del hábito
     * @param usuarioId ID del usuario propietario
     * @param fecha fecha a consultar (puede ser null para usar hoy)
     * @return true si el hábito está COMPLETADO ese día
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
     *
     * @param id ID del hábito
     * @param usuarioId ID del usuario propietario
     * @param fecha fecha del registro (puede ser null para usar hoy)
     * @return el hábito con racha y métricas actualizadas
     */
    @Transactional
    public HabitoDTO completarHabito(Integer id, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        Habito habito = obtenerEntidadPorId(id);
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
            return toDTO(habito, usuarioId, fecha);
        }

        registro.setEstado(EstadoHabito.COMPLETADO);
        registroHabitoRepository.save(registro);

        // Recalcular racha desde hoy hacia atras
        recalcularRacha(habito, id, usuarioId);

        Habito guardado = habitoRepository.save(habito);
        return toDTO(guardado, usuarioId, fecha);
    }

    /**
     * Revierte el hábito a PENDIENTE en la fecha indicada (por defecto hoy).
     * Si ya era PENDIENTE o no existe registro, no hace nada. Recalcula la racha.
     *
     * @param id ID del hábito
     * @param usuarioId ID del usuario propietario
     * @param fecha fecha del registro a revertir (puede ser null para usar hoy)
     * @return el hábito con racha y métricas recalculadas
     */
    @Transactional
    public HabitoDTO descompletarHabito(Integer id, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        Habito habito = obtenerEntidadPorId(id);
        if (!habito.getUsuario().getId().equals(usuarioId)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("No puedes modificar un hábito de otro usuario");
        }

        RegistroHabito registro = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFecha(id, usuarioId, fecha)
                .orElse(null);

        // Solo se puede "descompletar" si hay un registro (COMPLETADO o NO_COMPLETADO).
        // Si ya es PENDIENTE o no existe, no hay nada que cambiar.
        if (registro == null || registro.getEstado() == EstadoHabito.PENDIENTE) {
            return toDTO(habito, usuarioId, fecha);
        }

        registro.setEstado(EstadoHabito.PENDIENTE);
        registroHabitoRepository.save(registro);

        // Recalcular racha desde hoy hacia atras
        recalcularRacha(habito, id, usuarioId);

        Habito guardado = habitoRepository.save(habito);
        return toDTO(guardado, usuarioId, fecha);
    }

    /**
     * Delega el recálculo de racha al método correcto según la frecuencia del hábito.
     *
     * @param habito entidad cuyo {@code rachaActual} y {@code rachaMaxima} se actualizan
     * @param id ID del hábito (mismo que {@code habito.getId()}, para queries optimizadas)
     * @param usuarioId ID del usuario propietario
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
     *
     * @param habito entidad cuyas rachas se actualizan
     * @param id ID del hábito
     * @param usuarioId ID del usuario propietario
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
     *
     * @param habito entidad cuyas rachas se actualizan
     * @param id ID del hábito
     * @param usuarioId ID del usuario propietario
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
     *
     * @param id ID del hábito
     * @param usuarioId ID del usuario propietario
     * @return el hábito con métricas de progreso
     */
    public HabitoDTO obtenerProgreso(Integer id, Long usuarioId) {
        return toDTO(obtenerEntidadPorId(id), usuarioId);
    }

    /**
     * Actualiza los campos del hábito con los valores del DTO recibido.
     * Solo modifica y guarda si hubo algún cambio real.
     *
     * @param id ID del hábito a editar
     * @param dto datos nuevos del hábito
     * @param usuarioId ID del usuario propietario (para enriquecer el DTO de salida)
     * @return el hábito actualizado
     */
    public HabitoDTO editarHabito(Integer id, HabitoCreateDTO dto, Long usuarioId) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + id));
        boolean modificado = false;
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            habito.setNombre(dto.getNombre());
            modificado = true;
        }
        if (dto.getDescripcion() != null && !dto.getDescripcion().isBlank()) {
            habito.setDescripcion(dto.getDescripcion());
            modificado = true;
        }
        if (dto.getFrecuencia() != null) {
            habito.setFrecuencia(Frecuencia.valueOf(dto.getFrecuencia()));
            modificado = true;
        }
        if (dto.getDiasSemana() != null) {
            habito.setDiasSemana(parseDias(dto.getDiasSemana()));
            modificado = true;
        }
        if (dto.getTipo() != null) {
            habito.setTipo(TipoHabito.valueOf(dto.getTipo()));
            modificado = true;
        }
        if (dto.getIcono() != null) {
            habito.setIcono(dto.getIcono());
            modificado = true;
        }
        if (modificado) {
            habito = habitoRepository.save(habito);
        }
        return toDTO(habito, usuarioId);
    }
    /**
     * Desactiva el hábito (soft delete).
     *
     * @param id ID del hábito a desactivar
     */
    public void eliminarHabito(Integer id) {
        Habito habito = obtenerEntidadPorId(id);
        habito.setActivo(false);
        habitoRepository.save(habito);
    }
    /**
     * Verifica si el usuario indicado es el propietario del hábito.
     * Usado por {@code @PreAuthorize} en el controller para control de acceso.
     *
     * @param habitoId ID del hábito
     * @param usuarioId ID del usuario a comprobar
     * @return true si el usuario es el propietario del hábito
     */
    public boolean isOwner(Integer habitoId, Long usuarioId) {
        return habitoRepository.findById(habitoId)
                .map(habito -> habito.getUsuario().getId().equals(usuarioId))
                .orElse(false);
    }

    /**
     * Helper interno: recupera la entidad {@link Habito} para operaciones internas
     * del servicio que necesitan manipular la referencia JPA.
     *
     * @param id ID del hábito
     * @return la entidad encontrada
     */
    private Habito obtenerEntidadPorId(Integer id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Hábito no encontrado con ID " + id));
    }

    /**
     * Convierte un hábito a DTO usando hoy como fecha de referencia para {@code completadoHoy}.
     *
     * @param habito entidad origen
     * @param usuarioId ID del usuario propietario (para los campos derivados)
     * @return DTO equivalente
     */
    private HabitoDTO toDTO(Habito habito, Long usuarioId) {
        return toDTO(habito, usuarioId, null);
    }

    /**
     * Convierte un hábito a DTO indicando la fecha de referencia para {@code completadoHoy}
     * y calculando el progreso mensual real.
     *
     * @param habito entidad origen
     * @param usuarioId ID del usuario propietario (para los campos derivados)
     * @param fecha fecha de referencia (puede ser null para usar hoy)
     * @return DTO con campos derivados rellenados
     */
    private HabitoDTO toDTO(Habito habito, Long usuarioId, LocalDate fecha) {
        boolean completadoHoy = usuarioId != null
                && estaCompletadoEnFecha(habito.getId(), usuarioId, fecha);
        HabitoDTO dto = new HabitoDTO();
        dto.setId(habito.getId());
        dto.setNombre(habito.getNombre());
        dto.setDescripcion(habito.getDescripcion());
        dto.setRachaActual(habito.getRachaActual());
        dto.setRachaMaxima(habito.getRachaMaxima());
        dto.setUsuarioId(habito.getUsuario() != null ? habito.getUsuario().getId() : null);
        dto.setCompletadoHoy(completadoHoy);
        dto.setFrecuencia(habito.getFrecuencia() != null ? habito.getFrecuencia().name() : "DIARIO");
        dto.setDiasSemana(habito.getDiasSemana() != null
                ? habito.getDiasSemana().stream().map(DiaSemana::name).collect(Collectors.toSet())
                : new HashSet<>());
        dto.setTipo(habito.getTipo() != null ? habito.getTipo().name() : "POSITIVO");
        dto.setIcono(habito.getIcono());
        dto.setFechaInicio(habito.getFechaInicio());
        dto.setProgresoMensual(usuarioId != null ? calcularProgresoMensual(habito, usuarioId) : 0);
        return dto;
    }

    /**
     * Calcula el porcentaje de días completados en el mes actual respecto a los días esperados.
     * Para hábitos DIARIO cuenta todos los días del mes hasta hoy.
     * Para hábitos PERSONALIZADO cuenta solo los días programados en diasSemana.
     *
     * @param habito hábito a evaluar
     * @param usuarioId ID del usuario propietario
     * @return progreso mensual entre 0.0 y 1.0
     */
    private double calcularProgresoMensual(Habito habito, Long usuarioId) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        // Dias esperados este mes hasta hoy
        int diasEsperados = 0;
        if (habito.getFrecuencia() == Frecuencia.DIARIO) {
            diasEsperados = hoy.getDayOfMonth();
        } else {
            Set<DiaSemana> dias = habito.getDiasSemana();
            if (dias == null || dias.isEmpty()) return 0;
            for (LocalDate d = inicioMes; !d.isAfter(hoy); d = d.plusDays(1)) {
                DayOfWeek dow = d.getDayOfWeek();
                String diaEnum = switch (dow) {
                    case MONDAY -> "LUNES";
                    case TUESDAY -> "MARTES";
                    case WEDNESDAY -> "MIERCOLES";
                    case THURSDAY -> "JUEVES";
                    case FRIDAY -> "VIERNES";
                    case SATURDAY -> "SABADO";
                    case SUNDAY -> "DOMINGO";
                };
                if (dias.stream().anyMatch(ds -> ds.name().equals(diaEnum))) {
                    diasEsperados++;
                }
            }
        }

        if (diasEsperados == 0) return 0;

        // Dias completados reales
        List<RegistroHabito> registros = registroHabitoRepository
                .findByHabito_IdAndUsuario_IdAndFechaBetweenOrderByFechaDesc(habito.getId(), usuarioId, inicioMes, hoy);
        long completados = registros.stream()
                .filter(r -> r.getEstado() == EstadoHabito.COMPLETADO)
                .count();

        return Math.min((double) completados / diasEsperados, 1.0);
    }

    /**
     * Construye una nueva entidad {@link Habito} a partir del DTO de creación.
     * No asigna usuario, rachas ni fechaInicio: eso se resuelve en {@code crearHabito}.
     *
     * @param dto DTO con los datos del nuevo hábito
     * @return entidad sin persistir
     */
    private Habito toEntity(HabitoCreateDTO dto) {
        Habito habito = new Habito();
        habito.setNombre(dto.getNombre());
        habito.setDescripcion(dto.getDescripcion());
        if (dto.getFrecuencia() != null) {
            habito.setFrecuencia(Frecuencia.valueOf(dto.getFrecuencia()));
        }
        habito.setDiasSemana(parseDias(dto.getDiasSemana()));
        if (dto.getTipo() != null) {
            habito.setTipo(TipoHabito.valueOf(dto.getTipo()));
        }
        habito.setIcono(dto.getIcono());
        return habito;
    }

    /**
     * Convierte un Set de Strings con nombres de días a un Set de {@link DiaSemana}.
     *
     * @param dias conjunto de strings (ej: "LUNES", "MARTES")
     * @return conjunto de enums equivalente
     */
    private Set<DiaSemana> parseDias(Set<String> dias) {
        if (dias == null || dias.isEmpty()) return new HashSet<>();
        return dias.stream()
                .map(DiaSemana::valueOf)
                .collect(Collectors.toSet());
    }
}
