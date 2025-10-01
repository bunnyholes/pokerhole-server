package dev.xiyo.pokerhole.core.domain.matching;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * 매칭 요청 Value Object
 * 불변 객체로 플레이어의 매칭 요청 정보를 담음
 */
@Value
@Builder
public class MatchingRequest {

    /**
     * 요청 고유 ID
     */
    UUID requestId;

    /**
     * 세션 ID
     */
    String sessionId;

    /**
     * 플레이어 닉네임
     */
    String nickname;

    /**
     * 매칭 타입 (랜덤 또는 코드)
     */
    MatchingType type;

    /**
     * 매칭 코드 (코드 매칭인 경우)
     */
    MatchingCode matchingCode;

    /**
     * 요청 생성 시각
     */
    Instant createdAt;

    /**
     * 랜덤 매칭 요청 생성
     */
    public static MatchingRequest forRandomMatching(String sessionId, String nickname) {
        return MatchingRequest.builder()
                .requestId(UUID.randomUUID())
                .sessionId(sessionId)
                .nickname(nickname)
                .type(MatchingType.RANDOM)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 코드 매칭 요청 생성
     */
    public static MatchingRequest forCodeMatching(String sessionId, String nickname, MatchingCode code) {
        return MatchingRequest.builder()
                .requestId(UUID.randomUUID())
                .sessionId(sessionId)
                .nickname(nickname)
                .type(MatchingType.CODE)
                .matchingCode(code)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 요청 타임아웃 여부 확인 (10초 기준)
     */
    public boolean isTimedOut(int timeoutSeconds) {
        return Instant.now().isAfter(createdAt.plusSeconds(timeoutSeconds));
    }

    /**
     * 랜덤 매칭 요청인지 확인
     */
    public boolean isRandomMatching() {
        return type == MatchingType.RANDOM;
    }

    /**
     * 코드 매칭 요청인지 확인
     */
    public boolean isCodeMatching() {
        return type == MatchingType.CODE;
    }
}
