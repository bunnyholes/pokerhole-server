package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;

import java.time.Instant;

/**
 * 딜러 버튼 로테이션 이벤트
 * Texas Hold'em에서 새로운 핸드가 시작될 때 딜러 버튼이 시계방향으로 이동합니다.
 */
public record DealerButtonRotated(
    GameId gameId,
    int previousPosition,
    int newPosition,
    int handNumber,
    Instant occurredAt
) implements GameEvent {

    /**
     * 편의 생성자 (occurredAt 자동 설정)
     */
    public DealerButtonRotated(GameId gameId, int previousPosition, int newPosition, int handNumber) {
        this(gameId, previousPosition, newPosition, handNumber, Instant.now());
    }
}
