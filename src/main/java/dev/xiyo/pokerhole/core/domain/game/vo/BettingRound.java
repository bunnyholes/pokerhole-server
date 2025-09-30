package dev.xiyo.pokerhole.core.domain.game.vo;

/**
 * 포커 베팅 라운드
 */
public enum BettingRound {
    PRE_FLOP("Pre-Flop", "최초 2장 배분 후"),
    FLOP("Flop", "공용 카드 3장 공개"),
    TURN("Turn", "공용 카드 1장 추가"),
    RIVER("River", "공용 카드 1장 추가"),
    SHOWDOWN("Showdown", "최종 승부");
    
    private final String displayName;
    private final String description;
    
    BettingRound(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
