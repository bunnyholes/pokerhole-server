package dev.xiyo.pokerhole.adapter.out.matching;

import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionContext;
import dev.xiyo.pokerhole.adapter.in.terminal.state.TerminalSessionState;
import dev.xiyo.pokerhole.adapter.out.network.session.SessionRegistry;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingNotificationPort;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 터미널 매칭 알림 어댑터
 * MatchingNotificationPort 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalMatchingNotificationAdapter implements MatchingNotificationPort {

    private final SessionRegistry sessionRegistry;

    @Override
    public void notifyMatchingCompleted(List<MatchingRequest> matchedPlayers) {
        log.info("✅ 매칭 완료 알림: {} 명", matchedPlayers.size());

        matchedPlayers.forEach(request -> {
            log.info("  → 플레이어: {} (sessionId: {})",
                request.getNickname(), request.getSessionId());

            // 세션 상태를 GAME_LOBBY로 전환
            sessionRegistry.findById(request.getSessionId()).ifPresent(context -> {
                context.transitionTo(TerminalSessionState.GAME_LOBBY);
                renderGameLobby(context);
            });
        });
    }

    @Override
    public void notifyMatchingProgress(String sessionId, int currentCount, int requiredCount) {
        log.info("⏳ 매칭 진행 상황: sessionId={}, {}/{} 명",
            sessionId, currentCount, requiredCount);

        // 매칭 진행 상황 UI 업데이트
        sessionRegistry.findById(sessionId).ifPresent(context -> {
            if (context.getCurrentState() == TerminalSessionState.MATCHING) {
                renderMatching(context, currentCount, requiredCount);
            }
        });
    }

    @Override
    public void notifyMatchingCancelled(String sessionId) {
        log.info("❌ 매칭 취소 알림: sessionId={}", sessionId);

        // 세션 상태를 MAIN_MENU로 전환
        sessionRegistry.findById(sessionId).ifPresent(context -> {
            context.transitionTo(TerminalSessionState.MAIN_MENU);
            renderMenu(context);
        });
    }

    /**
     * 게임 로비 렌더링
     */
    private void renderGameLobby(TerminalSessionContext context) {
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
     * 매칭 화면 렌더링
     */
    private void renderMatching(TerminalSessionContext context, int currentCount, int requiredCount) {
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
     * 메뉴 렌더링
     */
    private void renderMenu(TerminalSessionContext context) {
        clearScreen(context.getTerminal());

        StringBuilder menu = new StringBuilder();
        menu.append("\n📋 ═══════ 메인 메뉴 ═══════\n\n");
        menu.append("  ▶ 【랜덤 매칭】 빠른 게임 시작\n");
        menu.append("    코드 매칭 친구와 함께 플레이\n");
        menu.append("    게임 설명서 포커 규칙 보기\n");
        menu.append("    어바웃 프로젝트 정보\n");
        menu.append("\n화살표 ↑↓ : 이동 | Enter : 선택 | q : 종료\n");

        writeToTerminal(context.getTerminal(), menu.toString());
    }

    /**
     * 화면 클리어
     */
    private void clearScreen(Terminal terminal) {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
    }

    /**
     * 터미널에 텍스트 출력
     */
    private void writeToTerminal(Terminal terminal, String text) {
        terminal.writer().write(text);
        terminal.flush();
    }
}

