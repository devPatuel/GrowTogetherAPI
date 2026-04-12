package com.jordipatuel.GrowTogetherAPI.service;

import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de tareas programadas relacionadas con hábitos.
 *
 * Se ejecuta automáticamente cada noche a las 00:01 para rellenar los registros
 * de hábitos que no fueron completados el día anterior, marcándolos como NO_COMPLETADO.
 * Esto garantiza que el historial y las estadísticas sean consistentes sin depender
 * de que el usuario interactúe con la app.
 */
@Service
public class HabitoScheduledService {

    private static final Logger log = LoggerFactory.getLogger(HabitoScheduledService.class);

    private final HabitoRepository habitoRepository;
    private final RegistroHabitoService registroHabitoService;

    public HabitoScheduledService(HabitoRepository habitoRepository,
                                   RegistroHabitoService registroHabitoService) {
        this.habitoRepository = habitoRepository;
        this.registroHabitoService = registroHabitoService;
    }

    /**
     * Tarea programada que se ejecuta a las 00:01 cada día.
     * Recorre todos los hábitos activos y delega en {@link RegistroHabitoService#rellenarNoCompletados}
     * para marcar como NO_COMPLETADO los días sin registro.
     * Los errores en un hábito concreto se logean y no interrumpen el resto del proceso.
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void marcarNoCompletadosDiario() {
        log.info("Ejecutando tarea programada: rellenar hábitos no completados");
        List<Habito> habitos = habitoRepository.findByActivoTrue();
        int total = 0;
        for (Habito habito : habitos) {
            try {
                registroHabitoService.rellenarNoCompletados(habito);
                total++;
            } catch (Exception e) {
                log.error("Error al rellenar hábito {}: {}", habito.getId(), e.getMessage());
            }
        }
        log.info("Tarea completada: {} hábitos procesados", total);
    }
}
