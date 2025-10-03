package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;

import java.time.Instant;

/**
 * 턴 변경 이벤트
 * Texas Hold'em에서 플레이어의 턴이 변경될 때 발생합니다.
 */
public record TurnChanged(
    GameId gameId,
    PlayerId currentPlayerId,
    int timeoutSeconds,
    Instant occurredAt
) implements GameEvent {

    /**
     * 편의 생성자 (occurredAt 자동 설정)
     */
    public TurnChanged(GameId gameId, PlayerId currentPlayerId, int timeoutSeconds) {
        this(gameId, currentPlayerId, timeoutSeconds, Instant.now());
    }

    /**
     * 기본 타임아웃 30초로 생성
     */
    public TurnChanged(GameId gameId, PlayerId currentPlayerId) {
        this(gameId, currentPlayerId, 30);
    }
}
