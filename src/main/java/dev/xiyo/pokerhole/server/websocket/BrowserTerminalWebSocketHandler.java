package dev.xiyo.pokerhole.server.websocket;

import dev.xiyo.pokerhole.server.session.SessionState;
import dev.xiyo.pokerhole.server.terminal.TerminalCommandProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserTerminalWebSocketHandler extends TextWebSocketHandler {
    private static final String STATE_ATTRIBUTE = "pokerholeSession";

    private final TerminalCommandProcessor commandProcessor;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SessionState state = new SessionState(new WebSocketParticipantConnection(session));
        session.getAttributes().put(STATE_ATTRIBUTE, state);
        commandProcessor.onConnect(state);
        log.info("WebSocket connection {} opened", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionState state = getState(session);
        if (state == null) {
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }
        boolean keepAlive = commandProcessor.handle(state, message.getPayload());
        if (!keepAlive) {
            session.close(CloseStatus.NORMAL);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SessionState state = getState(session);
        if (state != null) {
            commandProcessor.handleDisconnect(state);
        }
        log.info("WebSocket connection {} closed: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error on {}: {}", session.getId(), exception.getMessage());
        super.handleTransportError(session, exception);
    }

    private SessionState getState(WebSocketSession session) {
        Object attribute = session.getAttributes().get(STATE_ATTRIBUTE);
        if (attribute instanceof SessionState state) {
            return state;
        }
        return null;
    }
}
