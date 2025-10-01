package dev.xiyo.pokerhole.adapter.out.ai;

import dev.xiyo.pokerhole.core.domain.ai.AIDecision;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategy;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Rule-based AI 전략 구현
 * 핸드 강도와 팟 오즈 기반 결정
 */
@Slf4j
@RequiredArgsConstructor
public abstract class RuleBasedAIStrategy implements AIStrategy {

    protected final Random random = new Random();

    /**
     * 보수적 AI 전략
     */
    public static class Conservative extends RuleBasedAIStrategy {
        @Override
        public AIDecision decide(GameContext context) {
            double handStrength = context.getHandStrength();

            // 강한 패 (0.7+)
            if (handStrength >= 0.7) {
                int raiseAmount = context.minimumRaise() + (random.nextInt(2) * context.minimumRaise());
                return AIDecision.raise(raiseAmount, "강한 패", 0.9);
            }

            // 중간 패 (0.4-0.7)
            if (handStrength >= 0.4) {
                if (context.currentBet() == 0) {
                    return AIDecision.check("중간 패, 체크");
                }
                if (context.currentBet() <= context.minimumRaise()) {
                    return AIDecision.call(context.currentBet(), "중간 패, 콜");
                }
            }

            // 약한 패
            return AIDecision.fold("약한 패");
        }

        @Override
        public AIStrategyType getType() {
            return AIStrategyType.CONSERVATIVE;
        }
    }

    /**
     * 공격적 AI 전략
     */
    public static class Aggressive extends RuleBasedAIStrategy {
        @Override
        public AIDecision decide(GameContext context) {
            double handStrength = context.getHandStrength();
            boolean shouldBluff = random.nextDouble() < 0.3; // 30% 블러핑

            // 강한 패 또는 블러핑
            if (handStrength >= 0.5 || shouldBluff) {
                int raiseAmount = context.minimumRaise() * (2 + random.nextInt(3));
                String reason = shouldBluff ? "블러핑" : "공격적 플레이";
                return AIDecision.raise(raiseAmount, reason, shouldBluff ? 0.5 : 0.8);
            }

            // 중간 패
            if (handStrength >= 0.3) {
                if (context.currentBet() <= context.minimumRaise() * 2) {
                    return AIDecision.call(context.currentBet(), "공격적 콜");
                }
            }

            // 약한 패
            return AIDecision.fold("약한 패");
        }

        @Override
        public AIStrategyType getType() {
            return AIStrategyType.AGGRESSIVE;
        }
    }

    /**
     * 랜덤 AI 전략
     */
    public static class RandomStrategy extends RuleBasedAIStrategy {
        @Override
        public AIDecision decide(GameContext context) {
            int action = random.nextInt(100);

            // 30% 폴드
            if (action < 30) {
                return AIDecision.fold("랜덤 폴드");
            }

            // 30% 체크/콜
            if (action < 60) {
                if (context.currentBet() == 0) {
                    return AIDecision.check("랜덤 체크");
                }
                return AIDecision.call(context.currentBet(), "랜덤 콜");
            }

            // 40% 베팅/레이즈
            int raiseAmount = context.minimumRaise() * (1 + random.nextInt(3));
            return AIDecision.raise(raiseAmount, "랜덤 레이즈", 0.5);
        }

        @Override
        public AIStrategyType getType() {
            return AIStrategyType.RANDOM;
        }
    }
}
