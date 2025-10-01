package dev.xiyo.pokerhole.core.application.port.in.matching;

/**
 * 매칭 취소 Use Case
 */
public interface CancelMatchingUseCase {

    /**
     * 매칭 대기 취소
     *
     * @param sessionId 세션 ID
     */
    void cancelMatching(String sessionId);
}
