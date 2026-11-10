package com.studio.tattoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.tattoo.model.artist.TattooArtistService;
import com.studio.tattoo.model.artist.dto.BookedSlotResponseDTO;
import com.studio.tattoo.model.artist.dto.CreateTattooArtistDTO;
import com.studio.tattoo.model.artist.dto.TattooArtistResponseDTO;
import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import com.studio.tattoo.security.SecurityConfig;
import com.studio.tattoo.security.SecurityFilter;
import com.studio.tattoo.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TattooArtistController.class)
@Import({SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class TattooArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TattooArtistService tattooArtistService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Test
    @DisplayName("GET /artists/me deve retornar perfil do artista autenticado com status 200")
    void shouldReturnProfileForAuthenticatedArtist() throws Exception {
        UUID artistId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("anderson@email.com")
                .name("Anderson")
                .role(Role.ROLE_ARTIST)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TattooArtistResponseDTO responseDTO = new TattooArtistResponseDTO(
                artistId,
                userId,
                "Anderson Tattoo",
                "anderson@email.com",
                "11999999999",
                "Blackwork",
                "Bio teste"
        );

        when(tattooArtistService.getProfile(any(User.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/artists/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artistId.toString()))
                .andExpect(jsonPath("$.name").value("Anderson Tattoo"))
                .andExpect(jsonPath("$.email").value("anderson@email.com"))
                .andExpect(jsonPath("$.specialty").value("Blackwork"));
    }

    @Test
    @DisplayName("GET /artists/{id}/booked-slots deve retornar lista de horários ocupados")
    void shouldReturnBookedSlots() throws Exception {
        UUID artistId = UUID.randomUUID();
        LocalDateTime sessionDate = LocalDateTime.of(2026, 11, 15, 14, 0);
        BookedSlotResponseDTO slot = new BookedSlotResponseDTO(sessionDate, 120);

        when(tattooArtistService.findBookedSlots(any(UUID.class), any(), any()))
                .thenReturn(List.of(slot));

        mockMvc.perform(get("/artists/{id}/booked-slots", artistId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estimatedDurationMinutes").value(120));
    }
}
