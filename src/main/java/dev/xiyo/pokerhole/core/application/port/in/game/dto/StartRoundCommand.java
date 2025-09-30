package dev.xiyo.pokerhole.core.application.port.in.game.dto;

import dev.xiyo.pokerhole.core.domain.game.GameId;

/**
 * 라운드 시작 커맨드
 */
public record StartRoundCommand(
    GameId gameId,
    String requesterId
) {
    public StartRoundCommand {
        if (gameId == null) {
            throw new IllegalArgumentException("GameId cannot be null");
        }
        if (requesterId == null || requesterId.isBlank()) {
            throw new IllegalArgumentException("RequesterId cannot be null or blank");
        }
    }
}
