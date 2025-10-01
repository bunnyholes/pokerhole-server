package dev.xiyo.pokerhole.adapter.in.websocket.message;

/**
 * Server -> Client 메시지 타입
 */
public enum ServerMessageType {
    // 연결 응답
    REGISTER_SUCCESS,   // 등록 성공
    REGISTER_FAILURE,   // 등록 실패

    // 매칭 이벤트
    MATCHING_STARTED,   // 매칭 시작
    MATCHING_PROGRESS,  // 매칭 진행 (대기 인원)
    MATCHING_COMPLETED, // 매칭 완료
    MATCHING_CANCELLED, // 매칭 취소

    // 게임 상태
    GAME_STARTED,       // 게임 시작
    GAME_STATE_UPDATE,  // 게임 상태 업데이트
    PLAYER_ACTION,      // 플레이어 액션 알림
    ROUND_COMPLETED,    // 라운드 종료
    GAME_ENDED,         // 게임 종료

    // 에러
    ERROR,              // 일반 에러
    INVALID_ACTION      // 잘못된 액션
}
