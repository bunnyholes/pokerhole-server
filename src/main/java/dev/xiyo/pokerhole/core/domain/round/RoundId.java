package dev.xiyo.pokerhole.core.domain.round;

import lombok.Value;

import java.util.UUID;

/**
 * 라운드 식별자
 */
@Value(staticConstructor = "of")
public class RoundId {
    String value;
    
    public static RoundId generate() {
        return RoundId.of(UUID.randomUUID().toString());
    }
}
