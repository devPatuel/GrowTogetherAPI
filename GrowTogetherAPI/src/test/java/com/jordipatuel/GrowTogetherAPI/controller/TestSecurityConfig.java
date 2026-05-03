package com.jordipatuel.GrowTogetherAPI.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Config de seguridad mínima usada por los @WebMvcTest de controllers protegidos.
 * Permite todas las requests sin autenticación pero deja activo el filtro de
 * Spring Security que carga el SecurityContext desde la sesión, de modo que
 * el parámetro {@code Authentication authentication} del controller llega informado
 * cuando el test usa {@code with(authentication(...))}.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
