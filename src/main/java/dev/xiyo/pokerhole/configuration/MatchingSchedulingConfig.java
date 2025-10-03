package dev.xiyo.pokerhole.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 매칭 스케줄링 및 비동기 설정
 * @Scheduled, @Async 활성화
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
public class MatchingSchedulingConfig {

    public MatchingSchedulingConfig() {
        log.info("매칭 스케줄링 및 비동기 처리 활성화");
    }
}
