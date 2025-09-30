package dev.xiyo.pokerhole.core.domain.ai;

import dev.xiyo.pokerhole.core.domain.player.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI 플레이어 테스트
 */
class AIPlayerTest {

    @Test
    void AI_플레이어_생성_및_확인() {
        // When
        AIPlayer aiPlayer = AIPlayer.createRandom();

        // Then
        assertThat(aiPlayer).isNotNull();
        assertThat(aiPlayer.getNickName()).startsWith("AI_");
        assertThat(aiPlayer.getPersona()).isEqualTo(AIPersona.RANDOM);
        assertThat(AIPlayer.isAI(aiPlayer)).isTrue();
    }

    @Test
    void 일반_플레이어는_AI가_아님() {
        // Given
        String uniqueName = "Test_" + UUID.randomUUID().toString().substring(0, 8);
        Player normalPlayer = Player.newPlayer(uniqueName);

        // Then
        assertThat(AIPlayer.isAI(normalPlayer)).isFalse();
    }

    @Test
    void AI_플레이어는_Player의_모든_기능_사용_가능() {
        // Given
        AIPlayer aiPlayer = AIPlayer.createRandom();

        // When & Then: 기본 플레이어 기능 사용
        assertThat(aiPlayer.getPoint()).isEqualTo(10_000);
        assertThat(aiPlayer.getWins()).isZero();
        assertThat(aiPlayer.getLosses()).isZero();
        
        // 승리 기록
        aiPlayer.win();
        assertThat(aiPlayer.getWins()).isEqualTo(1);
        
        // 포인트 증가
        aiPlayer.prizePoint(100);
        assertThat(aiPlayer.getPoint()).isEqualTo(10_100);
    }

    @Test
    void 다양한_페르소나로_AI_생성() {
        // When
        AIPlayer random = AIPlayer.createWithPersona(AIPersona.RANDOM);
        AIPlayer conservative = AIPlayer.createWithPersona(AIPersona.CONSERVATIVE);
        AIPlayer aggressive = AIPlayer.createWithPersona(AIPersona.AGGRESSIVE);

        // Then
        assertThat(random.getPersona()).isEqualTo(AIPersona.RANDOM);
        assertThat(conservative.getPersona()).isEqualTo(AIPersona.CONSERVATIVE);
        assertThat(aggressive.getPersona()).isEqualTo(AIPersona.AGGRESSIVE);
    }
}
