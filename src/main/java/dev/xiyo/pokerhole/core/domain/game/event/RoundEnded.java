package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;

import java.time.Instant;
import java.util.Optional;

/**
 * 라운드 종료 이벤트
 */
public record RoundEnded(
    GameId gameId,
    int roundNumber,
    Optional<PlayerId> winnerId,
    long potAmount,
    Instant occurredAt
) implements GameEvent {
    
    public RoundEnded(GameId gameId, int roundNumber, Optional<PlayerId> winnerId, long potAmount) {
        this(gameId, roundNumber, winnerId, potAmount, Instant.now());
    }
}
