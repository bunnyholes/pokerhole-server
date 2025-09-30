package dev.xiyo.pokerhole.core.domain.ai;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI 페르소나
 * 추후 LLM 통합 및 다양한 전략 추가 가능
 */
@Getter
@RequiredArgsConstructor
public enum AIPersona {
    /**
     * 무작위 플레이어 (현재 기본)
     */
    RANDOM("무작위 플레이어", "완전히 무작위로 행동하는 AI"),
    
    /**
     * 보수적 플레이어 (추후 구현)
     */
    CONSERVATIVE("보수적 플레이어", "안전한 선택을 선호하는 AI"),
    
    /**
     * 공격적 플레이어 (추후 구현)
     */
    AGGRESSIVE("공격적 플레이어", "공격적인 베팅을 선호하는 AI"),
    
    /**
     * LLM 기반 플레이어 (추후 구현)
     */
    LLM_POWERED("AI 기반 플레이어", "대형 언어 모델을 사용하는 전략적 AI");
    
    private final String displayName;
    private final String description;
}
