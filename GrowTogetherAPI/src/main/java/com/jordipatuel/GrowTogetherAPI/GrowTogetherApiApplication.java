package com.jordipatuel.GrowTogetherAPI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Punto de entrada de la aplicación Spring Boot.
 *
 * {@code @EnableScheduling} activa el planificador de tareas necesario para que
 * {@link com.jordipatuel.GrowTogetherAPI.service.HabitoScheduledService} ejecute
 * el relleno nocturno de NO_COMPLETADO.
 */
@SpringBootApplication
@EnableScheduling
public class GrowTogetherApiApplication {
	/**
	 * Arranca la aplicación Spring Boot.
	 *
	 * @param args argumentos de línea de comandos pasados al arrancar
	 */
	public static void main(String[] args) {
		SpringApplication.run(GrowTogetherApiApplication.class, args);
	}
}
