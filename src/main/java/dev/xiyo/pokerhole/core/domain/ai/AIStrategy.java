package dev.xiyo.pokerhole.core.domain.ai;

import dev.xiyo.pokerhole.core.domain.card.Hand;

/**
 * AI 전략 인터페이스
 * Strategy 패턴으로 다양한 AI 행동 구현
 */
public interface AIStrategy {

    /**
     * AI 결정 생성
     *
     * @param context 게임 컨텍스트
     * @return AI 결정
     */
    AIDecision decide(GameContext context);

    /**
     * 전략 타입 반환
     */
    AIStrategyType getType();

    /**
     * 게임 컨텍스트
     * AI가 결정을 내리기 위해 필요한 정보
     */
    record GameContext(
            Hand hand,
            int currentPot,
            int currentBet,
            int myChips,
            int minimumRaise,
            int opponentCount
    ) {
        /**
         * 핸드 강도 평가 (0.0 ~ 1.0)
         */
        public double getHandStrength() {
            if (hand == null) return 0.0;
            // 간단한 강도 평가 (실제 게임에서는 Hand.rank()를 사용)
            return 0.5; // 기본값
        }

        /**
         * 팟 오즈 계산
         */
        public double getPotOdds() {
            if (currentBet == 0) return 0.0;
            return (double) currentBet / (currentPot + currentBet);
        }
    }
}
