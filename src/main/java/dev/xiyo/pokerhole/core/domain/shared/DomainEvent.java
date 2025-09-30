package dev.xiyo.pokerhole.core.domain.shared;

import java.time.Instant;

/**
 * 마커 인터페이스: 도메인 이벤트를 나타냅니다.
 * 모든 도메인 이벤트는 이 인터페이스를 구현해야 합니다.
 */
public interface DomainEvent {
    /**
     * 이벤트 발생 시각
     */
    Instant occurredAt();
}
