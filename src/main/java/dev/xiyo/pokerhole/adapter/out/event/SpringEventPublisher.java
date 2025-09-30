package dev.xiyo.pokerhole.adapter.out.event;

import dev.xiyo.pokerhole.core.application.port.out.EventPublisher;
import dev.xiyo.pokerhole.core.domain.shared.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring 이벤트를 사용한 EventPublisher 구현체
 * 도메인 이벤트를 Spring의 ApplicationEventPublisher를 통해 발행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
