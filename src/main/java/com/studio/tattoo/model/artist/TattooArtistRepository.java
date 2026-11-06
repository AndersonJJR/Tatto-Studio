package com.studio.tattoo.model.artist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TattooArtistRepository extends JpaRepository<TattooArtist, UUID> {

    Optional<TattooArtist> findByUserId(UUID userId);

    Optional<TattooArtist> findByUserEmail(String email);

    List<TattooArtist> findBySpecialtyContainingIgnoreCase(String specialty);
}
