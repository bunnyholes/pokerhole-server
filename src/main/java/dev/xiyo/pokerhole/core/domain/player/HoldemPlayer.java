package dev.xiyo.pokerhole.core.domain.player;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Hand;
import dev.xiyo.pokerhole.core.domain.wallet.Wallet;
import dev.xiyo.pokerhole.core.common.exception.GameException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * 홀덤 플레이어 (베팅 기능 포함)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class HoldemPlayer {
    @NonNull
    private final PlayerId id;
    @NonNull
    private final PlayerProfile profile;
    private Wallet wallet;
    private final Hand hand;
    private boolean handOpen;
    
    public static HoldemPlayer create(PlayerId id, PlayerProfile profile, Wallet wallet) {
        return new HoldemPlayer(id, profile, wallet, new Hand(), false);
    }
    
    public void receiveCard(Card card) {
        hand.add(card);
    }
    
    public void openHand() {
        if (hand.iterator().hasNext()) {
            hand.open();
            handOpen = true;
        }
    }
    
    public void deductChips(long amount) {
        if (!wallet.canAfford(amount)) {
            throw new GameException("칩이 부족합니다");
        }
        wallet = wallet.deduct(amount);
    }
    
    public void addChips(long amount) {
        wallet = wallet.add(amount);
    }
    
    public void clearHand() {
        hand.clear();
        handOpen = false;
    }
}
