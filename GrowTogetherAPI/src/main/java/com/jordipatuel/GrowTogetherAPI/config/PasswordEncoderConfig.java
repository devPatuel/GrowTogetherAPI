package com.jordipatuel.GrowTogetherAPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del codificador de contraseñas.
 *
 * Separada de {@link SecurityConfig} para evitar dependencias circulares entre beans:
 * {@link com.jordipatuel.GrowTogetherAPI.service.UsuarioService} necesita el {@link PasswordEncoder}
 * y Spring Security necesita {@link com.jordipatuel.GrowTogetherAPI.service.UsuarioService}.
 * Tenerlo en una clase propia rompe el ciclo.
 */
@Configuration
public class PasswordEncoderConfig {

    /** Expone BCrypt como bean de codificación de contraseñas. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
