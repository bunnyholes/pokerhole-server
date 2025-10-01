package dev.xiyo.pokerhole.adapter.out.network.ssh;

import dev.xiyo.pokerhole.adapter.in.terminal.TerminalUIController;
import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionContext;
import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionState;
import dev.xiyo.pokerhole.adapter.out.network.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * SSH Shell 명령 - 터미널 UI 제공
 */
@Slf4j
@RequiredArgsConstructor
public class TerminalSshCommand implements Command {

    private final TerminalUIController uiController;
    private final SessionRegistry sessionRegistry;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private volatile boolean running = true;

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        log.info("SSH session started: {}", channel.getSession().getIoSession().getRemoteAddress());
        log.info("TERM type: {}", env.getEnv().get("TERM"));
        log.info("PTY modes: {}", env.getPtyModes());
        log.info("PTY columns: {}, rows: {}", env.getEnv().get("COLUMNS"), env.getEnv().get("LINES"));

        // SSH PTY 크기 파싱
        int cols = 80; // 기본값
        int rows = 24; // 기본값
        try {
            if (env.getEnv().get("COLUMNS") != null) {
                cols = Integer.parseInt(env.getEnv().get("COLUMNS"));
            }
            if (env.getEnv().get("LINES") != null) {
                rows = Integer.parseInt(env.getEnv().get("LINES"));
            }
        } catch (NumberFormatException e) {
            log.warn("PTY 크기 파싱 실패, 기본값 사용: {}x{}", cols, rows);
        }

        int finalCols = cols;
        int finalRows = rows;

        // JLine Terminal 생성 (SSH PTY 지원)
        try (Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(in, out)
                .type(env.getEnv().getOrDefault("TERM", "xterm-256color"))
                .size(new org.jline.terminal.Size(finalCols, finalRows))  // 명시적 크기 설정
                .signalHandler(Terminal.SignalHandler.SIG_IGN)
                .build()) {

            log.info("Terminal initialized: type={}, size={}x{}, name={}",
                    terminal.getType(), terminal.getWidth(), terminal.getHeight(), terminal.getName());

            TerminalSessionContext context = new TerminalSessionContext(terminal);
            sessionRegistry.register(context.getSessionId(), context);

            try {
                runUILoop(context);
            } finally {
                sessionRegistry.unregister(context.getSessionId());
            }

        } catch (Exception e) {
            log.error("SSH command execution failed", e);
        } finally {
            callback.onExit(0);
        }
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

            if (c == -2) {
                // EOF - client disconnected
                break;
            }

            handleKeyInput(context, reader, c);
        }
    }

    /**
     * 키 입력 처리
     */
    private void handleKeyInput(TerminalSessionContext context, NonBlockingReader reader, int c) throws IOException {
        log.info("키 입력: {} (0x{}) char={}", c, Integer.toHexString(c), (char)c);
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

    @Override
    public void destroy(ChannelSession channel) {
        running = false;
    }
}
