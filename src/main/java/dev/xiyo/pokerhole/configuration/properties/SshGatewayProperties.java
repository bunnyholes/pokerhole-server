package dev.xiyo.pokerhole.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSH 게이트웨이 설정
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "pokerhole.ssh")
public class SshGatewayProperties {

    /**
     * SSH 서버 활성화 여부
     */
    private boolean enabled = true;

    /**
     * SSH 서버 호스트
     */
    private String host = "0.0.0.0";

    /**
     * SSH 서버 포트
     */
    private int port = 2222;
}
