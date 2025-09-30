package dev.xiyo.pokerhole.core.domain.round;

/**
 * 라운드 생명주기 상태
 */
public enum RoundLifecycleState {
    /**
     * 라운드 진행 중
     */
    IN_PROGRESS,
    
    /**
     * 라운드 완료, 결과 표시 중
     */
    COMPLETED,
    
    /**
     * 다음 라운드 대기 중 (5초 타이머)
     */
    WAITING_NEXT,
    
    /**
     * 라운드 종료됨
     */
    TERMINATED
}
