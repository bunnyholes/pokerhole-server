package dev.xiyo.pokerhole.adapter.in.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.xiyo.pokerhole.adapter.in.websocket.message.*;
import dev.xiyo.pokerhole.adapter.in.websocket.session.PlayerSession;
import dev.xiyo.pokerhole.adapter.in.websocket.session.WebSocketSessionRegistry;
import dev.xiyo.pokerhole.adapter.out.persistence.jpa.adapter.GuestVisitService;
import dev.xiyo.pokerhole.core.application.port.in.matching.*;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

/**
 * 게임용 WebSocket 핸들러
 * 새로운 프로토콜 기반 메시지 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final MessageCodec messageCodec;
    private final WebSocketSessionRegistry sessionRegistry;
    private final GuestVisitService guestVisitService;

    // Matching Use Cases
    private final JoinRandomMatchingUseCase joinRandomMatchingUseCase;
    private final JoinCodeMatchingUseCase joinCodeMatchingUseCase;
    private final CancelMatchingUseCase cancelMatchingUseCase;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        guestVisitService.recordVisit(session.getId());
        log.info("WebSocket 연결 수립: sessionId={}", session.getId());
        
        // 클라이언트에게 연결 성공 알림 (REGISTER 대기 중)
        sendMessage(session, ServerMessage.of(ServerMessageType.REGISTER_SUCCESS,
                Map.of("message", "연결되었습니다. REGISTER 메시지를 보내주세요.")));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        
        try {
            ClientMessage clientMessage = messageCodec.decodeClientMessage(payload);
            handleClientMessage(session, clientMessage);
        } catch (JsonProcessingException e) {
            log.warn("JSON 파싱 실패: sessionId={}, payload={}", session.getId(), payload);
            sendError(session, "잘못된 메시지 형식입니다.");
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: sessionId={}", session.getId(), e);
            sendError(session, "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessionRegistry.unregisterBySessionId(sessionId);
        log.info("WebSocket 연결 종료: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 전송 오류: sessionId={}", session.getId(), exception);
    }

    /**
     * 클라이언트 메시지 처리
     */
    private void handleClientMessage(WebSocketSession session, ClientMessage message) {
        ClientMessageType type = message.getType();
        Map<String, Object> payload = message.getPayload();

        log.debug("클라이언트 메시지 수신: sessionId={}, type={}", session.getId(), type);

        switch (type) {
            case REGISTER -> handleRegister(session, payload);
            case HEARTBEAT -> handleHeartbeat(session);
            case JOIN_RANDOM_MATCH -> handleJoinRandomMatch(session);
            case JOIN_CODE_MATCH -> handleJoinCodeMatch(session, payload);
            case CANCEL_MATCHING -> handleCancelMatching(session);
            case CALL -> handleGameAction(session, "CALL");
            case RAISE -> handleGameAction(session, "RAISE", payload);
            case FOLD -> handleGameAction(session, "FOLD");
            case CHECK -> handleGameAction(session, "CHECK");
            case ALL_IN -> handleGameAction(session, "ALL_IN");
            case LEAVE_GAME -> handleLeaveGame(session);
            case CHAT_MESSAGE -> handleChatMessage(session, payload);
            default -> {
                log.warn("알 수 없는 메시지 타입: {}", type);
                sendError(session, "지원하지 않는 메시지 타입입니다: " + type);
            }
        }
    }

    /**
     * 플레이어 등록 처리
     */
    private void handleRegister(WebSocketSession session, Map<String, Object> payload) {
        if (payload == null) {
            sendError(session, "REGISTER 메시지에 페이로드가 필요합니다.");
            return;
        }

        String uuid = (String) payload.get("uuid");
        String nickname = (String) payload.get("nickname");

        if (uuid == null || uuid.isBlank() || nickname == null || nickname.isBlank()) {
            sendError(session, "UUID와 닉네임이 필요합니다.");
            return;
        }

        // 중복 UUID 체크
        if (sessionRegistry.containsUuid(uuid)) {
            sendMessage(session, ServerMessage.of(ServerMessageType.REGISTER_FAILURE,
                    Map.of("reason", "이미 등록된 UUID입니다.")));
            return;
        }

        // 세션 등록
        PlayerSession playerSession = PlayerSession.of(session, uuid, nickname);
        sessionRegistry.register(playerSession);

        sendMessage(session, ServerMessage.of(ServerMessageType.REGISTER_SUCCESS,
                Map.of(
                        "uuid", uuid,
                        "nickname", nickname,
                        "message", "등록이 완료되었습니다."
                )));
    }

    /**
     * Heartbeat 처리
     */
    private void handleHeartbeat(WebSocketSession session) {
        // 단순 응답 (연결 유지 확인)
        log.trace("Heartbeat 수신: sessionId={}", session.getId());
    }

    /**
     * 랜덤 매칭 참가
     */
    private void handleJoinRandomMatch(WebSocketSession session) {
        PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        log.info("랜덤 매칭 요청: sessionId={}, nickname={}", session.getId(), playerSession.getNickname());

        try {
            var command = new JoinRandomMatchingUseCase.JoinRandomMatchingCommand(
                    session.getId(),
                    playerSession.getNickname()
            );
            MatchingRequest request = joinRandomMatchingUseCase.joinRandomMatching(command);

            // 세션 상태 업데이트
            playerSession.joinMatching(request.getRequestId().toString());

            sendMessage(session, ServerMessage.of(ServerMessageType.MATCHING_STARTED,
                    Map.of(
                            "requestId", request.getRequestId(),
                            "message", "매칭을 시작합니다..."
                    )));
        } catch (Exception e) {
            log.error("랜덤 매칭 실패: sessionId={}", session.getId(), e);
            sendError(session, "매칭 시작 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 코드 매칭 참가
     */
    private void handleJoinCodeMatch(WebSocketSession session, Map<String, Object> payload) {
        PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        String code = payload != null ? (String) payload.get("code") : null;

        if (code == null || code.isBlank()) {
            sendError(session, "매칭 코드가 필요합니다.");
            return;
        }

        log.info("코드 매칭 요청: sessionId={}, nickname={}, code={}",
                session.getId(), playerSession.getNickname(), code);

        try {
            var command = new JoinCodeMatchingUseCase.JoinCodeMatchingCommand(
                    session.getId(),
                    playerSession.getNickname(),
                    code
            );
            MatchingRequest request = joinCodeMatchingUseCase.joinCodeMatching(command);

            // 세션 상태 업데이트
            playerSession.joinMatching(request.getRequestId().toString());

            sendMessage(session, ServerMessage.of(ServerMessageType.MATCHING_STARTED,
                    Map.of(
                            "requestId", request.getRequestId(),
                            "code", code,
                            "message", "코드 매칭을 시작합니다..."
                    )));
        } catch (Exception e) {
            log.error("코드 매칭 실패: sessionId={}, code={}", session.getId(), code, e);
            sendError(session, "코드 매칭 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 매칭 취소
     */
    private void handleCancelMatching(WebSocketSession session) {
        PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        log.info("매칭 취소 요청: sessionId={}", session.getId());

        try {
            cancelMatchingUseCase.cancelMatching(session.getId());

            // 세션 상태 업데이트
            playerSession.leaveMatching();

            sendMessage(session, ServerMessage.of(ServerMessageType.MATCHING_CANCELLED,
                    Map.of("message", "매칭이 취소되었습니다.")));
        } catch (Exception e) {
            log.warn("매칭 취소 실패 (매칭 중이 아닐 수 있음): sessionId={}", session.getId(), e);
            sendMessage(session, ServerMessage.of(ServerMessageType.MATCHING_CANCELLED,
                    Map.of("message", "매칭이 취소되었습니다.")));
        }
    }

    /**
     * 게임 액션 처리
     */
    private void handleGameAction(WebSocketSession session, String action) {
        handleGameAction(session, action, null);
    }

    private void handleGameAction(WebSocketSession session, String action, Map<String, Object> payload) {
        // TODO: 게임 액션 처리 로직
        log.info("게임 액션: sessionId={}, action={}, payload={}", session.getId(), action, payload);
    }

    /**
     * 게임 나가기
     */
    private void handleLeaveGame(WebSocketSession session) {
        PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        log.info("게임 나가기 요청: sessionId={}, roomId={}", session.getId(), playerSession.getCurrentRoomId());

        // 현재 방에 있는지 확인
        if (!playerSession.isInRoom()) {
            sendError(session, "현재 방에 참가하고 있지 않습니다.");
            return;
        }

        // TODO: GameRoom에서 플레이어 제거 및 다른 참가자들에게 알림
        // 현재는 세션 상태만 업데이트
        playerSession.leaveRoom();

        sendMessage(session, ServerMessage.of(ServerMessageType.GAME_ENDED,
                Map.of("message", "게임에서 나갔습니다.")));
    }

    /**
     * 채팅 메시지
     */
    private void handleChatMessage(WebSocketSession session, Map<String, Object> payload) {
        PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
                .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

        String message = payload != null ? (String) payload.get("message") : null;

        if (message == null || message.isBlank()) {
            sendError(session, "채팅 메시지가 필요합니다.");
            return;
        }

        log.info("채팅 메시지: sessionId={}, nickname={}, message={}",
                session.getId(), playerSession.getNickname(), message);

        // TODO: 같은 방에 있는 플레이어들에게 브로드캐스트
        // 현재는 자신에게만 에코
        sendMessage(session, ServerMessage.of(ServerMessageType.CHAT_MESSAGE,
                Map.of(
                        "nickname", playerSession.getNickname(),
                        "message", message,
                        "timestamp", System.currentTimeMillis()
                )));
    }

    /**
     * 에러 메시지 전송
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        sendMessage(session, ServerMessage.of(ServerMessageType.ERROR,
                Map.of("message", errorMessage)));
    }

    /**
     * 메시지 전송
     */
    private void sendMessage(WebSocketSession session, ServerMessage message) {
        if (!session.isOpen()) {
            log.warn("세션이 닫혀있어 메시지를 전송할 수 없습니다: sessionId={}", session.getId());
            return;
        }

        try {
            String json = messageCodec.encode(message);
            session.sendMessage(new TextMessage(json));
        } catch (JsonProcessingException e) {
            log.error("메시지 인코딩 실패: sessionId={}", session.getId(), e);
        } catch (IOException e) {
            log.error("메시지 전송 실패: sessionId={}", session.getId(), e);
        }
    }
}
