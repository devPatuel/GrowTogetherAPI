package com.jordipatuel.GrowTogetherAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.JwtAuthenticationFilter;
import com.jordipatuel.GrowTogetherAPI.config.RateLimitFilter;
import com.jordipatuel.GrowTogetherAPI.config.SecurityConfig;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoDTO;
import com.jordipatuel.GrowTogetherAPI.service.HabitoService;
import com.jordipatuel.GrowTogetherAPI.service.RegistroHabitoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del HabitoController. Excluimos SecurityConfig para no evaluar @PreAuthorize:
 * lo cubrimos en el integration test. Aquí verificamos el contrato del controller.
 */
@WebMvcTest(controllers = HabitoController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = { SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitFilter.class }))
@Import(TestSecurityConfig.class)
class HabitoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private HabitoService habitoService;
    @MockBean private RegistroHabitoService registroHabitoService;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        AuthUserDetails principal = new AuthUserDetails(7L, "jordi@example.com", "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_STANDARD")), 0);
        auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private HabitoDTO sampleHabito(int id) {
        return new HabitoDTO(id, "Leer", "20 min", 3, 10, 7L, false,
                "DIARIO", null, "POSITIVO", null, LocalDate.now(), 0.5);
    }

    @Test
    void crear_conDtoValidoYAuth_devuelve201YElHabitoCreado() throws Exception {
        HabitoCreateDTO dto = new HabitoCreateDTO("Leer", "20 min", "DIARIO", null, "POSITIVO", null);
        when(habitoService.crearHabito(any(), eq(7L))).thenReturn(sampleHabito(99));

        mockMvc.perform(post("/api/v1/habitos")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.usuarioId").value(7));
    }

    @Test
    void crear_conNombreVacio_devuelve400PorValidacion() throws Exception {
        HabitoCreateDTO dto = new HabitoCreateDTO("", "Descripción", null, null, null, null);

        mockMvc.perform(post("/api/v1/habitos")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listarPorUsuario_devuelve200ConLaListaDelService() throws Exception {
        when(habitoService.obtenerHabitosPorUsuario(eq(7L), any())).thenReturn(
                List.of(sampleHabito(1), sampleHabito(2)));

        mockMvc.perform(get("/api/v1/habitos/usuario/7")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void completar_devuelve200ConElHabitoActualizado() throws Exception {
        when(habitoService.completarHabito(eq(5), eq(7L), any())).thenReturn(sampleHabito(5));

        mockMvc.perform(post("/api/v1/habitos/5/completar")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void eliminar_devuelve204YLlamaAlServicio() throws Exception {
        mockMvc.perform(delete("/api/v1/habitos/5")
                .with(authentication(auth)))
            .andExpect(status().isNoContent());

        verify(habitoService).eliminarHabito(5);
    }

    @Test
    void descompletar_devuelve200ConElHabitoActualizado() throws Exception {
        when(habitoService.descompletarHabito(eq(5), eq(7L), any())).thenReturn(sampleHabito(5));

        mockMvc.perform(post("/api/v1/habitos/5/descompletar")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5));
    }
}
