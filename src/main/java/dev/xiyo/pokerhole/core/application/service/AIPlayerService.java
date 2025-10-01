package dev.xiyo.pokerhole.core.application.service;

import dev.xiyo.pokerhole.core.application.UseCase;
import dev.xiyo.pokerhole.core.application.port.in.ai.RequestAIPlayerUseCase;
import dev.xiyo.pokerhole.core.domain.ai.AIPlayer;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategyType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI 플레이어 서비스
 * AI 플레이어 관리 Use Case 구현
 */
@Slf4j
@UseCase
public class AIPlayerService implements RequestAIPlayerUseCase {

    private final Random random = new Random();

    @Override
    public List<AIPlayer> requestAIPlayers(RequestAIPlayerCommand command) {
        log.info("AI 플레이어 {} 명 생성 요청", command.count());

        List<AIPlayer> aiPlayers = new ArrayList<>();

        for (int i = 0; i < command.count(); i++) {
            AIStrategyType strategy = command.preferredStrategy() != null
                    ? command.preferredStrategy()
                    : selectRandomStrategy();

            AIPlayer player = AIPlayer.create(strategy, command.initialChips());
            aiPlayers.add(player);

            log.info("AI 플레이어 생성: id={}, nickname={}, strategy={}",
                    player.getId(), player.getNickname(), player.getStrategyType());
        }

        return aiPlayers;
    }

    /**
     * 랜덤 전략 선택 (균형있는 분포)
     */
    private AIStrategyType selectRandomStrategy() {
        AIStrategyType[] strategies = AIStrategyType.values();
        return strategies[random.nextInt(strategies.length)];
    }
}
