package dev.xiyo.pokerhole.core.domain.player.vo;

/**
 * 포커 플레이어 상태
 * Texas Hold'em 게임 중 플레이어의 현재 상태를 나타냅니다.
 */
public enum PlayerStatus {
    WAITING("Waiting", "대기 중 - 게임 시작 전"),
    ACTIVE("Active", "활성 - 게임 참여 중, 액션 가능"),
    FOLDED("Folded", "폴드 - 이번 라운드 포기"),
    ALL_IN("All-in", "올인 - 모든 칩 베팅, 액션 불가"),
    OUT("Out", "탈락 - 칩 소진, 게임 종료");

    private final String displayName;
    private final String description;

    PlayerStatus(String displayName, String description) {
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
     * 액션을 취할 수 있는 상태인지 확인
     * @return 액션 가능하면 true
     */
    public boolean canAct() {
        return this == ACTIVE;
    }

    /**
     * 게임에 참여 중인 상태인지 확인 (승자 결정 대상)
     * @return 게임 참여 중이면 true
     */
    public boolean isInGame() {
        return this == ACTIVE || this == ALL_IN;
    }

    /**
     * 게임에서 탈락한 상태인지 확인
     * @return 탈락했으면 true
     */
    public boolean isEliminated() {
        return this == FOLDED || this == OUT;
    }
}
