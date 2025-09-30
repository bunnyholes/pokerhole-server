package dev.xiyo.pokerhole.core.domain.player;

import lombok.Value;

import java.util.UUID;

/**
 * 플레이어 식별자
 */
@Value(staticConstructor = "of")
public class PlayerId {
    String value;
    
    public static PlayerId generate() {
        return PlayerId.of(UUID.randomUUID().toString());
    }
}
