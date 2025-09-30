package dev.xiyo.pokerhole.core.domain.game.event;

import dev.xiyo.pokerhole.core.domain.shared.DomainEvent;

/**
 * 게임 관련 도메인 이벤트의 마커 인터페이스
 * 모든 게임 이벤트는 이 인터페이스를 구현해야 합니다.
 */
public sealed interface GameEvent extends DomainEvent
    permits RoundStarted, RoundEnded, BettingPhaseStarted {
}
