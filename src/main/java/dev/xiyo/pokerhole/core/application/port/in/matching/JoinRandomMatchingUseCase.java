package dev.xiyo.pokerhole.core.application.port.in.matching;

import dev.xiyo.pokerhole.core.domain.matching.MatchingRequest;

/**
 * 랜덤 매칭 참가 Use Case
 */
public interface JoinRandomMatchingUseCase {

    /**
     * 랜덤 매칭 큐에 참가
     *
     * @param command 참가 명령
     * @return 생성된 매칭 요청
     */
    MatchingRequest joinRandomMatching(JoinRandomMatchingCommand command);

    /**
     * 랜덤 매칭 참가 명령
     */
    record JoinRandomMatchingCommand(
            String sessionId,
            String nickname
    ) {
    }
}
