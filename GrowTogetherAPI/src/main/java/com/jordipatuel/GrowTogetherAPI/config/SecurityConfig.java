package com.jordipatuel.GrowTogetherAPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso solo lectura a STANDARD y ADMIN
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, Config.API_URL + "/**").permitAll()
                        // Permitir votar a STANDARD y ADMIN (Excepción específica antes de la regla general de ADMIN)
                        .requestMatchers(HttpMethod.POST, Config.API_URL + "/frases/*/valoraciones").hasAnyRole("STANDARD", "ADMIN")
                        // Permitir acceso total a ADMIN para crear, actualizar y borrar
                        .requestMatchers(HttpMethod.POST, Config.API_URL + "/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, Config.API_URL + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, Config.API_URL + "/**").hasRole("ADMIN")
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Utilizar autenticación básica para la API REST
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();
    }
} 
