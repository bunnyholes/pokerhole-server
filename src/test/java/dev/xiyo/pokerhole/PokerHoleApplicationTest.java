package dev.xiyo.pokerhole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class PokerHoleApplicationTest {

    @Test
    void printsStartupMessage(CapturedOutput output) {
        assertTrue(
                output.getOut().contains("서버가 실행되엇습니다. !"),
                "서버가 실행되엇습니다. ! 메시지가 애플리케이션 시작 시 출력되어야 합니다."
        );
    }
}
