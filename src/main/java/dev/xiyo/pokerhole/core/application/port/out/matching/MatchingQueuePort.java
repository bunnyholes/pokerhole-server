package dev.xiyo.pokerhole.core.application.port.out.matching;

import dev.xiyo.pokerhole.core.domain.matching.MatchingQueue;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;

import java.util.List;

/**
 * 매칭 큐 Port (출력 포트)
 */
public interface MatchingQueuePort {

    /**
     * 매칭 큐 조회
     */
    MatchingQueue getQueue();

    /**
     * 랜덤 매칭 요청 추가
     */
    void addToRandomQueue(MatchingRequest request);

    /**
     * 코드 매칭 요청 추가
     */
    void addToCodeQueue(MatchingRequest request);

    /**
     * 매칭 풀 추출 (랜덤)
     */
    List<MatchingRequest> extractRandomPool();

    /**
     * 매칭 풀 추출 (코드)
     */
    List<MatchingRequest> extractCodePool(String code);

    /**
     * 타임아웃된 요청 조회
     */
    List<MatchingRequest> findTimedOutRequests(int timeoutSeconds);

    /**
     * 세션 ID로 요청 제거
     */
    void removeBySessionId(String sessionId);
}
