package dev.xiyo.pokerhole.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 게임 관련 설정 속성
 */
@Data
@ConfigurationProperties(prefix = "pokerhole.game")
public class GameProperties {
    
    /**
     * 초기 플레이어 칩 수
     */
    private int initialChips = 10_000;
    
    /**
     * 최소 베팅 금액
     */
    private int minimumBet = 100;
    
    /**
     * 블라인드 금액
     */
    private int blindAmount = 50;
    
    /**
     * 최대 플레이어 수
     */
    private int maxPlayers = 10;
    
    /**
     * 최소 플레이어 수 (게임 시작 조건)
     */
    private int minPlayers = 2;
    
    /**
     * 라운드 타임아웃 (초)
     */
    private int roundTimeoutSeconds = 60;
}
