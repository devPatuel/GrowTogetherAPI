package com.jordipatuel.GrowTogetherAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.JwtAuthenticationFilter;
import com.jordipatuel.GrowTogetherAPI.config.RateLimitFilter;
import com.jordipatuel.GrowTogetherAPI.config.SecurityConfig;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.model.enums.Roles;
import com.jordipatuel.GrowTogetherAPI.service.AuditService;
import com.jordipatuel.GrowTogetherAPI.service.JwtService;
import com.jordipatuel.GrowTogetherAPI.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests del AuthController. Excluimos la SecurityConfig real y los filtros de seguridad
 * porque solo nos interesa el contrato del controller, no el stack completo.
 * Los endpoints de /auth son públicos en producción, así que tampoco hace falta autenticación.
 */
@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = { SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UsuarioService usuarioService;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtService jwtService;
    @MockBean private AuditService auditService;

    @Test
    void registrar_conDtoValido_devuelve201YElUsuarioCreado() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Jordi", "jordi@example.com", "Prueba12", null);
        UsuarioDTO creado = new UsuarioDTO(7L, "Jordi", "jordi@example.com",
                Roles.STANDARD, new Date(), 0, null, "CLARO", "es");
        when(usuarioService.registrarUsuario(any())).thenReturn(creado);

        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.email").value("jordi@example.com"));
    }

    @Test
    void registrar_conEmailInvalido_devuelve400() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Jordi", "no-es-email", "Prueba12", null);

        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_conPasswordDebil_devuelve400() throws Exception {
        // Sin mayúscula y sin número (incumple el @Pattern de UsuarioCreateDTO).
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Jordi", "jordi@example.com", "abcdefgh", null);

        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_conCredencialesValidas_devuelve200ConToken() throws Exception {
        AuthUserDetails principal = new AuthUserDetails(7L, "jordi@example.com", "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_STANDARD")), 0);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("token-falso");
        when(usuarioService.obtenerUsuarioPorEmail("jordi@example.com")).thenReturn(
                new UsuarioDTO(7L, "Jordi", "jordi@example.com", Roles.STANDARD,
                        new Date(), 0, null, "CLARO", "es"));

        String body = "{\"email\":\"jordi@example.com\",\"password\":\"Prueba12\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-falso"))
            .andExpect(jsonPath("$.usuarioId").value(7));
    }

    @Test
    void login_conCredencialesInvalidas_devuelve401() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        String body = "{\"email\":\"mal@example.com\",\"password\":\"mal\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    void recuperar_siempreDevuelve200ConMensajeGenerico() throws Exception {
        String body = "{\"email\":\"jordi@example.com\"}";

        mockMvc.perform(post("/api/v1/auth/recuperar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }
}
