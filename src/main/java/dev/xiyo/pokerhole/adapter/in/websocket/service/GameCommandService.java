package dev.xiyo.pokerhole.adapter.in.websocket.service;

import dev.xiyo.pokerhole.adapter.in.websocket.message.ServerMessage;
import dev.xiyo.pokerhole.adapter.in.websocket.message.ServerMessageType;
import dev.xiyo.pokerhole.adapter.in.websocket.session.PlayerSession;
import dev.xiyo.pokerhole.adapter.in.websocket.session.WebSocketSessionRegistry;
import dev.xiyo.pokerhole.server.room.GameRoom;
import dev.xiyo.pokerhole.server.room.RoomRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * 게임 명령 처리 서비스
 * 게임 액션 및 채팅 메시지를 처리하고 방 참가자들에게 브로드캐스트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameCommandService {

    private final RoomRegistry roomRegistry;
    private final WebSocketSessionRegistry sessionRegistry;

    /**
     * 게임 액션 실행
     *
     * @param sessionId 플레이어 세션 ID
     * @param action 액션 문자열 (CALL, RAISE, FOLD, CHECK, ALL_IN)
     * @param amount 금액 (RAISE의 경우)
     * @throws IllegalStateException 방에 참가하지 않았거나 잘못된 턴인 경우
     */
    public void executeAction(String sessionId, String action, Integer amount) {
        PlayerSession playerSession = sessionRegistry.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        String roomId = playerSession.getCurrentRoomId();
        if (roomId == null) {
            throw new IllegalStateException("방에 참가하지 않았습니다.");
        }

        GameRoom room = roomRegistry.findRoom(roomId)
                .orElseThrow(() -> new IllegalStateException("방을 찾을 수 없습니다: " + roomId));

        // TODO: 실제 게임 액션 처리 로직 구현
        // 현재 GameRoom/Dealer 구조는 턴제 Texas Hold'em이 아니므로
        // 향후 게임 로직이 추가되면 여기서 처리
        log.info("게임 액션 처리 (미구현): sessionId={}, action={}, amount={}",
                sessionId, action, amount);

        // 일단 기본 응답만 전송
        broadcastToRoom(room, Map.of(
                "type", "PLAYER_ACTION",
                "playerId", playerSession.getUuid(),
                "nickname", playerSession.getNickname(),
                "action", action,
                "amount", amount != null ? amount : 0
        ));
    }

    /**
     * 채팅 메시지 브로드캐스트
     *
     * @param roomId 방 ID
     * @param senderNickname 발신자 닉네임
     * @param message 메시지 내용
     */
    public void broadcastChatMessage(String roomId, String senderNickname, String message) {
        GameRoom room = roomRegistry.findRoom(roomId)
                .orElseThrow(() -> new IllegalStateException("방을 찾을 수 없습니다: " + roomId));

        Map<String, Object> chatPayload = Map.of(
                "nickname", senderNickname,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );

        ServerMessage chatMessage = ServerMessage.of(ServerMessageType.CHAT_MESSAGE, chatPayload);

        // 방에 있는 모든 WebSocket 세션에 전송
        broadcastToRoom(room, chatPayload);

        log.info("채팅 메시지 브로드캐스트: roomId={}, sender={}, message={}",
                roomId, senderNickname, message);
    }

    /**
     * 게임 상태 브로드캐스트
     *
     * @param room 게임 방
     */
    public void broadcastGameState(GameRoom room) {
        // TODO: 실제 게임 상태를 수집하여 브로드캐스트
        // 현재는 기본 정보만 전송
        Map<String, Object> gameState = Map.of(
                "roomId", room.id(),
                "roomName", room.name(),
                "playerCount", room.summary().participants(),
                "maxPlayers", room.summary().capacity()
        );

        ServerMessage stateMessage = ServerMessage.of(
                ServerMessageType.GAME_STATE_UPDATE,
                gameState
        );

        broadcastToRoom(room, gameState);

        log.info("게임 상태 브로드캐스트: roomId={}", room.id());
    }

    /**
     * 방 전체에 메시지 브로드캐스트 (내부 헬퍼)
     */
    private void broadcastToRoom(GameRoom room, Map<String, Object> messageData) {
        // GameRoom은 자체 브로드캐스트 메서드가 있지만 String 기반이므로
        // WebSocket JSON 메시지를 위해 여기서 직접 처리

        // TODO: GameRoom의 participants를 순회하며 WebSocket 세션에 전송
        // 현재는 GameRoom의 private participants에 접근할 수 없으므로
        // 로그만 남김
        log.warn("브로드캐스트 미구현: GameRoom.participants가 private이므로 접근 불가");
        log.debug("브로드캐스트할 메시지: {}", messageData);
    }

    /**
     * WebSocket 세션으로 메시지 전송 (헬퍼)
     * TODO: JSON 직렬화 구현 필요
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> messageData) {
        // TODO: Jackson ObjectMapper를 사용하여 JSON 직렬화
        log.debug("메시지 전송 (미구현): sessionId={}, data={}", session.getId(), messageData);
    }
}
