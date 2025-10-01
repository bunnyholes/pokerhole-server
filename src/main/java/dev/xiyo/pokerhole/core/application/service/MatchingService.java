package dev.xiyo.pokerhole.core.application.service;

import dev.xiyo.pokerhole.core.application.UseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.CancelMatchingUseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.CreateMatchingCodeUseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.JoinCodeMatchingUseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.JoinRandomMatchingUseCase;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingNotificationPort;
import dev.xiyo.pokerhole.core.application.port.out.matching.MatchingQueuePort;
import dev.xiyo.pokerhole.core.domain.matching.MatchingCode;
import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 매칭 서비스
 * 매칭 관련 Use Case 구현
 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class MatchingService implements
        JoinRandomMatchingUseCase,
        JoinCodeMatchingUseCase,
        CreateMatchingCodeUseCase,
        CancelMatchingUseCase {

    private final MatchingQueuePort queuePort;
    private final MatchingNotificationPort notificationPort;

    @Override
    public MatchingRequest joinRandomMatching(JoinRandomMatchingCommand command) {
        log.info("랜덤 매칭 참가: sessionId={}, nickname={}", command.sessionId(), command.nickname());

        MatchingRequest request = MatchingRequest.forRandomMatching(
                command.sessionId(),
                command.nickname()
        );

        queuePort.addToRandomQueue(request);

        // 매칭 진행 상황 알림
        int currentCount = queuePort.getQueue().getRandomQueueSize();
        int requiredCount = 4; // playersPerGame
        notificationPort.notifyMatchingProgress(command.sessionId(), currentCount, requiredCount);

        log.info("랜덤 매칭 큐에 추가됨: requestId={}, 현재 대기: {}/{}",
                request.getRequestId(), currentCount, requiredCount);

        return request;
    }

    @Override
    public MatchingRequest joinCodeMatching(JoinCodeMatchingCommand command) {
        log.info("코드 매칭 참가: sessionId={}, nickname={}, code={}",
                command.sessionId(), command.nickname(), command.matchingCode());

        MatchingCode code = MatchingCode.from(command.matchingCode());
        MatchingRequest request = MatchingRequest.forCodeMatching(
                command.sessionId(),
                command.nickname(),
                code
        );

        queuePort.addToCodeQueue(request);

        // 매칭 진행 상황 알림
        int currentCount = queuePort.getQueue().getCodeQueueSize(code.getCode());
        int requiredCount = 4; // playersPerGame
        notificationPort.notifyMatchingProgress(command.sessionId(), currentCount, requiredCount);

        log.info("코드 매칭 큐에 추가됨: requestId={}, code={}, 현재 대기: {}/{}",
                request.getRequestId(), code.getCode(), currentCount, requiredCount);

        return request;
    }

    @Override
    public MatchingCode createMatchingCode() {
        MatchingCode code = MatchingCode.generate();
        log.info("매칭 코드 생성: {}", code.getCode());
        return code;
    }

    @Override
    public void cancelMatching(String sessionId) {
        log.info("매칭 취소: sessionId={}", sessionId);
        queuePort.removeBySessionId(sessionId);
        notificationPort.notifyMatchingCancelled(sessionId);
    }
}
