package com.studio.tattoo.model.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByClientId(UUID clientId);

    List<Appointment> findByArtistId(UUID artistId);

    List<Appointment> findByArtistIdAndStatus(UUID artistId, AppointmentStatus status);

    List<Appointment> findByArtistIdAndSessionDateBetween(
            UUID artistId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
