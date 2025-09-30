package dev.xiyo.pokerhole.core.domain.player.vo;

import java.util.UUID;

/**
 * Player의 고유 식별자 (Value Object)
 * 불변 객체로 플레이어의 식별자를 표현합니다.
 */
public record PlayerId(UUID value) {
    
    public PlayerId {
        if (value == null) {
            throw new IllegalArgumentException("PlayerId value cannot be null");
        }
    }
    
    /**
     * 새로운 PlayerId 생성
     */
    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID());
    }
    
    /**
     * 문자열로부터 PlayerId 생성
     */
    public static PlayerId of(String value) {
        return new PlayerId(UUID.fromString(value));
    }
    
    /**
     * UUID로부터 PlayerId 생성
     */
    public static PlayerId of(UUID value) {
        return new PlayerId(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}
