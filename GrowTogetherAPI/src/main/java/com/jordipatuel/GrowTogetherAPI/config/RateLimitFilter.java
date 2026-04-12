package com.jordipatuel.GrowTogetherAPI.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de limitación de peticiones para los endpoints de login y registro.
 *
 * Limita a {@code MAX_ATTEMPTS} (10) peticiones POST por IP en una ventana
 * deslizante de {@code WINDOW_MS} (60 segundos). Si se supera el límite,
 * devuelve HTTP 429 con un mensaje de error.
 *
 * El conteo se almacena en memoria con un {@link ConcurrentHashMap} thread-safe.
 * Limitación: el conteo se pierde al reiniciar el servidor y no funciona
 * en entornos multi-instancia (cada servidor lleva su propio contador independiente).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, RateInfo> attempts = new ConcurrentHashMap<>();

    /**
     * Aplica el rate limiting solo a POST /api/v1/auth/login y /api/v1/auth/registrar.
     * El resto de peticiones pasan directamente.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.contains("/api/v1/auth/login") && !path.contains("/api/v1/auth/registrar")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String key = ip + ":" + path;

        RateInfo info = attempts.compute(key, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateInfo(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (info.count.get() > MAX_ATTEMPTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Demasiados intentos. Espera 1 minuto.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene la IP real del cliente teniendo en cuenta el header X-Forwarded-For
     * en caso de estar detrás de un proxy o balanceador.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** Estructura interna que almacena el inicio de la ventana y el contador de peticiones. */
    private static class RateInfo {
        final long windowStart;
        final AtomicInteger count;

        RateInfo(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
