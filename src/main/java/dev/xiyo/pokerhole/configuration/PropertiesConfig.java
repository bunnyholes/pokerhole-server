package dev.xiyo.pokerhole.configuration;

import dev.xiyo.pokerhole.configuration.properties.GameProperties;
import dev.xiyo.pokerhole.configuration.properties.MatchingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 속성 설정을 활성화하는 구성 클래스
 */
@Configuration
@EnableConfigurationProperties({
    GameProperties.class,
    MatchingProperties.class
})
public class PropertiesConfig {
}
