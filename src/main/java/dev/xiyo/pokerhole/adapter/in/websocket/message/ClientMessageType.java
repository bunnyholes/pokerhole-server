package dev.xiyo.pokerhole.adapter.in.websocket.message;

/**
 * Client -> Server 메시지 타입
 */
public enum ClientMessageType {
    // 연결 관리
    REGISTER,           // 최초 접속 (UUID, Nickname)
    HEARTBEAT,          // 연결 유지

    // 매칭
    JOIN_RANDOM_MATCH,  // 랜덤 매칭 참가
    JOIN_CODE_MATCH,    // 코드 매칭 참가
    CANCEL_MATCHING,    // 매칭 취소

    // 게임 액션
    CALL,               // 콜
    RAISE,              // 레이즈
    FOLD,               // 폴드
    CHECK,              // 체크
    ALL_IN,             // 올인

    // 기타
    LEAVE_GAME,         // 게임 나가기
    CHAT_MESSAGE        // 채팅
}
