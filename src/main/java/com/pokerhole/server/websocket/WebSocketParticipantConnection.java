package com.pokerhole.server.websocket;

import com.pokerhole.server.session.ParticipantConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class WebSocketParticipantConnection implements ParticipantConnection {

    private final WebSocketSession session;

    @Override
    public void send(String message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException ex) {
                log.warn("웹소켓 전송 실패: {}", ex.getMessage());
            }
        }
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}
