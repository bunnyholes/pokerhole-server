package dev.xiyo.pokerhole.core.domain.match;

import lombok.Value;

import java.util.UUID;

/**
 * 매치 세션 식별자
 */
@Value(staticConstructor = "of")
public class MatchSessionId {
    String value;
    
    public static MatchSessionId generate() {
        return MatchSessionId.of(UUID.randomUUID().toString());
    }
}
