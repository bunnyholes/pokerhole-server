package dev.xiyo.pokerhole.core.application.port.in.ai;

import dev.xiyo.pokerhole.core.domain.ai.AIPlayer;
import dev.xiyo.pokerhole.core.domain.ai.AIStrategyType;

import java.util.List;

/**
 * AI 플레이어 요청 Use Case
 */
public interface RequestAIPlayerUseCase {

    /**
     * AI 플레이어 생성 요청
     *
     * @param command AI 플레이어 요청 명령
     * @return 생성된 AI 플레이어 목록
     */
    List<AIPlayer> requestAIPlayers(RequestAIPlayerCommand command);

    /**
     * AI 플레이어 요청 명령
     */
    record RequestAIPlayerCommand(
            int count,
            AIStrategyType preferredStrategy,
            int initialChips
    ) {
        public static RequestAIPlayerCommand of(int count, int initialChips) {
            return new RequestAIPlayerCommand(count, null, initialChips);
        }
    }
}
