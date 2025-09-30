package dev.xiyo.pokerhole.core.domain.game;

import java.util.UUID;
import java.util.Objects;

/**
 * Game의 고유 식별자 (Value Object)
 */
public final class GameId {
    private final String value;

    private GameId(String value) {
        this.value = Objects.requireNonNull(value, "GameId value cannot be null");
    }

    public static GameId generate() {
        return new GameId(UUID.randomUUID().toString());
    }

    public static GameId of(String value) {
        return new GameId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameId gameId = (GameId) o;
        return value.equals(gameId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
