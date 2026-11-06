package com.studio.tattoo.model.appointment;

import com.studio.tattoo.exception.BusinessRuleException;
import com.studio.tattoo.model.appointment.dto.AppointmentResponseDTO;
import com.studio.tattoo.model.appointment.dto.CreateAppointmentDTO;
import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistRepository;
import com.studio.tattoo.model.client.Client;
import com.studio.tattoo.model.client.ClientRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private TattooArtistRepository tattooArtistRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User clientUser;
    private User artistUser;
    private Client client;
    private TattooArtist artist;
    private UUID artistId;
    private UUID clientId;
    private UUID appointmentId;

    @BeforeEach
    void setUp() {
        artistId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        clientUser = User.builder().id(UUID.randomUUID()).name("João Cliente").email("joao@gmail.com").role(Role.ROLE_CLIENT).build();
        artistUser = User.builder().id(UUID.randomUUID()).name("Ana Tattoo").email("ana@studio.com").role(Role.ROLE_ARTIST).build();

        client = Client.builder().id(clientId).user(clientUser).name("João Cliente").phone("11999998888").build();
        artist = TattooArtist.builder().id(artistId).user(artistUser).name("Ana Tattoo").specialty("Fineline").build();
    }

    @Test
    @DisplayName("Deve criar um agendamento com sucesso quando não há conflito de horários")
    void shouldCreateAppointmentSuccessfully() {
        LocalDateTime sessionDate = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        CreateAppointmentDTO dto = new CreateAppointmentDTO(artistId, sessionDate, 120, "Tatuagem dragão no braço");

        when(clientRepository.findByUserEmail(clientUser.getEmail())).thenReturn(Optional.of(client));
        when(tattooArtistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(appointmentRepository.findByArtistIdAndSessionDateBetween(eq(artistId), any(), any()))
                .thenReturn(Collections.emptyList());

        Appointment savedAppointment = Appointment.builder()
                .id(appointmentId)
                .sessionDate(sessionDate)
                .estimatedDurationMinutes(120)
                .status(AppointmentStatus.PENDING)
                .designSpecifications("Tatuagem dragão no braço")
                .client(client)
                .artist(artist)
                .createdAt(LocalDateTime.now())
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentResponseDTO response = appointmentService.create(dto, clientUser);

        assertNotNull(response);
        assertEquals(appointmentId, response.id());
        assertEquals(AppointmentStatus.PENDING, response.status());
        assertEquals("João Cliente", response.clientName());
        assertEquals("Ana Tattoo", response.artistName());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando houver conflito de horário na agenda do tatuador")
    void shouldThrowExceptionWhenScheduleConflicts() {
        LocalDateTime sessionDate = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        CreateAppointmentDTO dto = new CreateAppointmentDTO(artistId, sessionDate, 120, "Tatuagem floral");

        Appointment conflictingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .sessionDate(sessionDate.plusMinutes(30))
                .estimatedDurationMinutes(60)
                .status(AppointmentStatus.CONFIRMED)
                .artist(artist)
                .build();

        when(clientRepository.findByUserEmail(clientUser.getEmail())).thenReturn(Optional.of(client));
        when(tattooArtistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(appointmentRepository.findByArtistIdAndSessionDateBetween(eq(artistId), any(), any()))
                .thenReturn(List.of(conflictingAppointment));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                appointmentService.create(dto, clientUser)
        );

        assertTrue(ex.getMessage().contains("Conflito de agenda"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Tatuador deve conseguir confirmar um agendamento da sua própria agenda")
    void shouldAllowArtistToConfirmAppointment() {
        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .sessionDate(LocalDateTime.now().plusDays(1))
                .estimatedDurationMinutes(90)
                .status(AppointmentStatus.PENDING)
                .client(client)
                .artist(artist)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDTO response = appointmentService.updateStatus(appointmentId, AppointmentStatus.CONFIRMED, artistUser);

        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Cliente não deve conseguir confirmar um agendamento (apenas o tatuador pode confirmar)")
    void shouldDenyClientFromConfirmingAppointment() {
        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .sessionDate(LocalDateTime.now().plusDays(1))
                .estimatedDurationMinutes(90)
                .status(AppointmentStatus.PENDING)
                .client(client)
                .artist(artist)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(AccessDeniedException.class, () ->
                appointmentService.updateStatus(appointmentId, AppointmentStatus.CONFIRMED, clientUser)
        );

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    @DisplayName("Cliente deve conseguir cancelar seu próprio agendamento")
    void shouldAllowClientToCancelAppointment() {
        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .sessionDate(LocalDateTime.now().plusDays(3))
                .estimatedDurationMinutes(60)
                .status(AppointmentStatus.PENDING)
                .client(client)
                .artist(artist)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDTO response = appointmentService.cancel(appointmentId, clientUser);

        assertEquals(AppointmentStatus.CANCELLED, response.status());
        verify(appointmentRepository).save(appointment);
    }
}
