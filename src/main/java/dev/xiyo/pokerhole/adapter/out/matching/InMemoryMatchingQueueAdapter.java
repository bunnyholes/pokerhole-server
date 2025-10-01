package dev.xiyo.pokerhole.adapter.out.matching;

import dev.xiyo.pokerhole.configuration.properties.MatchingProperties;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingQueuePort;
import dev.xiyo.pokerhole.core.domain.matching.MatchingQueue;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 인메모리 매칭 큐 어댑터
 * MatchingQueuePort 구현
 */
@Slf4j
@Component
public class InMemoryMatchingQueueAdapter implements MatchingQueuePort {

    private final MatchingQueue queue;
    private final int waitTimeoutSeconds;

    public InMemoryMatchingQueueAdapter(MatchingProperties properties) {
        this.queue = new MatchingQueue(properties.getPlayersPerGame());
        this.waitTimeoutSeconds = properties.getWaitTimeoutSeconds();
        log.info("InMemoryMatchingQueueAdapter 초기화: playersPerGame={}, timeout={}초",
                properties.getPlayersPerGame(), waitTimeoutSeconds);
    }

    @Override
    public MatchingQueue getQueue() {
        return queue;
    }

    @Override
    public void addToRandomQueue(MatchingRequest request) {
        queue.addToRandomQueue(request);
        log.debug("랜덤 큐에 추가: requestId={}, 현재 큐 크기={}",
                request.getRequestId(), queue.getRandomQueueSize());
    }

    @Override
    public void addToCodeQueue(MatchingRequest request) {
        queue.addToCodeQueue(request);
        String code = request.getMatchingCode().getCode();
        log.debug("코드 큐에 추가: requestId={}, code={}, 현재 큐 크기={}",
                request.getRequestId(), code, queue.getCodeQueueSize(code));
    }

    @Override
    public List<MatchingRequest> extractRandomPool() {
        if (!queue.canMatchRandom()) {
            return List.of();
        }
        List<MatchingRequest> pool = queue.extractRandomPool();
        log.info("랜덤 매칭 풀 추출: {} 명", pool.size());
        return pool;
    }

    @Override
    public List<MatchingRequest> extractCodePool(String code) {
        List<MatchingRequest> pool = queue.extractCodePool(code);
        log.info("코드 매칭 풀 추출: code={}, {} 명", code, pool.size());
        return pool;
    }

    @Override
    public List<MatchingRequest> findTimedOutRequests(int timeoutSeconds) {
        List<MatchingRequest> timedOut = new ArrayList<>();
        timedOut.addAll(queue.findTimedOutRandomRequests(timeoutSeconds));
        timedOut.addAll(queue.findTimedOutCodeRequests(timeoutSeconds));

        if (!timedOut.isEmpty()) {
            log.info("타임아웃된 요청 발견: {} 건", timedOut.size());
        }

        return timedOut;
    }

    @Override
    public void removeBySessionId(String sessionId) {
        queue.removeBySessionId(sessionId);
        log.debug("세션 제거: sessionId={}", sessionId);
    }
}
