package dev.xiyo.pokerhole.core.domain.round;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

/**
 * 라운드 식별자
 */
@Value
@EqualsAndHashCode
public class RoundId {
    String value;

    private RoundId(String value) {
        this.value = value;
    }

    public static RoundId generate() {
        return new RoundId(UUID.randomUUID().toString());
    }

    public static RoundId of(String value) {
        return new RoundId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
