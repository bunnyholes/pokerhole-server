package dev.xiyo.pokerhole.server.guest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestVisitRepository extends JpaRepository<GuestVisit, UUID> {
}
