package dev.xiyo.pokerhole.adapter.in.terminal.state;

import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;

import java.time.Instant;
import java.util.UUID;

/**
 * 터미널 세션 컨텍스트
 * 세션별 UI 상태 및 JLine Terminal 관리
 */
@Slf4j
@Getter
@Setter
public class TerminalSessionContext {

    /**
     * 세션 ID
     */
    private final String sessionId;

    /**
     * JLine Terminal 인스턴스
     */
    private final Terminal terminal;

    /**
     * 현재 UI 상태
     */
    private TerminalSessionState currentState;

    /**
     * 메뉴 선택 인덱스
     */
    private int selectedMenuIndex;

    /**
     * 매칭 요청 정보
     */
    private MatchingRequest matchingRequest;

    /**
     * 매칭 시작 시각
     */
    private Instant matchingStartTime;

    /**
     * 닉네임
     */
    private String nickname;

    public TerminalSessionContext(Terminal terminal) {
        this.sessionId = UUID.randomUUID().toString();
        this.terminal = terminal;
        this.currentState = TerminalSessionState.BANNER;
        this.selectedMenuIndex = 0;
    }

    /**
     * 상태 전환
     */
    public void transitionTo(TerminalSessionState newState) {
        log.debug("상태 전환: {} → {} (sessionId={})", currentState, newState, sessionId);
        this.currentState = newState;
    }

    /**
     * 메뉴 인덱스 증가 (순환)
     */
    public void incrementMenuIndex(int maxIndex) {
        selectedMenuIndex = (selectedMenuIndex + 1) % maxIndex;
    }

    /**
     * 메뉴 인덱스 감소 (순환)
     */
    public void decrementMenuIndex(int maxIndex) {
        selectedMenuIndex = (selectedMenuIndex - 1 + maxIndex) % maxIndex;
    }

    /**
     * 매칭 시작
     */
    public void startMatching(MatchingRequest request) {
        this.matchingRequest = request;
        this.matchingStartTime = Instant.now();
        this.currentState = TerminalSessionState.MATCHING;
    }

    /**
     * 매칭 경과 시간 (초)
     */
    public long getMatchingElapsedSeconds() {
        if (matchingStartTime == null) {
            return 0;
        }
        return Instant.now().getEpochSecond() - matchingStartTime.getEpochSecond();
    }
}
