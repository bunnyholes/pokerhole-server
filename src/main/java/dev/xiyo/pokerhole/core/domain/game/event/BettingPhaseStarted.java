package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.game.vo.BettingRound;

import java.time.Instant;

/**
 * 베팅 페이즈 시작 이벤트
 */
public record BettingPhaseStarted(
    GameId gameId,
    BettingRound bettingRound,
    Instant occurredAt
) implements GameEvent {
    
    public BettingPhaseStarted(GameId gameId, BettingRound bettingRound) {
        this(gameId, bettingRound, Instant.now());
    }
}
