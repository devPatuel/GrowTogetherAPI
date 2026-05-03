package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.config.Scoring;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.RegistroDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.model.Desafio;
import com.jordipatuel.GrowTogetherAPI.model.ParticipacionDesafio;
import com.jordipatuel.GrowTogetherAPI.model.RegistroDesafio;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.DiaSemana;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoHabito;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import com.jordipatuel.GrowTogetherAPI.model.enums.Frecuencia;
import com.jordipatuel.GrowTogetherAPI.repository.DesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.ParticipacionDesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.RegistroDesafioRepository;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Servicio de gestión de participaciones en desafíos.
 *
 * Una participación vincula a un usuario con un desafío y registra su estado
 * (ACTIVO/SUPERADO/ABANDONADO), los puntos ganados y la racha actual del participante.
 *
 * Centraliza la lógica de seguimiento diario: completar/descompletar un día,
 * recalcular la racha (con la misma estrategia que HabitoService) y actualizar
 * los puntos según {@link Scoring}, donde una racha rota reinicia el multiplicador.
 *
 * Devuelve {@link ParticipacionDesafioDTO} en todas sus consultas para que los
 * Controllers no vean la entidad JPA.
 */
@Service
public class ParticipacionDesafioService {
    private final ParticipacionDesafioRepository participacionDesafioRepository;
    private final DesafioRepository desafioRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroDesafioRepository registroDesafioRepository;

    /**
     * Inyecta los repositorios necesarios para participaciones, desafíos, usuarios y registros.
     *
     * @param participacionDesafioRepository repositorio de participaciones
     * @param desafioRepository repositorio de desafíos
     * @param usuarioRepository repositorio de usuarios
     * @param registroDesafioRepository repositorio de registros diarios del desafío
     */
    @Autowired
    public ParticipacionDesafioService(
            ParticipacionDesafioRepository participacionDesafioRepository,
            DesafioRepository desafioRepository,
            UsuarioRepository usuarioRepository,
            RegistroDesafioRepository registroDesafioRepository) {
        this.participacionDesafioRepository = participacionDesafioRepository;
        this.desafioRepository = desafioRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroDesafioRepository = registroDesafioRepository;
    }

    /**
     * Inscribe al usuario en el desafío validando que no esté ya apuntado
     * y que el desafío no haya finalizado. Inicializa el estado a ACTIVO y puntos a 0.
     *
     * @param desafioId ID del desafío
     * @param usuarioId ID del usuario que se inscribe
     * @return la participación creada
     */
    public ParticipacionDesafioDTO unirseADesafio(Integer desafioId, Long usuarioId) {
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
        ParticipacionDesafio participacion = nuevaParticipacion(desafio, usuario);
        return toDTO(participacionDesafioRepository.save(participacion), null);
    }

    /**
     * Crea una participación si el usuario no estaba ya inscrito. Sin lanzar excepción si lo está.
     * Pensado para el flujo de invitar amigos al crear el desafío.
     *
     * @param desafio desafío al que se inscribe el usuario
     * @param usuarioId ID del usuario a inscribir
     * @return la participación existente o la recién creada
     */
    public ParticipacionDesafio crearParticipacionSiNoExiste(Desafio desafio, Long usuarioId) {
        Optional<ParticipacionDesafio> existente = participacionDesafioRepository
                .findByDesafioIdAndUsuarioId(desafio.getId(), usuarioId);
        if (existente.isPresent()) return existente.get();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID " + usuarioId));
        return participacionDesafioRepository.save(nuevaParticipacion(desafio, usuario));
    }

    /**
     * Devuelve los participantes de un desafío ordenados por puntos de mayor a menor.
     * Asigna posiciones 1, 2, 3... y rellena completadoHoy / foto en el DTO.
     *
     * @param desafioId ID del desafío
     * @return lista de participaciones con posición y métricas
     */
    public List<ParticipacionDesafioDTO> obtenerRanking(Integer desafioId) {
        List<ParticipacionDesafio> ranking = participacionDesafioRepository
                .findByDesafioIdOrderByPuntosGanadosEnDesafioDesc(desafioId);
        return mapearConPosicion(ranking);
    }

    /**
     * Devuelve todos los desafíos en los que participa un usuario.
     *
     * @param usuarioId ID del usuario
     * @return lista de participaciones del usuario
     */
    public List<ParticipacionDesafioDTO> obtenerPorUsuario(Long usuarioId) {
        return participacionDesafioRepository.findByUsuarioId(usuarioId).stream()
                .map(p -> toDTO(p, null))
                .collect(Collectors.toList());
    }

    /**
     * Devuelve todas las participaciones de un desafío concreto.
     *
     * @param desafioId ID del desafío
     * @return lista de participaciones con posición y métricas
     */
    public List<ParticipacionDesafioDTO> obtenerPorDesafio(Integer desafioId) {
        return mapearConPosicion(participacionDesafioRepository
                .findByDesafioIdOrderByPuntosGanadosEnDesafioDesc(desafioId));
    }

    /**
     * Devuelve todas las participaciones sin filtrar.
     *
     * @return lista completa de participaciones
     */
    public List<ParticipacionDesafioDTO> obtenerTodos() {
        return participacionDesafioRepository.findAll().stream()
                .map(p -> toDTO(p, null))
                .collect(Collectors.toList());
    }

    /**
     * Marca el desafío como COMPLETADO en la fecha indicada (por defecto hoy).
     * Si ya estaba COMPLETADO no hace nada. Recalcula racha y puntos del participante.
     *
     * @param desafioId ID del desafío
     * @param usuarioId ID del usuario participante
     * @param fecha fecha del registro (puede ser null para usar hoy)
     * @return la participación con racha y puntos actualizados
     */
    @Transactional
    public ParticipacionDesafioDTO completarDesafio(Integer desafioId, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        ParticipacionDesafio participacion = obtenerParticipacion(desafioId, usuarioId);
        Desafio desafio = participacion.getDesafio();
        validarFechaDentroDelDesafio(desafio, fecha);
        validarDiaPermitidoPorFrecuencia(desafio, fecha);

        final LocalDate fechaFinal = fecha;
        RegistroDesafio registro = registroDesafioRepository
                .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, fecha)
                .orElseGet(() -> {
                    RegistroDesafio nuevo = new RegistroDesafio();
                    nuevo.setDesafio(desafio);
                    nuevo.setUsuario(participacion.getUsuario());
                    nuevo.setFecha(fechaFinal);
                    nuevo.setEstado(EstadoHabito.PENDIENTE);
                    nuevo.setPuntosGanados(0);
                    return nuevo;
                });

        if (registro.getEstado() == EstadoHabito.COMPLETADO) {
            return toDTO(participacion, null);
        }

        registro.setEstado(EstadoHabito.COMPLETADO);
        registroDesafioRepository.save(registro);

        recalcularRacha(participacion);
        // Tras recalcular racha, los puntos del día son los del nuevo valor de rachaActual
        // pero ese valor refleja la racha hasta hoy; para el registro de un día pasado
        // necesitamos sumar puntos según la posición de ese día en la cadena de completados.
        recalcularPuntos(participacion);

        participacionDesafioRepository.save(participacion);
        return toDTO(participacion, null);
    }

    /**
     * Revierte el desafío a PENDIENTE en la fecha indicada (por defecto hoy).
     * Si no había registro o ya era PENDIENTE no hace nada. Recalcula racha y puntos.
     *
     * @param desafioId ID del desafío
     * @param usuarioId ID del usuario participante
     * @param fecha fecha del registro a revertir (puede ser null para usar hoy)
     * @return la participación con racha y puntos recalculados
     */
    @Transactional
    public ParticipacionDesafioDTO descompletarDesafio(Integer desafioId, Long usuarioId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        ParticipacionDesafio participacion = obtenerParticipacion(desafioId, usuarioId);

        RegistroDesafio registro = registroDesafioRepository
                .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, fecha)
                .orElse(null);
        if (registro == null || registro.getEstado() == EstadoHabito.PENDIENTE) {
            return toDTO(participacion, null);
        }

        registro.setEstado(EstadoHabito.PENDIENTE);
        registro.setPuntosGanados(0);
        registroDesafioRepository.save(registro);

        recalcularRacha(participacion);
        recalcularPuntos(participacion);
        participacionDesafioRepository.save(participacion);
        return toDTO(participacion, null);
    }

    /**
     * Marca la participación como ABANDONADO. El participante deja de aparecer en el podio
     * pero conserva su histórico de registros para no perder la gráfica.
     *
     * @param desafioId ID del desafío
     * @param usuarioId ID del usuario que abandona
     * @return la participación marcada como ABANDONADO
     */
    @Transactional
    public ParticipacionDesafioDTO abandonarDesafio(Integer desafioId, Long usuarioId) {
        ParticipacionDesafio participacion = obtenerParticipacion(desafioId, usuarioId);
        participacion.setEstadoProgreso(EstadoProgreso.ABANDONADO);
        return toDTO(participacionDesafioRepository.save(participacion), null);
    }

    /**
     * Devuelve el historial de registros del desafío para todos los participantes
     * en el rango indicado. Usado por la gráfica multilínea.
     *
     * @param desafioId ID del desafío
     * @param fechaInicio fecha inicial del rango (puede ser null)
     * @param fechaFin fecha final del rango (puede ser null)
     * @return lista de registros para alimentar la gráfica
     */
    public List<RegistroDesafioDTO> obtenerHistorial(Integer desafioId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<RegistroDesafio> registros;
        if (fechaInicio != null && fechaFin != null) {
            registros = registroDesafioRepository.findByDesafio_IdAndFechaBetween(desafioId, fechaInicio, fechaFin);
        } else {
            registros = registroDesafioRepository.findByDesafio_IdOrderByFechaAsc(desafioId);
        }
        return registros.stream().map(this::toRegistroDTO).collect(Collectors.toList());
    }

    /**
     * Recalcula la racha actual del participante según la frecuencia del desafío.
     * Adapta el algoritmo de {@link HabitoService} a la pareja (usuario, desafio).
     *
     * @param participacion participación cuyas rachas se actualizan
     */
    private void recalcularRacha(ParticipacionDesafio participacion) {
        Desafio desafio = participacion.getDesafio();
        Long usuarioId = participacion.getUsuario().getId();
        Integer desafioId = desafio.getId();
        if (desafio.getFrecuencia() == Frecuencia.PERSONALIZADO
                && desafio.getDiasSemana() != null
                && !desafio.getDiasSemana().isEmpty()) {
            recalcularRachaPersonalizada(participacion, desafioId, usuarioId, desafio.getDiasSemana());
        } else {
            recalcularRachaDiaria(participacion, desafioId, usuarioId);
        }
    }

    private void recalcularRachaDiaria(ParticipacionDesafio participacion, Integer desafioId, Long usuarioId) {
        int racha = 0;
        LocalDate dia = LocalDate.now();
        Optional<RegistroDesafio> hoyReg = registroDesafioRepository
                .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, dia);
        if (hoyReg.isEmpty() || hoyReg.get().getEstado() != EstadoHabito.COMPLETADO) {
            dia = dia.minusDays(1);
        }
        while (true) {
            Optional<RegistroDesafio> reg = registroDesafioRepository
                    .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, dia);
            if (reg.isPresent() && reg.get().getEstado() == EstadoHabito.COMPLETADO) {
                racha++;
                dia = dia.minusDays(1);
            } else {
                break;
            }
        }
        participacion.setRachaActual(racha);
        if (racha > participacion.getRachaMaxima()) participacion.setRachaMaxima(racha);
    }

    private void recalcularRachaPersonalizada(ParticipacionDesafio participacion, Integer desafioId,
                                              Long usuarioId, Set<DiaSemana> diasSemana) {
        Set<DayOfWeek> dowProgramados = diasSemana.stream()
                .map(d -> DayOfWeek.of(d.ordinal() + 1))
                .collect(Collectors.toSet());
        int racha = 0;
        LocalDate cursor = LocalDate.now();
        LocalDate limiteSeguridad = LocalDate.now().minusDays(730);

        while (cursor.isAfter(limiteSeguridad) && !dowProgramados.contains(cursor.getDayOfWeek())) {
            cursor = cursor.minusDays(1);
        }
        if (!cursor.isBefore(limiteSeguridad)) {
            Optional<RegistroDesafio> ultimo = registroDesafioRepository
                    .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, cursor);
            if (ultimo.isEmpty() || ultimo.get().getEstado() != EstadoHabito.COMPLETADO) {
                cursor = cursor.minusDays(1);
            }
        }
        while (cursor.isAfter(limiteSeguridad)) {
            if (!dowProgramados.contains(cursor.getDayOfWeek())) {
                cursor = cursor.minusDays(1);
                continue;
            }
            Optional<RegistroDesafio> reg = registroDesafioRepository
                    .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, cursor);
            if (reg.isPresent() && reg.get().getEstado() == EstadoHabito.COMPLETADO) {
                racha++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        participacion.setRachaActual(racha);
        if (racha > participacion.getRachaMaxima()) participacion.setRachaMaxima(racha);
    }

    /**
     * Recalcula desde cero los puntos del participante recorriendo todos sus registros COMPLETADO
     * en orden cronológico y aplicando la fórmula de {@link Scoring} con bonus de racha.
     * Una racha rota reinicia el multiplicador.
     * También actualiza {@code puntosGanados} en cada {@link RegistroDesafio} para alimentar la gráfica.
     *
     * @param participacion participación cuyos puntos se recalculan
     */
    private void recalcularPuntos(ParticipacionDesafio participacion) {
        Desafio desafio = participacion.getDesafio();
        Long usuarioId = participacion.getUsuario().getId();
        Integer desafioId = desafio.getId();

        List<RegistroDesafio> todos = registroDesafioRepository
                .findByDesafio_IdAndUsuario_Id(desafioId, usuarioId);
        todos.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));

        Set<DayOfWeek> dowProgramados = (desafio.getFrecuencia() == Frecuencia.PERSONALIZADO
                && desafio.getDiasSemana() != null && !desafio.getDiasSemana().isEmpty())
                ? desafio.getDiasSemana().stream().map(d -> DayOfWeek.of(d.ordinal() + 1)).collect(Collectors.toSet())
                : null;

        int rachaCadena = 0;
        int totalPuntos = 0;
        LocalDate diaPrevio = null;
        for (RegistroDesafio reg : todos) {
            // Solo cuentan los días COMPLETADO. Los NO_COMPLETADO o PENDIENTE rompen la cadena.
            if (reg.getEstado() != EstadoHabito.COMPLETADO) {
                rachaCadena = 0;
                diaPrevio = reg.getFecha();
                reg.setPuntosGanados(0);
                continue;
            }
            if (diaPrevio != null) {
                LocalDate esperado = siguienteDiaProgramado(diaPrevio, dowProgramados);
                if (!reg.getFecha().equals(esperado)) {
                    rachaCadena = 0;
                }
            }
            rachaCadena++;
            int puntos = Scoring.puntosDia(rachaCadena);
            reg.setPuntosGanados(puntos);
            totalPuntos += puntos;
            diaPrevio = reg.getFecha();
        }
        registroDesafioRepository.saveAll(todos);
        participacion.setPuntosGanadosEnDesafio(totalPuntos);
    }

    /**
     * Devuelve el siguiente día válido tras {@code dia}: el día siguiente si la frecuencia es DIARIA,
     * o el siguiente día de la semana programado si la frecuencia es PERSONALIZADA.
     *
     * @param dia día de referencia
     * @param dowProgramados días programados (null para frecuencia DIARIA)
     * @return siguiente día válido
     */
    private LocalDate siguienteDiaProgramado(LocalDate dia, Set<DayOfWeek> dowProgramados) {
        LocalDate siguiente = dia.plusDays(1);
        if (dowProgramados == null) return siguiente;
        for (int i = 0; i < 7; i++) {
            if (dowProgramados.contains(siguiente.getDayOfWeek())) return siguiente;
            siguiente = siguiente.plusDays(1);
        }
        return siguiente;
    }

    private void validarFechaDentroDelDesafio(Desafio desafio, LocalDate fecha) {
        LocalDate inicio = aLocalDate(desafio.getFechaInicio());
        LocalDate fin = aLocalDate(desafio.getFechaFin());
        if (fecha.isBefore(inicio)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La fecha es anterior al inicio del desafío");
        }
        if (fecha.isAfter(fin)) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("El desafío ya ha finalizado");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("No se puede marcar un día futuro");
        }
    }

    private void validarDiaPermitidoPorFrecuencia(Desafio desafio, LocalDate fecha) {
        if (desafio.getFrecuencia() != Frecuencia.PERSONALIZADO) return;
        if (desafio.getDiasSemana() == null || desafio.getDiasSemana().isEmpty()) return;
        Set<DayOfWeek> dow = desafio.getDiasSemana().stream()
                .map(d -> DayOfWeek.of(d.ordinal() + 1)).collect(Collectors.toSet());
        if (!dow.contains(fecha.getDayOfWeek())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("Este día no aplica a la frecuencia del desafío");
        }
    }

    private LocalDate aLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private ParticipacionDesafio obtenerParticipacion(Integer desafioId, Long usuarioId) {
        return participacionDesafioRepository.findByDesafioIdAndUsuarioId(desafioId, usuarioId)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException(
                        "El usuario no participa en este desafío"));
    }

    private ParticipacionDesafio nuevaParticipacion(Desafio desafio, Usuario usuario) {
        ParticipacionDesafio p = new ParticipacionDesafio();
        p.setDesafio(desafio);
        p.setUsuario(usuario);
        p.setFechaInscripcion(new Date());
        p.setEstadoProgreso(EstadoProgreso.ACTIVO);
        p.setPuntosGanadosEnDesafio(0);
        p.setRachaActual(0);
        p.setRachaMaxima(0);
        return p;
    }

    private List<ParticipacionDesafioDTO> mapearConPosicion(List<ParticipacionDesafio> participaciones) {
        return java.util.stream.IntStream.range(0, participaciones.size())
                .mapToObj(i -> toDTO(participaciones.get(i), i + 1))
                .collect(Collectors.toList());
    }

    /**
     * Convierte la entidad {@link ParticipacionDesafio} al DTO de respuesta.
     * Incluye nombre y foto del usuario para evitar llamadas adicionales al endpoint de perfil.
     * Calcula completadoHoy consultando el registro del día actual.
     *
     * @param p entidad origen
     * @param posicion posición en el ranking (1-indexed) o null si no aplica
     * @return DTO con métricas y datos del usuario rellenados
     */
    public ParticipacionDesafioDTO toDTO(ParticipacionDesafio p, Integer posicion) {
        ParticipacionDesafioDTO dto = new ParticipacionDesafioDTO();
        dto.setId(p.getId());
        dto.setFechaInscripcion(p.getFechaInscripcion());
        dto.setEstadoProgreso(p.getEstadoProgreso());
        dto.setPuntosGanadosEnDesafio(p.getPuntosGanadosEnDesafio());
        dto.setRachaActual(p.getRachaActual());
        dto.setRachaMaxima(p.getRachaMaxima());
        dto.setUsuarioId(p.getUsuario().getId());
        dto.setUsuarioNombre(p.getUsuario().getNombre());
        dto.setUsuarioFoto(p.getUsuario().getFoto());
        dto.setDesafioId(p.getDesafio().getId());
        dto.setPosicion(posicion);
        dto.setCompletadoHoy(estaCompletadoHoy(p));
        int rachaProyectada = rachaSiCompletaHoy(p);
        dto.setPuntosSiguientes(Scoring.puntosDia(rachaProyectada));
        dto.setMultiplicadorSiguiente(Scoring.multiplicador(rachaProyectada));
        return dto;
    }

    private boolean estaCompletadoHoy(ParticipacionDesafio p) {
        return registroDesafioRepository
                .findByDesafio_IdAndUsuario_IdAndFecha(p.getDesafio().getId(), p.getUsuario().getId(), LocalDate.now())
                .map(r -> r.getEstado() == EstadoHabito.COMPLETADO)
                .orElse(false);
    }

    /**
     * Calcula la racha que tendría el participante si completase hoy ahora mismo.
     *
     * Usa los registros reales (no {@code rachaActual} de la entidad, que puede estar
     * desincronizada con datos seed o INSERT manuales). Si hoy ya está marcado, devuelve
     * la racha actual para que la UI no engañe ofreciendo más puntos.
     *
     * @param p participación a evaluar
     * @return racha proyectada si se completase hoy
     */
    private int rachaSiCompletaHoy(ParticipacionDesafio p) {
        Desafio desafio = p.getDesafio();
        Long usuarioId = p.getUsuario().getId();
        Integer desafioId = desafio.getId();
        LocalDate hoy = LocalDate.now();

        Set<DayOfWeek> dowProgramados = (desafio.getFrecuencia() == Frecuencia.PERSONALIZADO
                && desafio.getDiasSemana() != null && !desafio.getDiasSemana().isEmpty())
                ? desafio.getDiasSemana().stream().map(d -> DayOfWeek.of(d.ordinal() + 1)).collect(Collectors.toSet())
                : null;

        if (dowProgramados != null && !dowProgramados.contains(hoy.getDayOfWeek())) {
            return 0;
        }

        LocalDate cursor = hoy.minusDays(1);
        if (dowProgramados != null) {
            int guard = 0;
            while (!dowProgramados.contains(cursor.getDayOfWeek()) && guard++ < 7) {
                cursor = cursor.minusDays(1);
            }
        }

        int racha = 1;
        LocalDate limite = hoy.minusDays(730);
        while (cursor.isAfter(limite)) {
            Optional<RegistroDesafio> reg = registroDesafioRepository
                    .findByDesafio_IdAndUsuario_IdAndFecha(desafioId, usuarioId, cursor);
            if (reg.isPresent() && reg.get().getEstado() == EstadoHabito.COMPLETADO) {
                racha++;
                cursor = cursor.minusDays(1);
                if (dowProgramados != null) {
                    int guard = 0;
                    while (!dowProgramados.contains(cursor.getDayOfWeek()) && guard++ < 7) {
                        cursor = cursor.minusDays(1);
                    }
                }
            } else {
                break;
            }
        }
        return racha;
    }

    private RegistroDesafioDTO toRegistroDTO(RegistroDesafio r) {
        RegistroDesafioDTO dto = new RegistroDesafioDTO();
        dto.setUsuarioId(r.getUsuario().getId());
        dto.setFecha(r.getFecha());
        dto.setEstado(r.getEstado());
        dto.setPuntosGanados(r.getPuntosGanados());
        return dto;
    }
}
