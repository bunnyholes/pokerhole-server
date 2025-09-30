package dev.xiyo.pokerhole.core.application.port.out;

import dev.xiyo.pokerhole.core.domain.shared.DomainEvent;

/**
 * 이벤트 발행 포트
 * 도메인 이벤트를 외부로 발행하는 인터페이스입니다.
 */
public interface EventPublisher {
    
    /**
     * 도메인 이벤트를 발행합니다.
     * 
     * @param event 발행할 도메인 이벤트
     */
    void publish(DomainEvent event);
}
