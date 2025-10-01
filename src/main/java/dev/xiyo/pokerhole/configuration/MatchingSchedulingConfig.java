package dev.xiyo.pokerhole.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 매칭 스케줄링 설정
 * @Scheduled 활성화
 */
@Slf4j
@Configuration
@EnableScheduling
public class MatchingSchedulingConfig {

    public MatchingSchedulingConfig() {
        log.info("매칭 스케줄링 활성화");
    }
}
