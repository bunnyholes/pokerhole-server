package dev.xiyo.pokerhole.core.application.port.out.ai;

import dev.xiyo.pokerhole.core.domain.ai.AIDecision;
import dev.xiyo.pokerhole.core.domain.ai.AIPlayer;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategy;

/**
 * AI 전략 Port (출력 포트)
 */
public interface AIStrategyPort {

    /**
     * AI 결정 실행
     */
    AIDecision executeStrategy(AIPlayer player, AIStrategy.GameContext context);

    /**
     * AI 전략 조회
     */
    AIStrategy getStrategy(AIPlayer player);
}
