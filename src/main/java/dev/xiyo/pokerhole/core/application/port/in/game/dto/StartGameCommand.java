package dev.xiyo.pokerhole.core.application.port.in.game.dto;

import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;

import java.util.List;
import java.util.UUID;

/**
 * 게임 시작 커맨드
 * 매칭이 완료된 플레이어 목록으로 새로운 게임을 시작합니다.
 */
public record StartGameCommand(
    UUID gameSessionId,
    List<MatchingRequest> matchedPlayers
) {
    public StartGameCommand {
        if (gameSessionId == null) {
            throw new IllegalArgumentException("GameSessionId cannot be null");
        }
        if (matchedPlayers == null || matchedPlayers.isEmpty()) {
            throw new IllegalArgumentException("MatchedPlayers cannot be null or empty");
        }
    }
}
