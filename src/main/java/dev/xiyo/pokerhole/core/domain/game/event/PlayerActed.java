package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.game.vo.BettingRound;
import dev.xiyo.pokerhole.core.domain.game.vo.PlayerAction;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;

import java.time.Instant;

/**
 * 플레이어 액션 이벤트
 * Texas Hold'em에서 플레이어가 액션(FOLD/CHECK/CALL/BET/RAISE/ALL_IN)을 취했을 때 발생합니다.
 */
public record PlayerActed(
    GameId gameId,
    PlayerId playerId,
    PlayerAction action,
    long amount,
    BettingRound bettingRound,
    Instant occurredAt
) implements GameEvent {

    /**
     * 편의 생성자 (occurredAt 자동 설정)
     */
    public PlayerActed(GameId gameId, PlayerId playerId, PlayerAction action, long amount, BettingRound bettingRound) {
        this(gameId, playerId, action, amount, bettingRound, Instant.now());
    }
}
