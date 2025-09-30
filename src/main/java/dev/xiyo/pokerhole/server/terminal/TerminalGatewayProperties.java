package dev.xiyo.pokerhole.server.terminal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pokerhole.terminal")
public class TerminalGatewayProperties {
    /**
     * 터미널 TCP 게이트웨이를 활성화할지 여부입니다.
     */
    private boolean enabled = true;

    /**
     * 대기할 호스트 주소입니다.
     */
    private String host = "0.0.0.0";

    /**
     * 터미널 클라이언트와 통신할 포트입니다.
     */
    private int port = 7777;
}
