package dev.xiyo.pokerhole.core.domain.game.vo;

/**
 * 팟(Pot) - 게임에 걸린 총 금액
 * 불변 객체로 게임의 팟을 표현합니다.
 */
public record Pot(long amount) {
    
    public Pot {
        if (amount < 0) {
            throw new IllegalArgumentException("Pot amount cannot be negative");
        }
    }
    
    /**
     * 빈 팟 생성
     */
    public static Pot empty() {
        return new Pot(0);
    }
    
    /**
     * 팟에 금액 추가
     */
    public Pot add(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative amount to pot");
        }
        return new Pot(this.amount + amount);
    }
    
    /**
     * 팟이 비어있는지 확인
     */
    public boolean isEmpty() {
        return amount == 0;
    }
    
    @Override
    public String toString() {
        return String.valueOf(amount);
    }
}
