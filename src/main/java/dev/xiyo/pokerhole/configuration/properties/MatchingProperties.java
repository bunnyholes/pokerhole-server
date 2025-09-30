package dev.xiyo.pokerhole.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 매칭 관련 설정 속성
 */
@Data
@ConfigurationProperties(prefix = "pokerhole.matching")
public class MatchingProperties {
    
    /**
     * 자동 매칭 활성화 여부
     */
    private boolean autoMatchingEnabled = true;
    
    /**
     * 매칭 대기 시간 (초) - 이 시간 초과 시 AI 플레이어 투입
     */
    private int waitTimeoutSeconds = 30;
    
    /**
     * 매칭 큐 처리 주기 (밀리초)
     */
    private long queueProcessIntervalMs = 5000;
    
    /**
     * 게임당 필요한 플레이어 수
     */
    private int playersPerGame = 4;
    
    /**
     * AI 플레이어 투입 활성화 여부
     */
    private boolean aiPlayerEnabled = true;
    
    /**
     * 코드 매칭 활성화 여부
     */
    private boolean codeMatchingEnabled = true;
    
    /**
     * 매칭 코드 유효 시간 (분)
     */
    private int codeExpirationMinutes = 30;
}
