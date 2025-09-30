package dev.xiyo.pokerhole.core.domain.player.vo;

/**
 * Player의 닉네임 (Value Object)
 * 불변 객체로 플레이어의 닉네임을 표현하며, 유효성 검증을 포함합니다.
 */
public record Nickname(String value) {
    
    private static final int MAX_LENGTH = 20;
    private static final int MIN_LENGTH = 1;
    
    public Nickname {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Nickname cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Nickname must be " + MAX_LENGTH + " characters or less");
        }
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Nickname must be at least " + MIN_LENGTH + " character");
        }
    }
    
    /**
     * 닉네임 생성
     */
    public static Nickname of(String value) {
        return new Nickname(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
