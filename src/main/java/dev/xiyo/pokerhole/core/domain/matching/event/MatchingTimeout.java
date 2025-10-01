package dev.xiyo.pokerhole.core.domain.matching.event;

import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import dev.xiyo.pokerhole.core.domain.shared.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * 매칭 타임아웃 이벤트
 * 10초 이상 대기 중인 요청이 발생했을 때 발행
 */
@Value
public class MatchingTimeout implements DomainEvent {

    /**
     * 타임아웃된 요청들
     */
    List<MatchingRequest> timedOutRequests;

    /**
     * 이벤트 발생 시각
     */
    Instant occurredAt;

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public static MatchingTimeout of(List<MatchingRequest> requests) {
        return new MatchingTimeout(requests, Instant.now());
    }

    /**
     * AI 투입이 필요한 요청 수
     */
    public int getRequiredAICount(int playersPerGame) {
        int totalPlayers = timedOutRequests.size();
        return Math.max(0, playersPerGame - totalPlayers);
    }
}
