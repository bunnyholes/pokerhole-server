package com.pokerhole.server.terminal;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalTcpServer {

    private final TerminalCommandProcessor commandProcessor;
    private final TerminalGatewayProperties properties;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService clientExecutor;
    private ServerSocket serverSocket;
    private Thread acceptThread;

    @PostConstruct
    public void start() throws IOException {
        if (!properties.isEnabled()) {
            log.info("터미널 TCP 게이트웨이가 비활성화되어 있습니다.");
            return;
        }

        if (running.compareAndSet(false, true)) {
            clientExecutor = Executors.newVirtualThreadPerTaskExecutor();

            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(properties.getHost(), properties.getPort()));

            acceptThread = Thread.ofPlatform().name("terminal-accept-loop", 0)
                    .daemon(true)
                    .start(this::acceptLoop);

            log.info("Terminal TCP 서버가 {}:{} 에서 대기 중입니다.", properties.getHost(), properties.getPort());
        }
    }

    @PreDestroy
    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException ex) {
                log.debug("서버 소켓 종료 중 오류: {}", ex.getMessage());
            }

            if (acceptThread != null) {
                acceptThread.interrupt();
            }

            if (clientExecutor != null) {
                clientExecutor.shutdownNow();
            }

            log.info("Terminal TCP 서버를 종료했습니다.");
        }
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(true);
                clientExecutor.submit(new TerminalClientConnection(socket, commandProcessor));
            } catch (SocketException ex) {
                if (running.get()) {
                    log.warn("터미널 서버 소켓 오류: {}", ex.getMessage());
                }
            } catch (IOException ex) {
                if (running.get()) {
                    log.error("터미널 연결 수락 중 오류", ex);
                }
            }
        }
    }
}
