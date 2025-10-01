package dev.xiyo.pokerhole.configuration;

import dev.xiyo.pokerhole.adapter.out.ai.RuleBasedAIStrategy;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategy;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * AI 설정
 * AI 전략 빈 등록
 */
@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public Map<AIStrategyType, AIStrategy> aiStrategies() {
        Map<AIStrategyType, AIStrategy> strategies = Map.of(
                AIStrategyType.CONSERVATIVE, new RuleBasedAIStrategy.Conservative(),
                AIStrategyType.AGGRESSIVE, new RuleBasedAIStrategy.Aggressive(),
                AIStrategyType.RANDOM, new RuleBasedAIStrategy.RandomStrategy()
        );

        log.info("AI 전략 등록 완료: {} 개", strategies.size());
        return strategies;
    }
}
