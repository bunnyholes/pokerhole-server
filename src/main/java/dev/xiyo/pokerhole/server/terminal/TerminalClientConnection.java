package dev.xiyo.pokerhole.server.terminal;

import dev.xiyo.pokerhole.server.session.ParticipantConnection;
import dev.xiyo.pokerhole.server.session.SessionState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TerminalClientConnection implements Runnable, ParticipantConnection {

    private final Socket socket;
    private final TerminalCommandProcessor commandProcessor;
    @Getter
    private final SessionState state;
    private volatile PrintWriter writer;

    public TerminalClientConnection(Socket socket, TerminalCommandProcessor commandProcessor) {
        this.socket = socket;
        this.commandProcessor = commandProcessor;
        this.state = new SessionState(this);
    }

    @Override
    public void run() {
        log.info("Accepted terminal connection from {}", socket.getRemoteSocketAddress());
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            this.writer = writer;
            commandProcessor.onConnect(state);

            String line;
            while ((line = reader.readLine()) != null) {
                boolean keepAlive = commandProcessor.handle(state, line);
                if (!keepAlive) {
                    break;
                }
            }
        } catch (IOException ex) {
            log.warn("Terminal connection {} closed with error: {}", socket.getRemoteSocketAddress(), ex.getMessage());
        } finally {
            commandProcessor.handleDisconnect(state);
            close();
            log.info("Terminal connection {} disconnected", socket.getRemoteSocketAddress());
        }
    }

    @Override
    public void send(String message) {
        PrintWriter currentWriter = this.writer;
        if (currentWriter != null && !currentWriter.checkError()) {
            currentWriter.println(message);
        }
    }

    @Override
    public boolean isOpen() {
        return !socket.isClosed();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

}
