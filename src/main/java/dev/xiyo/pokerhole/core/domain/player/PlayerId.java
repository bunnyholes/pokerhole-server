package dev.xiyo.pokerhole.core.domain.player;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

/**
 * 플레이어 식별자
 */
@Value
@EqualsAndHashCode
public class PlayerId {
    String value;

    private PlayerId(String value) {
        this.value = value;
    }

    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID().toString());
    }

    public static PlayerId of(String value) {
        return new PlayerId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
