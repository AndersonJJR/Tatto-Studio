package com.studio.tattoo.model.artist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TattooArtistRepository extends JpaRepository<TattooArtist, Long> {

    Optional<TattooArtist> findByUserId(Long userId);

    Optional<TattooArtist> findByUserEmail(String email);

    List<TattooArtist> findBySpecialtyContainingIgnoreCase(String specialty);
}
