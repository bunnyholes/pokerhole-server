package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;

import java.time.Instant;

/**
 * 라운드 시작 이벤트
 */
public record RoundStarted(
    GameId gameId,
    int roundNumber,
    Instant occurredAt
) implements GameEvent {
    
    public RoundStarted(GameId gameId, int roundNumber) {
        this(gameId, roundNumber, Instant.now());
    }
}
