package dev.xiyo.pokerhole.core.domain.game.vo;

/**
 * 포커 플레이어 액션 타입
 * Texas Hold'em에서 플레이어가 취할 수 있는 행동을 정의합니다.
 */
public enum PlayerAction {
    FOLD("Fold", "폴드 - 게임 포기"),
    CHECK("Check", "체크 - 베팅 없이 패스"),
    CALL("Call", "콜 - 현재 베팅에 맞춤"),
    BET("Bet", "베팅 - 처음 베팅"),
    RAISE("Raise", "레이즈 - 기존 베팅보다 높게"),
    ALL_IN("All-in", "올인 - 모든 칩 베팅");

    private final String displayName;
    private final String description;

    PlayerAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 베팅이 필요한 액션인지 확인
     * @return 베팅이 필요하면 true
     */
    public boolean requiresBet() {
        return this == CALL || this == BET || this == RAISE || this == ALL_IN;
    }

    /**
     * 금액 지정이 필요한 액션인지 확인
     * @return 금액 지정이 필요하면 true
     */
    public boolean requiresAmount() {
        return this == BET || this == RAISE;
    }
}
