package dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.GuestVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuestVisitRepository extends JpaRepository<GuestVisit, UUID> {
}
