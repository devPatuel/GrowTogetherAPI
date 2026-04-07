package com.jordipatuel.GrowTogetherAPI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class GrowTogetherApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(GrowTogetherApiApplication.class, args);
	}
}
