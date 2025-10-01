package dev.xiyo.pokerhole.configuration;

import dev.xiyo.pokerhole.adapter.in.web.handler.BrowserTerminalWebSocketHandler;
import dev.xiyo.pokerhole.adapter.in.websocket.GameWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final BrowserTerminalWebSocketHandler terminalHandler;
    private final GameWebSocketHandler gameHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 레거시 터미널 핸들러 (하위 호환성)
        registry.addHandler(terminalHandler, "/ws/terminal").setAllowedOriginPatterns("*");
        
        // 새로운 게임 프로토콜 핸들러
        registry.addHandler(gameHandler, "/ws/game").setAllowedOriginPatterns("*");
    }
}
