package dev.xiyo.pokerhole.core.domain.matching.event;

import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import dev.xiyo.pokerhole.core.domain.shared.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 매칭 완료 이벤트
 * 4명이 모여 게임을 시작할 준비가 되었을 때 발행
 */
@Value
public class MatchingCompleted implements DomainEvent {

    /**
     * 게임 세션 ID
     */
    UUID gameSessionId;

    /**
     * 매칭된 요청들 (플레이어 정보)
     */
    List<MatchingRequest> matchedRequests;

    /**
     * 이벤트 발생 시각
     */
    Instant occurredAt;

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public static MatchingCompleted of(List<MatchingRequest> requests) {
        return new MatchingCompleted(
                UUID.randomUUID(),
                requests,
                Instant.now()
        );
    }
}
