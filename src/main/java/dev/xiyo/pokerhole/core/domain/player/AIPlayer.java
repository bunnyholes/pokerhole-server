package dev.xiyo.pokerhole.core.domain.player;

import dev.xiyo.pokerhole.core.domain.betting.ActionType;
import dev.xiyo.pokerhole.core.domain.betting.PlayerAction;
import dev.xiyo.pokerhole.core.domain.wallet.Wallet;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI 플레이어
 */
@Getter
public class AIPlayer extends HoldemPlayer {
    private final AIPersona persona;
    
    private AIPlayer(PlayerId id, PlayerProfile profile, Wallet wallet, AIPersona persona) {
        super(id, profile, wallet, new dev.xiyo.pokerhole.core.domain.card.Hand(), false);
        this.persona = persona;
    }
    
    public static AIPlayer createRandom() {
        return new AIPlayer(
            PlayerId.generate(),
            PlayerProfile.bot("Bot_" + System.nanoTime()),
            Wallet.withInitialAmount(10_000),
            AIPersona.RANDOM
        );
    }
    
    public PlayerAction decideAction(GameContext context) {
        return persona.decide(this, context);
    }
}

/**
 * AI 페르소나 (추후 LLM 통합 준비)
 */
enum AIPersona {
    RANDOM {
        @Override
        public PlayerAction decide(AIPlayer player, GameContext context) {
            var available = context.availableActions(player);
            var random = ThreadLocalRandom.current();
            var action = available.get(random.nextInt(available.size()));
            
            return switch (action) {
                case FOLD -> PlayerAction.fold(player.getId());
                case CHECK -> PlayerAction.check(player.getId());
                case CALL -> PlayerAction.call(player.getId(), context.callAmount());
                case BET -> PlayerAction.bet(player.getId(), context.getMinimumBet());
                case RAISE -> PlayerAction.raise(player.getId(), context.getMinimumBet() * 2);
                case ALL_IN -> PlayerAction.allIn(player.getId(), player.getWallet().getBalance());
            };
        }
    };
    
    public abstract PlayerAction decide(AIPlayer player, GameContext context);
}
