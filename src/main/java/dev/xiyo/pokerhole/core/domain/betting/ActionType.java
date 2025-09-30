package dev.xiyo.pokerhole.core.domain.betting;

/**
 * 플레이어가 수행할 수 있는 액션 타입
 */
public enum ActionType {
    FOLD,    // 폴드 (포기)
    CHECK,   // 체크 (현재 베팅액과 동일할 때 패스)
    CALL,    // 콜 (현재 베팅액만큼 추가)
    BET,     // 베팅 (최초 베팅)
    RAISE,   // 레이즈 (베팅 인상)
    ALL_IN   // 올인 (모든 칩 투입)
}
