package dev.xiyo.pokerhole.core.domain.matching;

import dev.xiyo.pokerhole.core.domain.shared.AggregateRoot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 매칭 큐 Aggregate Root
 * 매칭 대기 중인 요청들을 관리
 */
@Getter
public class MatchingQueue {

    private final UUID id;
    private final Map<UUID, MatchingRequest> randomQueue;
    private final Map<String, List<MatchingRequest>> codeQueues;
    private final int playersPerGame;

    public MatchingQueue(int playersPerGame) {
        this.id = UUID.randomUUID();
        this.randomQueue = new ConcurrentHashMap<>();
        this.codeQueues = new ConcurrentHashMap<>();
        this.playersPerGame = playersPerGame;
    }

    /**
     * 랜덤 매칭 큐에 요청 추가
     */
    public void addToRandomQueue(MatchingRequest request) {
        if (!request.isRandomMatching()) {
            throw new IllegalArgumentException("랜덤 매칭 요청만 추가할 수 있습니다.");
        }
        randomQueue.put(request.getRequestId(), request);
    }

    /**
     * 코드 매칭 큐에 요청 추가
     */
    public void addToCodeQueue(MatchingRequest request) {
        if (!request.isCodeMatching()) {
            throw new IllegalArgumentException("코드 매칭 요청만 추가할 수 있습니다.");
        }
        String code = request.getMatchingCode().getCode();
        codeQueues.computeIfAbsent(code, k -> new ArrayList<>()).add(request);
    }

    /**
     * 랜덤 매칭이 가능한지 확인 (4명 이상)
     */
    public boolean canMatchRandom() {
        return randomQueue.size() >= playersPerGame;
    }

    /**
     * 코드 매칭이 가능한지 확인 (특정 코드에 4명 이상)
     */
    public Optional<String> findMatchableCode() {
        return codeQueues.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= playersPerGame)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * 랜덤 매칭 풀 생성 (4명 추출)
     */
    public List<MatchingRequest> extractRandomPool() {
        if (!canMatchRandom()) {
            throw new IllegalStateException("랜덤 매칭 풀을 생성할 수 없습니다.");
        }

        List<MatchingRequest> pool = randomQueue.values().stream()
                .limit(playersPerGame)
                .collect(Collectors.toList());

        pool.forEach(req -> randomQueue.remove(req.getRequestId()));
        return pool;
    }

    /**
     * 코드 매칭 풀 생성 (특정 코드에서 4명 추출)
     */
    public List<MatchingRequest> extractCodePool(String code) {
        List<MatchingRequest> queue = codeQueues.get(code);
        if (queue == null || queue.size() < playersPerGame) {
            throw new IllegalStateException("코드 매칭 풀을 생성할 수 없습니다.");
        }

        List<MatchingRequest> pool = queue.stream()
                .limit(playersPerGame)
                .collect(Collectors.toList());

        queue.removeAll(pool);
        if (queue.isEmpty()) {
            codeQueues.remove(code);
        }

        return pool;
    }

    /**
     * 타임아웃된 랜덤 매칭 요청 조회
     */
    public List<MatchingRequest> findTimedOutRandomRequests(int timeoutSeconds) {
        return randomQueue.values().stream()
                .filter(req -> req.isTimedOut(timeoutSeconds))
                .collect(Collectors.toList());
    }

    /**
     * 타임아웃된 코드 매칭 요청 조회
     */
    public List<MatchingRequest> findTimedOutCodeRequests(int timeoutSeconds) {
        return codeQueues.values().stream()
                .flatMap(List::stream)
                .filter(req -> req.isTimedOut(timeoutSeconds))
                .collect(Collectors.toList());
    }

    /**
     * 요청 제거 (세션 ID로)
     */
    public void removeBySessionId(String sessionId) {
        randomQueue.values().removeIf(req -> req.getSessionId().equals(sessionId));
        codeQueues.values().forEach(queue ->
            queue.removeIf(req -> req.getSessionId().equals(sessionId))
        );
    }

    /**
     * 현재 랜덤 큐 크기
     */
    public int getRandomQueueSize() {
        return randomQueue.size();
    }

    /**
     * 특정 코드 큐 크기
     */
    public int getCodeQueueSize(String code) {
        List<MatchingRequest> queue = codeQueues.get(code);
        return queue == null ? 0 : queue.size();
    }
}
