package com.jordipatuel.GrowTogetherAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;
import com.jordipatuel.GrowTogetherAPI.config.JwtAuthenticationFilter;
import com.jordipatuel.GrowTogetherAPI.config.RateLimitFilter;
import com.jordipatuel.GrowTogetherAPI.config.SecurityConfig;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.DesafioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.ParticipacionDesafioDTO;
import com.jordipatuel.GrowTogetherAPI.model.enums.EstadoProgreso;
import com.jordipatuel.GrowTogetherAPI.service.DesafioService;
import com.jordipatuel.GrowTogetherAPI.service.ParticipacionDesafioService;
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

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del DesafioController. Misma estrategia que HabitoControllerTest:
 * verificamos contrato sin Spring Security real, los @PreAuthorize se cubren
 * en el integration test.
 */
@WebMvcTest(controllers = DesafioController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = { SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitFilter.class }))
@Import(TestSecurityConfig.class)
class DesafioControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DesafioService desafioService;
    @MockBean private ParticipacionDesafioService participacionDesafioService;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        AuthUserDetails principal = new AuthUserDetails(7L, "jordi@example.com", "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_STANDARD")), 0);
        auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private DesafioDTO sampleDesafio(int id) {
        return new DesafioDTO(id, "Reto " + id, "descripción", null,
                new Date(), new Date(System.currentTimeMillis() + 86_400_000L * 30),
                true, "DIARIO", null, "POSITIVO", null,
                7L, "Jordi", List.of());
    }

    private ParticipacionDesafioDTO sampleParticipacion(long id, int desafioId) {
        return new ParticipacionDesafioDTO(id, new Date(), EstadoProgreso.ACTIVO,
                0, 0, 0, false, null, 7L, "Jordi", null, desafioId, 10, 1.0);
    }

    @Test
    void verMisDesafios_devuelve200ConLaListaDelService() throws Exception {
        when(desafioService.obtenerMisDesafios(7L))
                .thenReturn(List.of(sampleDesafio(1), sampleDesafio(2)));

        mockMvc.perform(get("/api/v1/desafios/mios")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void crear_conDtoValido_devuelve201ConElDesafioCreado() throws Exception {
        DesafioCreateDTO dto = new DesafioCreateDTO(
                "Reto", "Test", null,
                new Date(), new Date(System.currentTimeMillis() + 86_400_000L * 30),
                "DIARIO", null, "POSITIVO", null, null);
        when(desafioService.crearDesafio(any(), eq(7L))).thenReturn(sampleDesafio(100));

        mockMvc.perform(post("/api/v1/desafios")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void crear_conNombreVacio_devuelve400PorValidacion() throws Exception {
        DesafioCreateDTO dto = new DesafioCreateDTO(
                "", "Test", null,
                new Date(), new Date(System.currentTimeMillis() + 86_400_000L * 30),
                null, null, null, null, null);

        mockMvc.perform(post("/api/v1/desafios")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unirse_devuelve201ConLaParticipacion() throws Exception {
        when(participacionDesafioService.unirseADesafio(9, 7L))
                .thenReturn(sampleParticipacion(50L, 9));

        mockMvc.perform(post("/api/v1/desafios/9/unirse")
                .with(authentication(auth)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(50))
            .andExpect(jsonPath("$.desafioId").value(9));
    }

    @Test
    void abandonar_devuelve200ConLaParticipacionActualizada() throws Exception {
        ParticipacionDesafioDTO abandonada = sampleParticipacion(50L, 9);
        abandonada.setEstadoProgreso(EstadoProgreso.ABANDONADO);
        when(participacionDesafioService.abandonarDesafio(9, 7L)).thenReturn(abandonada);

        mockMvc.perform(delete("/api/v1/desafios/9/abandonar")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estadoProgreso").value("ABANDONADO"));
    }

    @Test
    void verDetalle_devuelve200ConElDesafio() throws Exception {
        when(desafioService.obtenerPorId(9)).thenReturn(sampleDesafio(9));

        mockMvc.perform(get("/api/v1/desafios/9")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(9));
    }
}
