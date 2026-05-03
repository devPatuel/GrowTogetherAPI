package com.jordipatuel.GrowTogetherAPI.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
/**
 * Configuración principal de Spring Security.
 *
 * Define la cadena de filtros, las reglas de autorización por endpoint,
 * la política de sesiones (stateless) y el CORS.
 * Registra {@link RateLimitFilter} y {@link JwtAuthenticationFilter} antes
 * del filtro estándar de autenticación de Spring.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOriginPatterns;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad:
     * - CORS habilitado con orígenes permitidos
     * - CSRF desactivado (API stateless)
     * - Sesiones stateless (sin HttpSession)
     * - Rutas públicas: Swagger, /auth/**
     * - Rutas admin: requieren rol ADMIN
     * - Resto: requieren autenticación JWT
     * - Filtros: RateLimitFilter → JwtAuthenticationFilter → resto de la cadena
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    /**
     * Configura los orígenes CORS permitidos. La lista se inyecta desde la
     * propiedad {@code app.cors.allowed-origins} (por defecto en
     * {@code application.properties}, sobreescribible con la variable de
     * entorno {@code CORS_ORIGINS} en producción).
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    /**
     * Expone el {@link AuthenticationManager} como bean para que
     * {@link com.jordipatuel.GrowTogetherAPI.controller.AuthController}
     * pueda usarlo en el proceso de login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
