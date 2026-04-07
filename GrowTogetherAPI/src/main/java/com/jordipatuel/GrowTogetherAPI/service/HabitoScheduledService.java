package com.jordipatuel.GrowTogetherAPI.service;

import com.jordipatuel.GrowTogetherAPI.model.Habito;
import com.jordipatuel.GrowTogetherAPI.repository.HabitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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
