package dev.xiyo.pokerhole.core.domain.round;

import dev.xiyo.pokerhole.core.domain.betting.BettingRound;
import dev.xiyo.pokerhole.core.domain.betting.Position;
import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.player.PlayerId;
import lombok.*;

import java.util.*;

/**
 * 게임 상태 (불변 객체)
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoundGameState {
    @NonNull GameId gameId;
    @NonNull BettingRound currentRound;
    
    @Singular
    List<Card> communityCards;    // 커뮤니티 카드 (최대 5장)
    
    long pot;                      // 팟 금액
    long currentBet;               // 현재 라운드 최고 베팅액
    
    @Singular
    Map<PlayerId, Long> playerBets;  // 각 플레이어의 현재 베팅액
    
    @Singular
    Map<PlayerId, Position> positions; // 플레이어 포지션
    
    @NonNull PlayerId currentTurn;     // 현재 차례인 플레이어
    
    @Singular
    Set<PlayerId> foldedPlayers;      // 폴드한 플레이어
    
    @Singular
    Set<PlayerId> allInPlayers;       // 올인한 플레이어
    
    public RoundGameState nextTurn(PlayerId nextPlayer) {
        return this.toBuilder()
            .currentTurn(nextPlayer)
            .build();
    }
    
    public RoundGameState addToPot(long amount) {
        return this.toBuilder()
            .pot(pot + amount)
            .build();
    }
    
    public RoundGameState updatePlayerBet(PlayerId playerId, long amount) {
        var updated = new HashMap<>(playerBets);
        updated.put(playerId, amount);
        return this.toBuilder()
            .playerBets(updated)
            .build();
    }
    
    public RoundGameState nextRound(BettingRound round) {
        return this.toBuilder()
            .currentRound(round)
            .currentBet(0)
            .playerBets(Map.of())
            .build();
    }
    
    public long getPlayerBet(PlayerId playerId) {
        return playerBets.getOrDefault(playerId, 0L);
    }
    
    public long callAmount(PlayerId playerId) {
        return currentBet - getPlayerBet(playerId);
    }
    
    public boolean canCheck(PlayerId playerId) {
        return getPlayerBet(playerId) == currentBet;
    }
}
