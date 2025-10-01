package dev.xiyo.pokerhole.core.application.port.out.matching;

import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;

import java.util.List;

/**
 * 매칭 알림 Port (출력 포트)
 */
public interface MatchingNotificationPort {

    /**
     * 매칭 완료 알림 (게임 로비로 이동)
     */
    void notifyMatchingCompleted(List<MatchingRequest> matchedPlayers);

    /**
     * 매칭 진행 상황 알림
     */
    void notifyMatchingProgress(String sessionId, int currentCount, int requiredCount);

    /**
     * 매칭 취소 알림
     */
    void notifyMatchingCancelled(String sessionId);
}
