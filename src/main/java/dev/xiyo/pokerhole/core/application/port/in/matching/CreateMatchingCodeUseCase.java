package dev.xiyo.pokerhole.core.application.port.in.matching;

import dev.xiyo.pokerhole.core.domain.matching.MatchingCode;

/**
 * 매칭 코드 생성 Use Case
 */
public interface CreateMatchingCodeUseCase {

    /**
     * 새로운 매칭 코드 생성
     *
     * @return 생성된 매칭 코드
     */
    MatchingCode createMatchingCode();
}
