package dev.xiyo.pokerhole.adapter.in.terminal;

import dev.xiyo.pokerhole.server.room.GameRoom;
import dev.xiyo.pokerhole.server.room.GameRoomSummary;
import dev.xiyo.pokerhole.server.room.RoomRegistry;
import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import dev.xiyo.pokerhole.adapter.in.terminal.command.SlashHintShell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalCommandProcessor {

    private final RoomRegistry roomRegistry;
    private final SlashHintShell slashHintShell;

    public void onConnect(SessionState state) {
        state.send("🎮 PokerHole 네트워크 서버에 오신 것을 환영합니다!");
        state.send("HELP 를 입력하면 사용 가능한 명령을 확인할 수 있습니다.");
        slashHintShell.onConnect(state);
    }

    public boolean handle(SessionState state, String rawInput) {
        String input = rawInput == null ? "" : rawInput.trim();
        if (input.isEmpty()) {
            return true;
        }

        if (slashHintShell.handle(state, input)) {
            return true;
        }

        String[] tokens = input.split("\\s+");
        String command = tokens[0].toUpperCase(Locale.ROOT);

        try {
            return switch (command) {
                case "HELP" -> {
                    printHelp(state);
                    yield true;
                }
                case "ROOM" -> {
                    handleRoomCommand(state, tokens, input);
                    yield true;
                }
                case "START" -> {
                    handleStart(state);
                    yield true;
                }
                case "LEAVE" -> {
                    handleLeave(state);
                    yield true;
                }
                case "QUIT", "EXIT" -> handleQuit(state);
                default -> {
                    state.send("❓ 알 수 없는 명령어입니다. HELP 를 참고하세요.");
                    yield true;
                }
            };
        } catch (IllegalArgumentException | IllegalStateException ex) {
            state.send("❌ " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to process command '{}': {}", input, ex.getMessage(), ex);
            state.send("⚠️ 서버에서 명령을 처리하는 중 문제가 발생했습니다.");
        }

        return true;
    }

    public void handleDisconnect(SessionState state) {
        Optional<GameRoom> room = state.currentRoom();
        room.ifPresent(currentRoom -> {
            currentRoom.handleDisconnect(state);
            boolean empty = currentRoom.isEmpty();
            if (empty) {
                roomRegistry.removeRoom(currentRoom.id());
            }
        });
    }

    private void printHelp(SessionState state) {
        state.send("📘 사용 가능한 명령어:");
        state.send(" - ROOM LIST : 생성된 방 목록 확인");
        state.send(" - ROOM CREATE <방이름> <닉네임> : 새 방을 만들고 방장으로 입장");
        state.send(" - ROOM JOIN <방ID> <닉네임> : 기존 방에 참가");
        state.send(" - START : 방장이 게임을 시작");
        state.send(" - LEAVE : 현재 방에서 나가기");
        state.send(" - QUIT : 서버 연결 종료");
    }

    private void handleRoomCommand(SessionState state, String[] tokens, String fullInput) {
        if (tokens.length < 2) {
            throw new IllegalArgumentException("ROOM 명령 뒤에는 LIST, CREATE, JOIN 중 하나를 입력해야 합니다.");
        }

        String subCommand = tokens[1].toUpperCase(Locale.ROOT);
        switch (subCommand) {
            case "LIST" -> handleRoomList(state);
            case "CREATE" -> handleRoomCreate(state, tokens);
            case "JOIN" -> handleRoomJoin(state, tokens);
            default -> throw new IllegalArgumentException("지원하지 않는 ROOM 하위 명령입니다: " + subCommand);
        }
    }

    private void handleRoomList(SessionState state) {
        List<GameRoomSummary> rooms = roomRegistry.listRooms();
        if (rooms.isEmpty()) {
            state.send("📭 현재 생성된 방이 없습니다. ROOM CREATE 명령으로 새 방을 만들어보세요.");
            return;
        }

        state.send("📋 참여 가능한 방 목록:");
        rooms.forEach(summary -> state.send(String.format(Locale.KOREA,
                " - %s (ID: %s) 인원 %d/%d",
                summary.name(), summary.id(), summary.participants(), summary.capacity())));
    }

    private void handleRoomCreate(SessionState state, String[] tokens) {
        ensureNotInRoom(state, "이미 방에 참여 중입니다. LEAVE 명령으로 나간 뒤 다시 시도하세요.");
        if (tokens.length < 4) {
            throw new IllegalArgumentException("사용법: ROOM CREATE <방이름> <닉네임>");
        }

        String roomName = tokens[2];
        String nickname = tokens[3];

        GameRoom room = roomRegistry.createRoom(roomName);
        try {
            room.join(state, nickname, true);
        } catch (RuntimeException ex) {
            roomRegistry.removeRoom(room.id());
            throw ex;
        }
    }

    private void handleRoomJoin(SessionState state, String[] tokens) {
        ensureNotInRoom(state, "이미 방에 참여 중입니다. LEAVE 명령으로 나간 뒤 다시 시도하세요.");
        if (tokens.length < 4) {
            throw new IllegalArgumentException("사용법: ROOM JOIN <방ID> <닉네임>");
        }

        String roomId = tokens[2].toUpperCase(Locale.ROOT);
        String nickname = tokens[3];

        GameRoom room = roomRegistry.findRoom(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 방이 존재하지 않습니다: " + roomId));
        room.join(state, nickname, false);
    }

    private void handleStart(SessionState state) {
        GameRoom room = state.currentRoom()
                .orElseThrow(() -> new IllegalStateException("게임을 시작하려면 먼저 방에 입장해야 합니다."));
        room.startRound(state);
    }

    private void handleLeave(SessionState state) {
        GameRoom room = state.currentRoom()
                .orElseThrow(() -> new IllegalStateException("현재 참여 중인 방이 없습니다."));
        
        // 라운드 중이면 예약, 아니면 즉시 나가기
        room.requestLeave(state);
        
        // 즉시 나간 경우에만 처리
        if (state.currentRoom().isEmpty()) {
            boolean empty = room.isEmpty();
            if (empty) {
                roomRegistry.removeRoom(room.id());
            }
            state.send("🏠 대기실로 이동했습니다.");
        }
    }

    private boolean handleQuit(SessionState state) {
        state.send("👋 연결을 종료합니다. 즐거운 게임 되세요!");
        return false;
    }

    private void ensureNotInRoom(SessionState state, String message) {
        if (state.currentRoom().isPresent()) {
            throw new IllegalStateException(message);
        }
    }
}
