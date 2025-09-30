package dev.xiyo.pokerhole.core.domain.player;

import dev.xiyo.pokerhole.core.domain.betting.ActionType;
import dev.xiyo.pokerhole.core.domain.betting.PlayerAction;
import dev.xiyo.pokerhole.core.domain.round.RoundGameState;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 컨텍스트 (AI 결정에 필요한 정보)
 */
@Value
@Builder
public class GameContext {
    @NonNull RoundGameState state;
    long pot;
    long currentBet;
    long minimumBet;
    
    @Singular
    List<PlayerAction> recentActions;
    
    public List<ActionType> availableActions(HoldemPlayer player) {
        var actions = new ArrayList<ActionType>();
        
        // 항상 폴드 가능
        actions.add(ActionType.FOLD);
        
        // 체크 가능 조건
        if (state.canCheck(player.getId())) {
            actions.add(ActionType.CHECK);
        }
        
        // 콜 가능 조건
        if (callAmount() > 0 && player.getWallet().getBalance() >= callAmount()) {
            actions.add(ActionType.CALL);
        }
        
        // 베팅/레이즈 가능 조건
        if (state.getCurrentBet() == 0) {
            actions.add(ActionType.BET);
        } else if (player.getWallet().getBalance() > callAmount()) {
            actions.add(ActionType.RAISE);
        }
        
        // 올인 가능
        if (player.getWallet().getBalance() > 0) {
            actions.add(ActionType.ALL_IN);
        }
        
        return actions;
    }
    
    public long callAmount() {
        return state.getCurrentBet();
    }
}
