package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;

import java.time.Instant;
import java.util.Map;

/**
 * 팟 분배 이벤트
 * Texas Hold'em에서 라운드 종료 시 팟이 승자들에게 분배될 때 발생합니다.
 * 사이드 팟이 있는 경우 여러 승자가 각각 다른 금액을 받을 수 있습니다.
 */
public record PotDistributed(
    GameId gameId,
    Map<PlayerId, Long> winners,
    long totalPot,
    Instant occurredAt
) implements GameEvent {

    /**
     * 편의 생성자 (occurredAt 자동 설정)
     */
    public PotDistributed(GameId gameId, Map<PlayerId, Long> winners, long totalPot) {
        this(gameId, Map.copyOf(winners), totalPot, Instant.now());
    }

    /**
     * Record canonical constructor - 불변성 보장
     */
    public PotDistributed {
        winners = Map.copyOf(winners); // 방어적 복사
    }
}
