package com.studio.tattoo.model.artist;

import com.studio.tattoo.model.appointment.Appointment;
import com.studio.tattoo.model.appointment.AppointmentRepository;
import com.studio.tattoo.model.appointment.AppointmentStatus;
import com.studio.tattoo.model.artist.dto.BookedSlotResponseDTO;
import com.studio.tattoo.model.artist.dto.CreateTattooArtistDTO;
import com.studio.tattoo.model.artist.dto.TattooArtistResponseDTO;
import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TattooArtistServiceTest {

    @Mock
    private TattooArtistRepository tattooArtistRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private TattooArtistService tattooArtistService;

    private User artistUser;
    private TattooArtist artist;

    @BeforeEach
    void setUp() {
        artistUser = User.builder()
                .id(UUID.randomUUID())
                .name("Anderson")
                .email("anderson@email.com")
                .role(Role.ROLE_ARTIST)
                .build();

        artist = TattooArtist.builder()
                .id(UUID.randomUUID())
                .name("Anderson Tattoo")
                .phone("11999999999")
                .specialty("Blackwork")
                .bio("Especialista em Blackwork")
                .user(artistUser)
                .build();
    }

    @Test
    @DisplayName("Deve obter o perfil do tatuador autenticado com sucesso via /artists/me")
    void shouldGetProfileForAuthenticatedArtist() {
        when(tattooArtistRepository.findByUserEmail("anderson@email.com"))
                .thenReturn(Optional.of(artist));

        TattooArtistResponseDTO response = tattooArtistService.getProfile(artistUser);

        assertNotNull(response);
        assertEquals(artist.getId(), response.id());
        assertEquals("Anderson Tattoo", response.name());
        assertEquals("anderson@email.com", response.email());
        assertEquals("Blackwork", response.specialty());
        verify(tattooArtistRepository, times(1)).findByUserEmail("anderson@email.com");
    }

    @Test
    @DisplayName("Deve atualizar o perfil do tatuador autenticado com sucesso")
    void shouldUpdateProfileForAuthenticatedArtist() {
        CreateTattooArtistDTO dto = new CreateTattooArtistDTO(
                "Anderson Updated",
                "11988888888",
                "Realismo",
                "Nova bio de realismo"
        );

        when(tattooArtistRepository.findByUserEmail("anderson@email.com"))
                .thenReturn(Optional.of(artist));
        when(tattooArtistRepository.save(any(TattooArtist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TattooArtistResponseDTO response = tattooArtistService.updateProfile(dto, artistUser);

        assertNotNull(response);
        assertEquals("Anderson Updated", response.name());
        assertEquals("11988888888", response.phone());
        assertEquals("Realismo", response.specialty());
        assertEquals("Nova bio de realismo", response.bio());
        verify(tattooArtistRepository, times(1)).save(artist);
    }

    @Test
    @DisplayName("Deve retornar horários ocupados (booked slots) excluindo cancelados")
    void shouldFindBookedSlots() {
        UUID artistId = artist.getId();
        Appointment activeAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .artist(artist)
                .sessionDate(LocalDateTime.now().plusDays(1))
                .estimatedDurationMinutes(120)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        Appointment cancelledAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .artist(artist)
                .sessionDate(LocalDateTime.now().plusDays(2))
                .estimatedDurationMinutes(60)
                .status(AppointmentStatus.CANCELLED)
                .build();

        when(tattooArtistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(appointmentRepository.findByArtistId(artistId))
                .thenReturn(List.of(activeAppointment, cancelledAppointment));

        List<BookedSlotResponseDTO> slots = tattooArtistService.findBookedSlots(artistId, null, null);

        assertNotNull(slots);
        assertEquals(1, slots.size());
        assertEquals(activeAppointment.getSessionDate(), slots.get(0).sessionDate());
        assertEquals(120, slots.get(0).estimatedDurationMinutes());
    }
}
