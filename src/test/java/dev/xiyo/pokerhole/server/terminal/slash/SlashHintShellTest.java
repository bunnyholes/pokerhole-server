package dev.xiyo.pokerhole.server.terminal.slash;

import dev.xiyo.pokerhole.server.room.RoomRegistry;
import dev.xiyo.pokerhole.server.session.ParticipantConnection;
import dev.xiyo.pokerhole.server.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlashHintShellTest {

    private SlashHintRegistry hintRegistry;
    private RoomRegistry roomRegistry;
    private SlashHintShell shell;

    @BeforeEach
    void setUp() {
        hintRegistry = new SlashHintRegistry();
        roomRegistry = new RoomRegistry();
        shell = new SlashHintShell(hintRegistry, roomRegistry);
    }

    @Test
    void nonSlashInputIsIgnored() {
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        boolean handled = shell.handle(state, "ROOM LIST");

        assertThat(handled).isFalse();
        assertThat(connection.messages).isEmpty();
    }

    @Test
    void helpCommandPrintsUsage() {
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        boolean handled = shell.handle(state, "/help");

        assertThat(handled).isTrue();
        assertThat(connection.messages)
                .isNotEmpty()
                .first()
                .asString()
                .contains("슬래시 명령 목록");
    }

    @Test
    void hintCommandReturnsRegisteredHelp() {
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        boolean handled = shell.handle(state, "/hint ROOM CREATE");

        assertThat(handled).isTrue();
        assertThat(connection.messages)
                .anyMatch(line -> line.contains("ROOM CREATE"))
                .anyMatch(line -> line.contains("방장으로 입장"));
    }

    @Test
    void unknownHintShowsFallbackMessage() {
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        shell.handle(state, "/hint something");

        assertThat(connection.messages)
                .last()
                .asString()
                .contains("힌트가 등록되어 있지 않습니다");
    }

    @Test
    void roomsCommandDescribesEmptyState() {
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        shell.handle(state, "/rooms");

        assertThat(connection.messages)
                .last()
                .asString()
                .contains("현재 생성된 방이 없습니다");
    }

    @Test
    void roomsCommandListsExistingRooms() {
        roomRegistry.createRoom("테스트방");
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        shell.handle(state, "/rooms");

        assertThat(connection.messages)
                .anyMatch(line -> line.contains("테스트방"));
    }

    @Test
    void onConnectPromptsSlashHelp() {
        RecordingConnection connection = new RecordingConnection();
        SessionState state = new SessionState(connection);

        shell.onConnect(state);

        assertThat(connection.messages)
                .singleElement()
                .asString()
                .contains("/help");
    }

    private static class RecordingConnection implements ParticipantConnection {
        private final List<String> messages = new ArrayList<>();
        private boolean open = true;

        @Override
        public void send(String message) {
            messages.add(message);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }
    }
}
