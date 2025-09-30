package dev.xiyo.pokerhole.core.domain.ai;

import dev.xiyo.pokerhole.core.domain.player.Player;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

/**
 * AI 플레이어
 * 현재는 랜덤 액션만 지원하며, 추후 페르소나 및 LLM 기반 전략으로 확장 가능
 */
@Getter
public class AIPlayer extends Player {
    private final AIPersona persona;
    
    private AIPlayer(String nickname, AIPersona persona) {
        super(nickname);
        this.persona = persona;
    }
    
    /**
     * 랜덤 AI 플레이어 생성
     */
    public static AIPlayer createRandom() {
        String nickname = "AI_" + System.nanoTime();
        return new AIPlayer(nickname, AIPersona.RANDOM);
    }
    
    /**
     * 특정 페르소나를 가진 AI 플레이어 생성
     */
    public static AIPlayer createWithPersona(AIPersona persona) {
        String nickname = "AI_" + persona.name().substring(0, Math.min(5, persona.name().length())) + "_" + System.nanoTime();
        // 닉네임이 20자를 넘지 않도록 조정
        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }
        return new AIPlayer(nickname, persona);
    }
    
    /**
     * AI가 플레이어인지 확인
     */
    public static boolean isAI(Player player) {
        return player instanceof AIPlayer;
    }
}
