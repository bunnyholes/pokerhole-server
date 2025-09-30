package dev.xiyo.pokerhole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "pokerhole.terminal.enabled=false")
@ExtendWith(OutputCaptureExtension.class)
class PokerHoleApplicationTest {

    @Test
    void printsCustomBannerAndStartupMessage(CapturedOutput output) {
        String consoleOutput = output.getOut();

        assertTrue(
                consoleOutput.contains("XIYO"),
                "서버 시작 시 커스텀 배너인 'XIYO'가 출력되어야 합니다."
        );

        assertTrue(
                consoleOutput.contains("Server is running normally."),
                "애플리케이션이 시작되면 'Server is running normally.' 메시지가 출력되어야 합니다."
        );
    }
}
