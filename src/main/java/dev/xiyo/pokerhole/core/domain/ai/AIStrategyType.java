package dev.xiyo.pokerhole.core.domain.ai;

/**
 * AI 전략 타입
 */
public enum AIStrategyType {
    /**
     * 보수적 전략 - 안전한 플레이, 높은 승률
     */
    CONSERVATIVE,

    /**
     * 공격적 전략 - 과감한 베팅, 블러핑
     */
    AGGRESSIVE,

    /**
     * 랜덤 전략 - 예측 불가능한 플레이
     */
    RANDOM
}
