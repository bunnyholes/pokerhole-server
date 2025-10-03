package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.game.vo.BettingRound;

import java.time.Instant;
import java.util.List;

/**
 * 베팅 라운드 진행 이벤트
 * Texas Hold'em에서 베팅 라운드가 진행될 때 발생합니다 (PRE_FLOP → FLOP → TURN → RIVER).
 * 커뮤니티 카드가 공개됩니다.
 */
public record RoundProgressed(
    GameId gameId,
    BettingRound fromRound,
    BettingRound toRound,
    List<Card> newCommunityCards,
    Instant occurredAt
) implements GameEvent {

    /**
     * 편의 생성자 (occurredAt 자동 설정)
     */
    public RoundProgressed(GameId gameId, BettingRound fromRound, BettingRound toRound, List<Card> newCommunityCards) {
        this(gameId, fromRound, toRound, List.copyOf(newCommunityCards), Instant.now());
    }

    /**
     * Record canonical constructor - 불변성 보장
     */
    public RoundProgressed {
        newCommunityCards = List.copyOf(newCommunityCards); // 방어적 복사
    }
}
