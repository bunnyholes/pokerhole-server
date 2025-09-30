package dev.xiyo.pokerhole.core.domain.betting;

import dev.xiyo.pokerhole.core.domain.player.PlayerId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

/**
 * 플레이어의 베팅 액션
 */
@Value
@Builder(toBuilder = true)
public class PlayerAction {
    @NonNull ActionType type;
    @NonNull PlayerId playerId;
    long amount;
    @NonNull Instant timestamp;
    
    public static PlayerAction fold(PlayerId playerId) {
        return PlayerAction.builder()
            .type(ActionType.FOLD)
            .amount(0)
            .playerId(playerId)
            .timestamp(Instant.now())
            .build();
    }
    
    public static PlayerAction check(PlayerId playerId) {
        return PlayerAction.builder()
            .type(ActionType.CHECK)
            .amount(0)
            .playerId(playerId)
            .timestamp(Instant.now())
            .build();
    }
    
    public static PlayerAction call(PlayerId playerId, long amount) {
        return PlayerAction.builder()
            .type(ActionType.CALL)
            .amount(amount)
            .playerId(playerId)
            .timestamp(Instant.now())
            .build();
    }
    
    public static PlayerAction bet(PlayerId playerId, long amount) {
        return PlayerAction.builder()
            .type(ActionType.BET)
            .amount(amount)
            .playerId(playerId)
            .timestamp(Instant.now())
            .build();
    }
    
    public static PlayerAction raise(PlayerId playerId, long amount) {
        return PlayerAction.builder()
            .type(ActionType.RAISE)
            .amount(amount)
            .playerId(playerId)
            .timestamp(Instant.now())
            .build();
    }
    
    public static PlayerAction allIn(PlayerId playerId, long amount) {
        return PlayerAction.builder()
            .type(ActionType.ALL_IN)
            .amount(amount)
            .playerId(playerId)
            .timestamp(Instant.now())
            .build();
    }
}
