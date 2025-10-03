package dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.GuestVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface GuestVisitRepository extends JpaRepository<GuestVisit, UUID> {

    /**
     * PostgreSQL 시퀀스에서 다음 guest_number 값을 가져옵니다.
     * 이 메서드는 스레드 안전하며, 동시 호출 시에도 고유한 값을 보장합니다.
     *
     * @return 다음 guest_number
     */
    @Query(value = "SELECT nextval('guest_number_seq')", nativeQuery = true)
    Long getNextGuestNumber();
}
