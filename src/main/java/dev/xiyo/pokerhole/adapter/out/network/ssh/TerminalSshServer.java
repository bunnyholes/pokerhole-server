package dev.xiyo.pokerhole.adapter.out.network.ssh;

import dev.xiyo.pokerhole.adapter.in.terminal.TerminalUIController;
import dev.xiyo.pokerhole.adapter.out.network.session.SessionRegistry;
import dev.xiyo.pokerhole.configuration.properties.SshGatewayProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * SSH 서버 - 터미널 UI 제공
 * Apache MINA SSHD 기반
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalSshServer {

    private final TerminalUIController uiController;
    private final SessionRegistry sessionRegistry;
    private final SshGatewayProperties properties;

    private SshServer sshd;

    @PostConstruct
    public void start() throws IOException {
        if (!properties.isEnabled()) {
            log.info("SSH 게이트웨이가 비활성화되어 있습니다.");
            return;
        }

        sshd = SshServer.setUpDefaultServer();
        sshd.setHost(properties.getHost());
        sshd.setPort(properties.getPort());

        // Host key 생성 (실제 운영에서는 고정된 키 사용 권장)
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
                Paths.get("hostkey.ser")
        ));

        // 인증 없이 접속 허용 (개발 단계)
        sshd.setPasswordAuthenticator((username, password, session) -> true);

        // Shell 명령 팩토리 설정
        sshd.setShellFactory(channel -> new TerminalSshCommand(uiController, sessionRegistry));

        sshd.start();

        log.info("SSH 서버가 {}:{} 에서 대기 중입니다. (접속: ssh localhost -p {})",
                properties.getHost(), properties.getPort(), properties.getPort());
    }

    @PreDestroy
    public void stop() throws IOException {
        if (sshd != null && sshd.isStarted()) {
            sshd.stop();
            log.info("SSH 서버를 종료했습니다.");
        }
    }
}
