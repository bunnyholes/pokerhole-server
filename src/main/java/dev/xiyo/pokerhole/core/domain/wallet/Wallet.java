package dev.xiyo.pokerhole.core.domain.wallet;

import dev.xiyo.pokerhole.core.common.exception.GameException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 플레이어의 지갑 (칩 관리)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Wallet {
    private long balance;
    
    public static Wallet withInitialAmount(long initialAmount) {
        if (initialAmount < 0) {
            throw new IllegalArgumentException("초기 금액은 0 이상이어야 합니다");
        }
        return new Wallet(initialAmount);
    }
    
    public static Wallet empty() {
        return new Wallet(0);
    }
    
    public Wallet deduct(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("차감 금액은 0 이상이어야 합니다");
        }
        if (amount > balance) {
            throw new GameException("잔액이 부족합니다");
        }
        return new Wallet(balance - amount);
    }
    
    public Wallet add(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("추가 금액은 0 이상이어야 합니다");
        }
        return new Wallet(balance + amount);
    }
    
    public boolean canAfford(long amount) {
        return balance >= amount;
    }
}
