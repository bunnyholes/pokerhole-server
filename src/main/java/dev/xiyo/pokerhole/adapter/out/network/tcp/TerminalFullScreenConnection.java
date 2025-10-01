package dev.xiyo.pokerhole.adapter.out.network.tcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.xiyo.pokerhole.adapter.in.terminal.TerminalUIController;
import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionContext;
import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionState;
import dev.xiyo.pokerhole.adapter.out.network.session.SessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Full-Screen Terminal 연결
 * JLine Terminal 기반 TUI 구현
 */
@Slf4j
public class TerminalFullScreenConnection implements Runnable {

    /**
     * 클라이언트 식별 정보 DTO
     */
    public record ClientInfo(String uuid, String nickname) {}

    private final Socket socket;
    private final TerminalUIController uiController;
    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean running = true;

    public TerminalFullScreenConnection(
            Socket socket,
            TerminalUIController uiController,
            SessionRegistry sessionRegistry
    ) {
        this.socket = socket;
        this.uiController = uiController;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void run() {
        log.info("Client registration from {}", socket.getRemoteSocketAddress());

        try {
            // 클라이언트 정보 수신 (간단한 등록만)
            ClientInfo clientInfo = receiveClientInfoSimple();

            if (clientInfo != null) {
                log.info("✅ Client registered - UUID: {}, Nickname: {}",
                        clientInfo.uuid(), clientInfo.nickname());

                // TODO: 실제로는 세션 등록, 매칭 큐 등록 등의 로직이 들어가야 함
                // 현재는 단순히 로그만 남김
            } else {
                log.warn("❌ Failed to receive client info");
            }

        } catch (Exception e) {
            log.error("Client registration failed", e);
        } finally {
            closeSocket();
        }
    }

    /**
     * 클라이언트 정보 수신 (간단한 소켓 읽기)
     */
    private ClientInfo receiveClientInfoSimple() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // 첫 번째 라인을 JSON으로 파싱
            String jsonLine = reader.readLine();
            if (jsonLine != null && !jsonLine.isEmpty()) {
                return objectMapper.readValue(jsonLine, ClientInfo.class);
            }
        } catch (Exception e) {
            log.warn("Failed to receive client info: {}", e.getMessage());
        }
        return null;
    }

    /**
     * UI 렌더링 루프
     */
    private void runUILoop(TerminalSessionContext context) throws IOException {
        Terminal terminal = context.getTerminal();
        NonBlockingReader reader = terminal.reader();

        // 배너 표시 (3초 자동 전환)
        uiController.renderBanner(context);
        scheduleTransitionToMenu(context, 3);

        // 메인 이벤트 루프
        while (running) {
            int c = reader.read(100); // 100ms timeout

            if (c == -1) {
                // Timeout or EOF
                if (context.getCurrentState() == TerminalSessionState.MATCHING) {
                    // 매칭 화면 업데이트
                    uiController.renderMatching(context, 1, 4);
                }
                continue;
            }

            handleKeyInput(context, reader, c);
        }
    }

    /**
     * 키 입력 처리
     */
    private void handleKeyInput(TerminalSessionContext context, NonBlockingReader reader, int c) throws IOException {
        log.debug("키 입력: {} (0x{})", (char)c, Integer.toHexString(c));
        TerminalSessionState currentState = context.getCurrentState();

        // ESC sequence 처리 (화살표 키)
        if (c == 27) {
            int next = reader.read(10);
            if (next == '[') {
                int arrow = reader.read(10);
                if (currentState == TerminalSessionState.MAIN_MENU) {
                    handleArrowKey(context, arrow);
                    return;
                }
            } else if (next == -1) {
                // ESC 단독 입력
                handleEscKey(context);
                return;
            }
        }

        // Enter 키
        if (c == '\r' || c == '\n') {
            if (currentState == TerminalSessionState.MAIN_MENU) {
                uiController.handleMenuSelection(context);
            }
            return;
        }

        // q 키 (종료 or 매칭 취소)
        if (c == 'q' || c == 'Q') {
            if (currentState == TerminalSessionState.MATCHING) {
                uiController.handleCancelMatching(context);
            } else {
                running = false;
            }
            return;
        }
    }

    /**
     * 화살표 키 처리
     */
    private void handleArrowKey(TerminalSessionContext context, int arrow) {
        switch (arrow) {
            case 'A' -> { // UP
                context.decrementMenuIndex(4);
                uiController.renderMenu(context);
            }
            case 'B' -> { // DOWN
                context.incrementMenuIndex(4);
                uiController.renderMenu(context);
            }
        }
    }

    /**
     * ESC 키 처리 (Manual/About → Menu)
     */
    private void handleEscKey(TerminalSessionContext context) {
        TerminalSessionState currentState = context.getCurrentState();
        if (currentState == TerminalSessionState.MANUAL_VIEW ||
            currentState == TerminalSessionState.ABOUT_VIEW) {
            context.transitionTo(TerminalSessionState.MAIN_MENU);
            uiController.renderMenu(context);
        }
    }

    /**
     * 배너 → 메뉴 자동 전환 스케줄링
     */
    private void scheduleTransitionToMenu(TerminalSessionContext context, int seconds) {
        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS)
                .execute(() -> {
                    if (context.getCurrentState() == TerminalSessionState.BANNER) {
                        context.transitionTo(TerminalSessionState.MAIN_MENU);
                        uiController.renderMenu(context);
                    }
                });
    }

    /**
     * Socket 종료
     */
    private void closeSocket() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.debug("Socket close error: {}", e.getMessage());
        }
    }
}
