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

    // 현재 상태 추적
    private String currentRoomId;
    private String currentMatchingId;

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
     * 방에 참가
     */
    public void joinRoom(String roomId) {
        this.currentRoomId = roomId;
        this.currentMatchingId = null; // 매칭 완료
    }

    /**
     * 매칭 시작
     */
    public void joinMatching(String matchingId) {
        this.currentMatchingId = matchingId;
    }

    /**
     * 방에서 나가기
     */
    public void leaveRoom() {
        this.currentRoomId = null;
    }

    /**
     * 매칭 취소
     */
    public void leaveMatching() {
        this.currentMatchingId = null;
    }

    /**
     * 현재 방에 있는지 확인
     */
    public boolean isInRoom() {
        return currentRoomId != null;
    }

    /**
     * 현재 매칭 중인지 확인
     */
    public boolean isMatching() {
        return currentMatchingId != null;
    }

    /**
     * PlayerSession 생성 (현재 시간 기준)
     */
    public static PlayerSession of(WebSocketSession session, String uuid, String nickname) {
        return new PlayerSession(session, uuid, nickname, Instant.now());
    }
}
