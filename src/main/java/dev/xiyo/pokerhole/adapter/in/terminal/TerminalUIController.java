package dev.xiyo.pokerhole.adapter.in.terminal;

import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionContext;
import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionState;
import dev.xiyo.pokerhole.core.application.port.in.matching.CancelMatchingUseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.JoinRandomMatchingUseCase;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import dev.xiyo.pokerhole.ui.cli.model.MenuOption;
import dev.xiyo.pokerhole.ui.cli.render.BannerRenderer;
import dev.xiyo.pokerhole.ui.cli.render.MenuRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.springframework.stereotype.Component;

/**
 * 터미널 UI 컨트롤러
 * 상태별 UI 렌더링 및 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalUIController {

    private final BannerRenderer bannerRenderer;
    private final MenuRenderer menuRenderer;
    private final JoinRandomMatchingUseCase joinRandomMatchingUseCase;
    private final CancelMatchingUseCase cancelMatchingUseCase;

    /**
     * 화면 클리어
     */
    public void clearScreen(Terminal terminal) {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
    }

    /**
     * 배너 렌더링
     */
    public void renderBanner(TerminalSessionContext context) {
        clearScreen(context.getTerminal());
        writeToTerminal(context.getTerminal(), bannerRenderer.renderBanner());
    }

    /**
     * 메뉴 렌더링
     */
    public void renderMenu(TerminalSessionContext context) {
        clearScreen(context.getTerminal());
        writeToTerminal(context.getTerminal(), menuRenderer.renderMenu(context.getSelectedMenuIndex()));
    }

    /**
     * 매칭 화면 렌더링
     */
    public void renderMatching(TerminalSessionContext context, int currentCount, int requiredCount) {
        clearScreen(context.getTerminal());

        long elapsedSeconds = context.getMatchingElapsedSeconds();
        int progress = (currentCount * 10) / requiredCount;

        StringBuilder sb = new StringBuilder();
        sb.append("\n┌─────────────────────────────────────┐\n");
        sb.append("│  🔍 매칭 중...                      │\n");
        sb.append("│                                     │\n");
        sb.append("│  ");
        for (int i = 0; i < 10; i++) {
            sb.append(i < progress ? "█" : "░");
        }
        sb.append(String.format("  %d/%d 명", currentCount, requiredCount));
        sb.append("        │\n");
        sb.append("│                                     │\n");
        sb.append(String.format("│  대기 시간: %d초                    │\n", elapsedSeconds));
        sb.append("│  (최대 10초)                        │\n");
        sb.append("│                                     │\n");
        sb.append("│  q: 매칭 취소                       │\n");
        sb.append("└─────────────────────────────────────┘\n");

        writeToTerminal(context.getTerminal(), sb.toString());
    }

    /**
     * 게임 설명서 렌더링
     */
    public void renderManual(TerminalSessionContext context) {
        clearScreen(context.getTerminal());
        writeToTerminal(context.getTerminal(), menuRenderer.renderManual());
    }

    /**
     * 어바웃 렌더링
     */
    public void renderAbout(TerminalSessionContext context) {
        clearScreen(context.getTerminal());
        writeToTerminal(context.getTerminal(), menuRenderer.renderAbout());
    }

    /**
     * 게임 로비 렌더링
     */
    public void renderGameLobby(TerminalSessionContext context) {
        clearScreen(context.getTerminal());

        StringBuilder sb = new StringBuilder();
        sb.append("\n┌─────────────────────────────────────┐\n");
        sb.append("│  ✅ 매칭 완료!                      │\n");
        sb.append("│                                     │\n");
        sb.append("│  4명의 플레이어가 모였습니다        │\n");
        sb.append("│                                     │\n");
        sb.append("│  잠시 후 게임이 시작됩니다...       │\n");
        sb.append("│                                     │\n");
        sb.append("└─────────────────────────────────────┘\n");

        writeToTerminal(context.getTerminal(), sb.toString());
    }

    /**
     * 메뉴 선택 처리
     */
    public void handleMenuSelection(TerminalSessionContext context) {
        MenuOption selected = MenuOption.values()[context.getSelectedMenuIndex()];
        log.info("메뉴 선택: {} (sessionId={})", selected, context.getSessionId());

        switch (selected) {
            case RANDOM_MATCHING -> handleRandomMatching(context);
            case CODE_MATCHING -> handleCodeMatching(context);
            case MANUAL -> {
                context.transitionTo(TerminalSessionState.MANUAL_VIEW);
                renderManual(context);
            }
            case ABOUT -> {
                context.transitionTo(TerminalSessionState.ABOUT_VIEW);
                renderAbout(context);
            }
        }
    }

    /**
     * 랜덤 매칭 시작
     */
    private void handleRandomMatching(TerminalSessionContext context) {
        String nickname = context.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = "Player_" + context.getSessionId().substring(0, 6);
            context.setNickname(nickname);
        }

        MatchingRequest request = joinRandomMatchingUseCase.joinRandomMatching(
                new JoinRandomMatchingUseCase.JoinRandomMatchingCommand(
                        context.getSessionId(),
                        nickname
                )
        );

        context.startMatching(request);
        renderMatching(context, 1, 4);
    }

    /**
     * 코드 매칭 시작
     */
    private void handleCodeMatching(TerminalSessionContext context) {
        clearScreen(context.getTerminal());
        writeToTerminal(context.getTerminal(), "\n코드 매칭 기능은 준비 중입니다.\n\nESC 키로 메뉴로 돌아가세요.\n");
    }

    /**
     * 매칭 취소
     */
    public void handleCancelMatching(TerminalSessionContext context) {
        if (context.getMatchingRequest() != null) {
            cancelMatchingUseCase.cancelMatching(context.getSessionId());
            context.setMatchingRequest(null);
            context.setMatchingStartTime(null);
        }

        context.transitionTo(TerminalSessionState.MAIN_MENU);
        renderMenu(context);
    }

    /**
     * 터미널에 텍스트 출력
     */
    private void writeToTerminal(Terminal terminal, String text) {
        terminal.writer().write(text);
        terminal.flush();
    }
}
