package dev.xiyo.pokerhole.adapter.out.persistence.jpa.adapter;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.GuestVisit;
import dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository.GuestVisitRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "pokerhole.terminal.enabled=false")
@ExtendWith(OutputCaptureExtension.class)
class GuestVisitServiceTest {

    @Autowired
    private GuestVisitService guestVisitService;

    @Autowired
    private GuestVisitRepository guestVisitRepository;

    @Test
    void assignsSequentialGuestNumbersAndLogs(CapturedOutput output) {
        long first = guestVisitService.recordVisit("session-1");
        long second = guestVisitService.recordVisit("session-2");

        assertThat(first).isEqualTo(1L);
        assertThat(second).isEqualTo(2L);
        assertThat(guestVisitRepository.count()).isEqualTo(2L);

        String logs = output.getOut();
        assertTrue(logs.contains("connected guest#1"));
        assertTrue(logs.contains("connected guest#2"));
    }
}
