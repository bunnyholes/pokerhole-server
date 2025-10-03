package dev.xiyo.pokerhole.adapter.in.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.xiyo.pokerhole.adapter.in.websocket.message.ServerMessage;
import dev.xiyo.pokerhole.adapter.in.websocket.message.ServerMessageType;
import dev.xiyo.pokerhole.adapter.in.websocket.metrics.GameMetrics;
import dev.xiyo.pokerhole.adapter.in.websocket.session.PlayerSession;
import dev.xiyo.pokerhole.adapter.in.websocket.session.WebSocketSessionRegistry;
import dev.xiyo.pokerhole.core.domain.game.vo.PlayerAction;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import dev.xiyo.pokerhole.server.room.GameRoom;
import dev.xiyo.pokerhole.server.room.RoomRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ObjectMapper objectMapper;
    private final TurnTimeoutService turnTimeoutService;
    private final GameMetrics gameMetrics;

    @Value("${pokerhole.game.turn-timeout-seconds:30}")
    private int turnTimeoutSeconds;

    /**
     * 게임 액션 실행
     *
     * @param sessionId 플레이어 세션 ID
     * @param action 액션 문자열 (CALL, RAISE, FOLD, CHECK, ALL_IN, BET)
     * @param amount 금액 (RAISE/BET의 경우)
     * @throws IllegalStateException 방에 참가하지 않았거나 잘못된 턴인 경우
     */
    public void executeAction(String sessionId, String action, Integer amount) {
        // 1. PlayerSession 조회
        PlayerSession playerSession = sessionRegistry.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        String roomId = playerSession.getCurrentRoomId();
        if (roomId == null) {
            throw new IllegalStateException("방에 참가하지 않았습니다.");
        }

        // 2. GameRoom 조회
        GameRoom room = roomRegistry.findRoom(roomId)
                .orElseThrow(() -> new IllegalStateException("방을 찾을 수 없습니다: " + roomId));

        // 3. PlayerAction 변환
        PlayerAction playerAction;
        try {
            playerAction = PlayerAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("알 수 없는 액션입니다: " + action);
        }

        // 4. 금액 검증 (BET/RAISE의 경우)
        int actionAmount = (amount != null) ? amount : 0;
        if ((playerAction == PlayerAction.BET || playerAction == PlayerAction.RAISE) && actionAmount <= 0) {
            throw new IllegalArgumentException(playerAction + " 액션에는 유효한 금액이 필요합니다.");
        }

        // 5. GameRoom에 액션 처리 위임
        try {
            // 현재 플레이어의 타임아웃 취소
            turnTimeoutService.cancelTimeout(playerSession.getNickname());

            room.processPlayerActionByNickname(playerSession.getNickname(), playerAction, actionAmount);

            // 메트릭 기록: 플레이어 액션
            gameMetrics.incrementPlayerAction(playerAction.name());

            log.info("게임 액션 처리 성공: sessionId={}, nickname={}, action={}, amount={}",
                    sessionId, playerSession.getNickname(), action, actionAmount);

            // 6. 액션 결과 브로드캐스트
            broadcastPlayerAction(room, playerSession, playerAction, actionAmount);

            // 7. 게임 상태 업데이트 브로드캐스트
            broadcastGameState(room);

            // 8. 다음 플레이어의 타임아웃 시작
            startNextPlayerTimeout(room);

        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("게임 액션 실패: sessionId={}, action={}, error={}",
                    sessionId, action, e.getMessage());
            throw e; // 재발생시켜서 GameWebSocketHandler에서 에러 응답 전송
        }
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
        // GameRoom에서 현재 게임 상태를 가져옴
        Map<String, Object> gameState = room.getGameStateMap();

        ServerMessage stateMessage = ServerMessage.of(
                ServerMessageType.GAME_STATE_UPDATE,
                gameState
        );

        broadcastServerMessage(room, stateMessage);

        log.info("게임 상태 브로드캐스트: roomId={}, round={}, pot={}, currentTurnPlayer={}",
                room.id(), gameState.get("round"), gameState.get("pot"), gameState.get("currentTurnPlayer"));
    }

    /**
     * 플레이어 액션 결과 브로드캐스트
     */
    private void broadcastPlayerAction(GameRoom room, PlayerSession playerSession, PlayerAction action, int amount) {
        Map<String, Object> payload = Map.of(
                "playerId", playerSession.getUuid(),
                "nickname", playerSession.getNickname(),
                "action", action.name(),
                "amount", amount
        );

        ServerMessage message = ServerMessage.of(ServerMessageType.PLAYER_ACTION, payload);
        broadcastServerMessage(room, message);
    }

    /**
     * 방 전체에 ServerMessage 브로드캐스트 (내부 헬퍼)
     */
    private void broadcastServerMessage(GameRoom room, ServerMessage serverMessage) {
        try {
            String json = objectMapper.writeValueAsString(serverMessage);

            // PlayerSession을 통해 WebSocket으로 브로드캐스트
            List<PlayerSession> roomPlayers = sessionRegistry.findAllByRoomId(room.id());

            if (roomPlayers.isEmpty()) {
                log.warn("방에 WebSocket 세션이 없습니다: roomId={}", room.id());
                return;
            }

            roomPlayers.forEach(playerSession -> {
                sendWebSocketMessage(playerSession.getWebSocketSession(), serverMessage);
            });

            log.debug("ServerMessage 브로드캐스트 성공: roomId={}, type={}, recipients={}",
                    room.id(), serverMessage.getType(), roomPlayers.size());
        } catch (JsonProcessingException e) {
            log.error("ServerMessage JSON 직렬화 실패: roomId={}", room.id(), e);
        }
    }

    /**
     * 방 전체에 메시지 브로드캐스트 (내부 헬퍼, 레거시)
     */
    private void broadcastToRoom(GameRoom room, Map<String, Object> messageData) {
        // ServerMessage로 래핑하여 전송
        ServerMessage message = ServerMessage.of(ServerMessageType.GAME_STATE_UPDATE, messageData);
        broadcastServerMessage(room, message);
    }

    /**
     * 다음 플레이어의 턴 타임아웃 시작
     *
     * @param room 게임 방
     */
    private void startNextPlayerTimeout(GameRoom room) {
        room.getCurrentTurnPlayer().ifPresent(currentPlayer -> {
            String playerNickname = currentPlayer.getNickName();

            log.info("다음 플레이어 타임아웃 시작: roomId={}, player={}, timeoutSeconds={}",
                    room.id(), playerNickname, turnTimeoutSeconds);

            // 타임아웃 시작 메시지 브로드캐스트
            broadcastTimeoutStarted(room, playerNickname, turnTimeoutSeconds);

            // 타임아웃 스케줄링
            turnTimeoutService.startTimeout(playerNickname, room, turnTimeoutSeconds, () -> {
                handlePlayerTimeout(room, playerNickname);
            });
        });
    }

    /**
     * 플레이어 타임아웃 처리 (자동 FOLD)
     *
     * @param room 게임 방
     * @param playerNickname 타임아웃된 플레이어 닉네임
     */
    private void handlePlayerTimeout(GameRoom room, String playerNickname) {
        try {
            log.warn("플레이어 타임아웃 발생, 자동 FOLD 처리: roomId={}, player={}",
                    room.id(), playerNickname);

            // 자동 FOLD 액션 처리
            room.processPlayerActionByNickname(playerNickname, PlayerAction.FOLD, 0);

            // 타임아웃 발생 메시지 브로드캐스트
            broadcastPlayerTimedOut(room, playerNickname);

            // 게임 상태 업데이트 브로드캐스트
            broadcastGameState(room);

            // 다음 플레이어 타임아웃 시작
            startNextPlayerTimeout(room);

        } catch (Exception e) {
            log.error("타임아웃 FOLD 처리 실패: roomId={}, player={}",
                    room.id(), playerNickname, e);
        }
    }

    /**
     * 타임아웃 시작 메시지 브로드캐스트
     */
    private void broadcastTimeoutStarted(GameRoom room, String playerNickname, int timeoutSeconds) {
        Map<String, Object> payload = Map.of(
                "playerNickname", playerNickname,
                "remainingSeconds", timeoutSeconds
        );

        ServerMessage message = ServerMessage.of(ServerMessageType.TURN_TIMEOUT_STARTED, payload);
        broadcastServerMessage(room, message);
    }

    /**
     * 플레이어 타임아웃 메시지 브로드캐스트
     */
    private void broadcastPlayerTimedOut(GameRoom room, String playerNickname) {
        Map<String, Object> payload = Map.of(
                "playerNickname", playerNickname,
                "action", "FOLD",
                "reason", "TIMEOUT"
        );

        ServerMessage message = ServerMessage.of(ServerMessageType.PLAYER_TIMED_OUT, payload);
        broadcastServerMessage(room, message);
    }

    /**
     * 매칭된 플레이어들로 게임 생성 및 시작
     *
     * @param gameSessionId 게임 세션 ID (매칭에서 생성된 ID)
     * @param matchedPlayers 매칭된 플레이어 목록
     * @return 생성된 방 ID
     */
    public String createAndStartGame(UUID gameSessionId, List<MatchingRequest> matchedPlayers) {
        log.info("🎮 게임 생성 시작: gameSessionId={}, players={}", gameSessionId, matchedPlayers.size());

        // 1. 방 이름 생성 (게임 세션 ID의 앞 8자리 사용)
        String roomName = "Game-" + gameSessionId.toString().substring(0, 8);

        // 2. GameRoom 생성
        GameRoom room = roomRegistry.createRoom(roomName);
        String roomId = room.id();

        log.info("  → GameRoom 생성 완료: roomId={}, roomName={}", roomId, roomName);

        // 3. 각 플레이어의 세션을 방에 업데이트 (PlayerSession.joinRoom)
        List<PlayerSession> playerSessions = matchedPlayers.stream()
                .map(req -> sessionRegistry.findBySessionId(req.getSessionId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());

        if (playerSessions.size() != matchedPlayers.size()) {
            log.warn("⚠️ 일부 플레이어 세션을 찾을 수 없습니다: expected={}, found={}",
                    matchedPlayers.size(), playerSessions.size());
        }

        // 4. GameRoom에 플레이어 직접 추가 (WebSocket 매칭용)
        for (PlayerSession session : playerSessions) {
            room.addPlayerForWebSocket(session.getNickname());
            log.info("  → 플레이어 Dealer 등록: nickname={}", session.getNickname());
        }

        // 5. PlayerSession 상태 업데이트 (matching → room)
        playerSessions.forEach(session -> {
            session.leaveMatching(); // 매칭 상태 해제
            session.joinRoom(roomId); // 방 참가 상태로 변경
            log.info("  → 플레이어 방 참가: nickname={}, roomId={}", session.getNickname(), roomId);
        });

        // 6. GAME_STARTED 메시지 브로드캐스트
        broadcastGameStarted(room, gameSessionId, matchedPlayers);

        // 7. Texas Hold'em 게임 자동 시작
        try {
            room.startTexasHoldemAuto();
            log.info("  → Texas Hold'em 게임 자동 시작 완료: roomId={}", roomId);
        } catch (Exception e) {
            log.error("⚠️ Texas Hold'em 게임 자동 시작 실패: roomId={}", roomId, e);
            // 게임 시작 실패해도 방은 생성되어 있으므로 계속 진행
        }

        // 8. 게임 상태 업데이트 브로드캐스트 (클라이언트 화면에 게임 정보 표시)
        broadcastGameState(room);

        // 9. 첫 번째 플레이어부터 타임아웃 시작
        startNextPlayerTimeout(room);

        log.info("✅ 게임 시작 완료: roomId={}, gameSessionId={}", roomId, gameSessionId);

        return roomId;
    }

    /**
     * GAME_STARTED 메시지 브로드캐스트
     */
    private void broadcastGameStarted(GameRoom room, UUID gameSessionId, List<MatchingRequest> players) {
        // 게임 상태 맵 생성 (초기 상태)
        Map<String, Object> gameState = Map.of(
                "gameId", room.id(),
                "gameSessionId", gameSessionId.toString(),
                "roomName", room.name(),
                "playerCount", players.size(),
                "players", players.stream()
                        .map(req -> Map.of(
                                "sessionId", req.getSessionId(),
                                "nickname", req.getNickname()
                        ))
                        .collect(Collectors.toList())
        );

        ServerMessage message = ServerMessage.of(ServerMessageType.GAME_STARTED, gameState);

        // 각 플레이어에게 개별 전송
        players.forEach(req -> {
            sessionRegistry.findBySessionId(req.getSessionId()).ifPresent(playerSession -> {
                sendWebSocketMessage(playerSession.getWebSocketSession(), message);
                log.info("  → GAME_STARTED 전송: nickname={}, sessionId={}",
                        req.getNickname(), req.getSessionId());
            });
        });

        log.info("📢 GAME_STARTED 브로드캐스트 완료: roomId={}, players={}", room.id(), players.size());
    }

    /**
     * WebSocket 메시지 전송 헬퍼
     * WebSocket 세션은 동시에 여러 메시지를 전송할 수 없으므로 세션별로 동기화합니다.
     * 전송 실패 시 최대 3회까지 재시도합니다.
     */
    private void sendWebSocketMessage(WebSocketSession session, ServerMessage message) {
        sendWebSocketMessageWithRetry(session, message, 3);
    }

    /**
     * 재시도 로직을 포함한 WebSocket 메시지 전송
     *
     * @param session WebSocket 세션
     * @param message 전송할 메시지
     * @param retriesLeft 남은 재시도 횟수
     */
    private void sendWebSocketMessageWithRetry(WebSocketSession session, ServerMessage message, int retriesLeft) {
        if (session == null) {
            log.warn("세션이 null이어서 메시지를 보낼 수 없습니다");
            return;
        }

        if (!session.isOpen()) {
            log.warn("세션이 닫혀있어 메시지를 보낼 수 없습니다: sessionId={}", session.getId());
            return;
        }

        // 세션별 동기화: WebSocket은 동시 전송을 지원하지 않음
        synchronized (session) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("메시지 전송 성공: sessionId={}, type={}", session.getId(), message.getType());
            } catch (JsonProcessingException e) {
                log.error("메시지 직렬화 실패: sessionId={}, type={}", session.getId(), message.getType(), e);
                // JSON 직렬화 실패는 재시도해도 소용없으므로 바로 리턴
            } catch (IOException e) {
                if (retriesLeft > 0) {
                    log.warn("메시지 전송 실패, 재시도 중... sessionId={}, type={}, retriesLeft={}",
                            session.getId(), message.getType(), retriesLeft);

                    try {
                        Thread.sleep(100); // 100ms 대기 후 재시도
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    sendWebSocketMessageWithRetry(session, message, retriesLeft - 1);
                } else {
                    log.error("메시지 전송 최종 실패: sessionId={}, type={}", session.getId(), message.getType(), e);
                    // 세션이 실제로 닫혔을 수 있으므로 레지스트리에서 제거
                    try {
                        sessionRegistry.unregisterBySessionId(session.getId());
                    } catch (Exception ex) {
                        log.error("세션 정리 실패: sessionId={}", session.getId(), ex);
                    }
                }
            }
        }
    }
}
