package dev.xiyo.pokerhole.adapter.in.websocket.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

/**
 * WebSocket 세션과 플레이어 정보를 담는 컨테이너
 */
@Getter
@RequiredArgsConstructor
public class PlayerSession {
    private final WebSocketSession webSocketSession;
    private final String uuid;
    private final String nickname;
    private final Instant connectedAt;

    /**
     * 세션이 열려있는지 확인
     */
    public boolean isOpen() {
        return webSocketSession.isOpen();
    }

    /**
     * 세션 ID 반환
     */
    public String getSessionId() {
        return webSocketSession.getId();
    }

    /**
     * PlayerSession 생성 (현재 시간 기준)
     */
    public static PlayerSession of(WebSocketSession session, String uuid, String nickname) {
        return new PlayerSession(session, uuid, nickname, Instant.now());
    }
}
