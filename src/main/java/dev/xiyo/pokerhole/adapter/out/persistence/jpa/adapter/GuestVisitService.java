package dev.xiyo.pokerhole.adapter.out.persistence.jpa.adapter;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.GuestVisit;
import dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository.GuestVisitRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestVisitService {

    private final GuestVisitRepository repository;

    @Transactional
    public long recordVisit(String sessionId) {
        // PostgreSQL 시퀀스에서 스레드 안전하게 다음 guest_number 획득
        long guestNumber = repository.getNextGuestNumber();

        GuestVisit visit = new GuestVisit();
        visit.setGuestNumber(guestNumber);
        visit.setSessionId(sessionId);
        visit.setConnectedAt(Instant.now());

        repository.save(visit);
        log.info("connected guest#{}", guestNumber);
        return guestNumber;
    }
}
