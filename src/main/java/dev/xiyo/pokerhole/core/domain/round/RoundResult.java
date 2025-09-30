package dev.xiyo.pokerhole.core.domain.round;

import dev.xiyo.pokerhole.core.domain.player.HoldemPlayer;
import dev.xiyo.pokerhole.core.domain.player.PlayerId;
import dev.xiyo.pokerhole.core.domain.card.Hand;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

/**
 * 라운드 결과
 */
@Value
@Builder
public class RoundResult {
    @NonNull RoundId roundId;
    @NonNull HoldemPlayer winner;
    long winAmount;
    
    @Singular
    Map<PlayerId, Hand> playerHands;
}
