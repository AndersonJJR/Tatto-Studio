package com.studio.tattoo.model.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByUserId(UUID userId);

    Optional<Client> findByUserEmail(String email);
}
