package dev.xiyo.pokerhole.adapter.in.terminal.handler;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 배너 표시 핸들러
 * 접속 시 3초간 배너 표시
 */
@Slf4j
@Component
public class BannerDisplayHandler {

    private static final int BANNER_DISPLAY_SECONDS = 3;

    private static final String BANNER = """
            ╔═══════════════════════════════════════════════════════════════╗
            ║                                                               ║
            ║   ██████╗  ██████╗ ██╗  ██╗███████╗██████╗ ██╗  ██╗ ██████╗  ║
            ║   ██╔══██╗██╔═══██╗██║ ██╔╝██╔════╝██╔══██╗██║  ██║██╔═══██╗ ║
            ║   ██████╔╝██║   ██║█████╔╝ █████╗  ██████╔╝███████║██║   ██║ ║
            ║   ██╔═══╝ ██║   ██║██╔═██╗ ██╔══╝  ██╔══██╗██╔══██║██║   ██║ ║
            ║   ██║     ╚██████╔╝██║  ██╗███████╗██║  ██║██║  ██║╚██████╔╝ ║
            ║   ╚═╝      ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝  ║
            ║                                                               ║
            ║              🃏 텍사스 홀덤 포커 v2.0 🃏                      ║
            ║                                                               ║
            ║              Powered by Spring Boot 4.0 & Java 21            ║
            ║                                                               ║
            ╚═══════════════════════════════════════════════════════════════╝

            환영합니다! 잠시 후 메인 메뉴로 이동합니다...
            """;

    /**
     * 배너 표시 및 자동 전환
     *
     * @param session 세션 상태
     * @param onComplete 완료 콜백
     */
    public void display(SessionState session, Runnable onComplete) {
        log.info("배너 표시 시작: sessionId={}", session.id());

        session.send(BANNER);

        // 3초 후 자동으로 메인 메뉴로 전환
        CompletableFuture.delayedExecutor(BANNER_DISPLAY_SECONDS, TimeUnit.SECONDS)
                .execute(() -> {
                    log.info("배너 표시 완료, 메인 메뉴로 전환: sessionId={}", session.id());
                    onComplete.run();
                });
    }

    /**
     * 배너 표시 시간 반환 (테스트용)
     */
    public int getBannerDisplaySeconds() {
        return BANNER_DISPLAY_SECONDS;
    }
}
