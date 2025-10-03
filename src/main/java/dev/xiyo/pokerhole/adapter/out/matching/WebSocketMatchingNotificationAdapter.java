package dev.xiyo.pokerhole.adapter.out.matching;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.xiyo.pokerhole.adapter.in.websocket.message.MessageCodec;
import dev.xiyo.pokerhole.adapter.in.websocket.message.ServerMessage;
import dev.xiyo.pokerhole.adapter.in.websocket.message.ServerMessageType;
import dev.xiyo.pokerhole.adapter.in.websocket.session.PlayerSession;
import dev.xiyo.pokerhole.adapter.in.websocket.session.WebSocketSessionRegistry;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingNotificationPort;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebSocket 기반 매칭 알림 어댑터
 * MatchingNotificationPort 구현
 */
@Slf4j
@Component
@org.springframework.context.annotation.Primary
@RequiredArgsConstructor
public class WebSocketMatchingNotificationAdapter implements MatchingNotificationPort {

    private final WebSocketSessionRegistry sessionRegistry;
    private final MessageCodec messageCodec;

    @Override
    public void notifyMatchingCompleted(List<MatchingRequest> matchedPlayers) {
        log.info("✅ 매칭 완료 알림: {} 명", matchedPlayers.size());

        List<Map<String, Object>> playerList = matchedPlayers.stream()
                .map(req -> Map.<String, Object>of(
                        "sessionId", req.getSessionId(),
                        "nickname", req.getNickname()
                ))
                .collect(Collectors.toList());

        matchedPlayers.forEach(request -> {
            sessionRegistry.findBySessionId(request.getSessionId()).ifPresent(playerSession -> {
                log.info("  → 플레이어: {} (sessionId: {})",
                        request.getNickname(), request.getSessionId());

                // 매칭 완료 메시지 전송
                sendMessage(playerSession.getWebSocketSession(),
                        ServerMessage.of(ServerMessageType.MATCHING_COMPLETED,
                                Map.of(
                                        "message", "매칭이 완료되었습니다!",
                                        "players", playerList
                                )));
            });
        });
    }

    @Override
    public void notifyMatchingProgress(String sessionId, int currentCount, int requiredCount) {
        sessionRegistry.findBySessionId(sessionId).ifPresent(playerSession -> {
            log.info("매칭 진행 상황 알림: sessionId={}, {}/{}", sessionId, currentCount, requiredCount);

            sendMessage(playerSession.getWebSocketSession(),
                    ServerMessage.of(ServerMessageType.MATCHING_PROGRESS,
                            Map.of(
                                    "currentCount", currentCount,
                                    "requiredCount", requiredCount,
                                    "message", String.format("대기 중... (%d/%d)", currentCount, requiredCount)
                            )));
        });
    }

    @Override
    public void notifyMatchingCancelled(String sessionId) {
        sessionRegistry.findBySessionId(sessionId).ifPresent(playerSession -> {
            log.info("매칭 취소 알림: sessionId={}", sessionId);

            sendMessage(playerSession.getWebSocketSession(),
                    ServerMessage.of(ServerMessageType.MATCHING_CANCELLED,
                            Map.of("message", "매칭이 취소되었습니다.")));

            // 세션 상태 업데이트
            playerSession.leaveMatching();
        });
    }

    /**
     * WebSocket 메시지 전송 헬퍼
     */
    private void sendMessage(WebSocketSession session, ServerMessage message) {
        if (!session.isOpen()) {
            log.warn("세션이 닫혀있어 메시지를 보낼 수 없습니다: sessionId={}", session.getId());
            return;
        }

        try {
            String json = messageCodec.encode(message);
            session.sendMessage(new TextMessage(json));
        } catch (JsonProcessingException e) {
            log.error("메시지 직렬화 실패: sessionId={}", session.getId(), e);
        } catch (IOException e) {
            log.error("메시지 전송 실패: sessionId={}", session.getId(), e);
        }
    }
}
