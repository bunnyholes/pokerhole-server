package dev.xiyo.pokerhole.core.domain.matching;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * 매칭 코드 Value Object
 * 친구와 함께 매칭하기 위한 코드
 */
@Value
public class MatchingCode {

    /**
     * 6자리 코드 (예: A1B2C3)
     */
    String code;

    /**
     * 코드 생성 시각
     */
    Instant createdAt;

    /**
     * 새로운 매칭 코드 생성
     */
    public static MatchingCode generate() {
        String code = UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
        return new MatchingCode(code, Instant.now());
    }

    /**
     * 문자열로부터 매칭 코드 생성
     */
    public static MatchingCode from(String code) {
        if (code == null || code.length() != 6) {
            throw new IllegalArgumentException("매칭 코드는 6자리여야 합니다.");
        }
        return new MatchingCode(code.toUpperCase(), Instant.now());
    }

    /**
     * 코드 만료 여부 확인 (30분 기준)
     */
    public boolean isExpired(int expirationMinutes) {
        return Instant.now().isAfter(createdAt.plusSeconds(expirationMinutes * 60L));
    }
}
