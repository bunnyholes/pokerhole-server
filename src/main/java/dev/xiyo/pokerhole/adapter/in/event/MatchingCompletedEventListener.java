package dev.xiyo.pokerhole.adapter.in.event;

import dev.xiyo.pokerhole.adapter.in.websocket.service.GameCommandService;
import dev.xiyo.pokerhole.core.domain.matching.event.MatchingCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 매칭 완료 이벤트 리스너
 * MatchingCompleted 도메인 이벤트를 듣고 게임을 시작합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingCompletedEventListener {

    private final GameCommandService gameCommandService;

    /**
     * 매칭 완료 이벤트 처리
     *
     * @param event 매칭 완료 이벤트
     */
    @Async
    @EventListener
    public void handleMatchingCompleted(MatchingCompleted event) {
        log.info("🎮 매칭 완료 이벤트 수신: gameSessionId={}, players={}",
                event.getGameSessionId(), event.getMatchedRequests().size());

        try {
            // GameCommandService를 통해 게임 생성 및 시작
            String roomId = gameCommandService.createAndStartGame(
                    event.getGameSessionId(),
                    event.getMatchedRequests()
            );

            log.info("✅ 게임 시작 성공: roomId={}, gameSessionId={}",
                    roomId, event.getGameSessionId());

        } catch (Exception e) {
            log.error("❌ 게임 시작 실패: gameSessionId={}", event.getGameSessionId(), e);
        }
    }
}
