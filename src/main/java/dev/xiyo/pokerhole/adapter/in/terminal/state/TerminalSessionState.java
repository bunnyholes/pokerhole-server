package dev.xiyo.pokerhole.adapter.in.terminal.state;

/**
 * 터미널 세션 상태
 * 플레이어의 현재 UI 상태를 표현
 */
public enum TerminalSessionState {
    /**
     * 배너 표시 (3초)
     */
    BANNER,

    /**
     * 메인 메뉴 (랜덤 매칭, 코드 매칭, 설명서, 어바웃)
     */
    MAIN_MENU,

    /**
     * 매칭 대기 중
     */
    MATCHING,

    /**
     * 게임 로비 (4명 모임)
     */
    GAME_LOBBY,

    /**
     * 게임 진행 중
     */
    IN_GAME,

    /**
     * 게임 결과 표시 (5초)
     */
    GAME_RESULT,

    /**
     * 설명서 보기
     */
    MANUAL_VIEW,

    /**
     * 어바웃 보기
     */
    ABOUT_VIEW,

    /**
     * 연결 종료
     */
    DISCONNECTED
}
