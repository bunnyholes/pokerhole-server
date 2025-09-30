package dev.xiyo.pokerhole.core.domain.match;

import lombok.Value;

import java.util.UUID;

/**
 * 매치 식별자
 */
@Value(staticConstructor = "of")
public class MatchId {
    String value;
    
    public static MatchId generate() {
        return MatchId.of(UUID.randomUUID().toString());
    }
}
