package dev.xiyo.pokerhole.core.domain.ai;

import lombok.Builder;
import lombok.Value;

/**
 * AI 결정 Value Object
 * AI가 내린 게임 액션 결정
 */
@Value
@Builder
public class AIDecision {

    /**
     * 액션 타입
     */
    ActionType action;

    /**
     * 베팅 금액 (RAISE, BET인 경우)
     */
    int amount;

    /**
     * 결정 이유 (로깅/디버깅용)
     */
    String reason;

    /**
     * 결정 신뢰도 (0.0 ~ 1.0)
     */
    double confidence;

    /**
     * 폴드 결정
     */
    public static AIDecision fold(String reason) {
        return AIDecision.builder()
                .action(ActionType.FOLD)
                .amount(0)
                .reason(reason)
                .confidence(1.0)
                .build();
    }

    /**
     * 체크 결정
     */
    public static AIDecision check(String reason) {
        return AIDecision.builder()
                .action(ActionType.CHECK)
                .amount(0)
                .reason(reason)
                .confidence(0.8)
                .build();
    }

    /**
     * 콜 결정
     */
    public static AIDecision call(int amount, String reason) {
        return AIDecision.builder()
                .action(ActionType.CALL)
                .amount(amount)
                .reason(reason)
                .confidence(0.7)
                .build();
    }

    /**
     * 베팅 결정
     */
    public static AIDecision bet(int amount, String reason, double confidence) {
        return AIDecision.builder()
                .action(ActionType.BET)
                .amount(amount)
                .reason(reason)
                .confidence(confidence)
                .build();
    }

    /**
     * 레이즈 결정
     */
    public static AIDecision raise(int amount, String reason, double confidence) {
        return AIDecision.builder()
                .action(ActionType.RAISE)
                .amount(amount)
                .reason(reason)
                .confidence(confidence)
                .build();
    }

    /**
     * 액션 타입
     */
    public enum ActionType {
        FOLD,
        CHECK,
        CALL,
        BET,
        RAISE
    }
}
