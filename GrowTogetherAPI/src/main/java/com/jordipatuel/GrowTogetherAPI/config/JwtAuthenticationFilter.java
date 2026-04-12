package com.jordipatuel.GrowTogetherAPI.config;
import com.jordipatuel.GrowTogetherAPI.service.JwtService;
import com.jordipatuel.GrowTogetherAPI.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
/**
 * Filtro que intercepta cada petición HTTP para validar el JWT del header Authorization.
 *
 * Proceso por petición:
 * 1. Si no hay header Authorization o no empieza por "Bearer ", deja pasar sin autenticar.
 * 2. Extrae el email del token y carga el usuario desde {@link UsuarioService}.
 * 3. Valida el token (firma, expiración y tokenVersion).
 * 4. Si es válido, establece la autenticación en el {@link org.springframework.security.core.context.SecurityContextHolder}.
 * Los errores de token expirado o inválido se logean como warning y se deja pasar la petición
 * sin autenticar, permitiendo que Spring Security devuelva 401.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lógica principal del filtro. Se ejecuta una sola vez por petición.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.usuarioService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT token expirado: {}", e.getMessage());
        } catch (io.jsonwebtoken.JwtException e) {
            logger.warn("JWT token inválido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error en filtro JWT: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
