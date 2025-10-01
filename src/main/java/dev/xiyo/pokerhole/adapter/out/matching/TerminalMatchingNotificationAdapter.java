package dev.xiyo.pokerhole.adapter.out.matching;

import dev.xiyo.pokerhole.adapter.out.network.session.SessionRegistry;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingNotificationPort;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 매칭 알림 어댑터
 * MatchingNotificationPort 구현 (WebSocket 기반)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalMatchingNotificationAdapter implements MatchingNotificationPort {

    private final SessionRegistry sessionRegistry;

    @Override
    public void notifyMatchingCompleted(List<MatchingRequest> matchedPlayers) {
        log.info("✅ 매칭 완료 알림: {} 명", matchedPlayers.size());

        matchedPlayers.forEach(request -> {
            log.info("  → 플레이어: {} (sessionId: {})",
                request.getNickname(), request.getSessionId());

            // 세션에 매칭 완료 메시지 전송
            sessionRegistry.findById(request.getSessionId()).ifPresent(state -> {
                state.send("✅ 매칭이 완료되었습니다!");
                state.send("게임이 곧 시작됩니다...");
            });
        });
    }

    @Override
    public void notifyMatchingProgress(String sessionId, int currentCount, int requiredCount) {
        log.info("⏳ 매칭 진행 상황: sessionId={}, {}/{} 명",
            sessionId, currentCount, requiredCount);

        // 매칭 진행 상황 메시지 전송
        sessionRegistry.findById(sessionId).ifPresent(state -> {
            state.send(String.format("⏳ 매칭 진행 중: %d/%d 명 대기", currentCount, requiredCount));
        });
    }

    @Override
    public void notifyMatchingCancelled(String sessionId) {
        log.info("❌ 매칭 취소 알림: sessionId={}", sessionId);

        // 매칭 취소 메시지 전송
        sessionRegistry.findById(sessionId).ifPresent(state -> {
            state.send("❌ 매칭이 취소되었습니다.");
        });
    }
}
