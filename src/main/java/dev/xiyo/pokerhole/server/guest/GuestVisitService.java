package dev.xiyo.pokerhole.server.guest;

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
    public synchronized long recordVisit(String sessionId) {
        long guestNumber = repository.count() + 1;

        GuestVisit visit = new GuestVisit();
        visit.setGuestNumber(guestNumber);
        visit.setSessionId(sessionId);
        visit.setConnectedAt(Instant.now());

        repository.save(visit);
        log.info("connected guest#{}", guestNumber);
        return guestNumber;
    }
}
