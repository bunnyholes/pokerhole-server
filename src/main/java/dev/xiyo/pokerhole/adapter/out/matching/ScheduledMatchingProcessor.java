package dev.xiyo.pokerhole.adapter.out.matching;

import dev.xiyo.pokerhole.configuration.properties.MatchingProperties;
import dev.xiyo.pokerhole.core.application.port.in.ai.RequestAIPlayerUseCase;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingNotificationPort;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingQueuePort;
import dev.xiyo.pokerhole.core.domain.ai.AIPlayer;
import dev.xiyo.pokerhole.core.domain.matching.MatchingQueue;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import dev.xiyo.pokerhole.core.domain.matching.event.MatchingCompleted;
import dev.xiyo.pokerhole.core.domain.matching.event.MatchingTimeout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 스케줄링된 매칭 프로세서
 * 주기적으로 큐를 체크하고 매칭을 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledMatchingProcessor {

    private final MatchingQueuePort queuePort;
    private final RequestAIPlayerUseCase requestAIPlayerUseCase;
    private final MatchingNotificationPort notificationPort;
    private final ApplicationEventPublisher eventPublisher;
    private final MatchingProperties properties;

    /**
     * 매칭 큐 처리 (5초마다)
     */
    @Scheduled(fixedDelayString = "${pokerhole.matching.queue-process-interval-ms:5000}")
    public void processMatchingQueue() {
        try {
            processRandomMatching();
            processCodeMatching();
            processTimeouts();
        } catch (Exception e) {
            log.error("매칭 큐 처리 중 오류 발생", e);
        }
    }

    /**
     * 랜덤 매칭 처리
     */
    private void processRandomMatching() {
        MatchingQueue queue = queuePort.getQueue();

        if (queue.canMatchRandom()) {
            List<MatchingRequest> pool = queuePort.extractRandomPool();
            log.info("랜덤 매칭 완료: {} 명 매칭됨", pool.size());

            MatchingCompleted event = MatchingCompleted.of(pool);
            eventPublisher.publishEvent(event);
            notificationPort.notifyMatchingCompleted(pool);
        }
    }

    /**
     * 코드 매칭 처리
     */
    private void processCodeMatching() {
        MatchingQueue queue = queuePort.getQueue();
        Optional<String> matchableCode = queue.findMatchableCode();

        if (matchableCode.isPresent()) {
            String code = matchableCode.get();
            List<MatchingRequest> pool = queuePort.extractCodePool(code);
            log.info("코드 매칭 완료: code={}, {} 명 매칭됨", code, pool.size());

            MatchingCompleted event = MatchingCompleted.of(pool);
            eventPublisher.publishEvent(event);
            notificationPort.notifyMatchingCompleted(pool);
        }
    }

    /**
     * 타임아웃 처리 (10초 초과)
     */
    private void processTimeouts() {
        if (!properties.isAiPlayerEnabled()) {
            return;
        }

        List<MatchingRequest> timedOutRequests = queuePort.findTimedOutRequests(
                properties.getWaitTimeoutSeconds()
        );

        if (!timedOutRequests.isEmpty()) {
            log.info("매칭 타임아웃 발생: {} 건", timedOutRequests.size());

            // AI 플레이어 투입
            int requiredAI = properties.getPlayersPerGame() - timedOutRequests.size();
            if (requiredAI > 0) {
                List<AIPlayer> aiPlayers = requestAIPlayerUseCase.requestAIPlayers(
                        RequestAIPlayerUseCase.RequestAIPlayerCommand.of(
                                requiredAI,
                                properties.getGameProperties().getInitialChips()
                        )
                );

                log.info("AI 플레이어 투입: {} 명", aiPlayers.size());

                MatchingTimeout timeoutEvent = MatchingTimeout.of(timedOutRequests);
                eventPublisher.publishEvent(timeoutEvent);

                // 타임아웃된 플레이어들을 매칭 완료 처리
                MatchingCompleted event = MatchingCompleted.of(timedOutRequests);
                eventPublisher.publishEvent(event);
                notificationPort.notifyMatchingCompleted(timedOutRequests);
            }
        }
    }
}
