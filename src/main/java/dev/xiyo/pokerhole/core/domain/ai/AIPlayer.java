package dev.xiyo.pokerhole.core.domain.ai;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.UUID;

/**
 * AI 플레이어 Entity
 * 게임에 참여하는 AI 플레이어를 표현
 */
@Value
@Builder
@With
public class AIPlayer {

    /**
     * AI 플레이어 고유 ID
     */
    UUID id;

    /**
     * AI 닉네임
     */
    String nickname;

    /**
     * AI 전략 타입
     */
    AIStrategyType strategyType;

    /**
     * AI 난이도 (1-10)
     */
    int difficultyLevel;

    /**
     * 현재 칩 수
     */
    int chips;

    /**
     * 새로운 AI 플레이어 생성
     */
    public static AIPlayer create(AIStrategyType strategyType, int initialChips) {
        return AIPlayer.builder()
                .id(UUID.randomUUID())
                .nickname(generateNickname(strategyType))
                .strategyType(strategyType)
                .difficultyLevel(calculateDifficulty(strategyType))
                .chips(initialChips)
                .build();
    }

    /**
     * 전략 타입에 따른 닉네임 생성
     */
    private static String generateNickname(AIStrategyType strategyType) {
        return switch (strategyType) {
            case CONSERVATIVE -> "보수적AI_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            case AGGRESSIVE -> "공격적AI_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            case RANDOM -> "랜덤AI_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        };
    }

    /**
     * 전략 타입에 따른 난이도 계산
     */
    private static int calculateDifficulty(AIStrategyType strategyType) {
        return switch (strategyType) {
            case CONSERVATIVE -> 5;
            case AGGRESSIVE -> 7;
            case RANDOM -> 3;
        };
    }

    /**
     * 칩 수 업데이트
     */
    public AIPlayer updateChips(int newChips) {
        return this.withChips(newChips);
    }

    /**
     * 파산 여부 확인
     */
    public boolean isBankrupt() {
        return chips <= 0;
    }
}
