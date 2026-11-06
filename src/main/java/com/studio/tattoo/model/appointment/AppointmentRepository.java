package com.studio.tattoo.model.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByArtistId(Long artistId);

    List<Appointment> findByArtistIdAndStatus(Long artistId, AppointmentStatus status);

    List<Appointment> findByArtistIdAndSessionDateBetween(
            Long artistId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
