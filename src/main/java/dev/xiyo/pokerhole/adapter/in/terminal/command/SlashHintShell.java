package dev.xiyo.pokerhole.adapter.in.terminal.command;

import dev.xiyo.pokerhole.server.room.GameRoomSummary;
import dev.xiyo.pokerhole.server.room.RoomRegistry;
import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * 터미널 TCP 세션에서 슬래시 명령을 처리하는 셸.
 */
@Component
@RequiredArgsConstructor
public class SlashHintShell {

    private final SlashHintRegistry hintRegistry;
    private final RoomRegistry roomRegistry;

    public void onConnect(SessionState state) {
        state.send("⌨️  슬래시 명령 도움말: /help 를 입력하면 힌트 명령을 확인할 수 있습니다.");
    }

    /**
     * 입력이 슬래시 명령이라면 처리하고, 그렇지 않으면 false 를 반환한다.
     */
    public boolean handle(SessionState state, String rawInput) {
        if (rawInput == null) {
            return false;
        }
        String input = rawInput.trim();
        if (!input.startsWith("/")) {
            return false;
        }
        if ("/".equals(input)) {
            state.send("ℹ️  슬래시 명령 사용법은 /help 로 확인할 수 있습니다.");
            return true;
        }

        String withoutSlash = input.substring(1).trim();
        if (withoutSlash.isEmpty()) {
            state.send("ℹ️  슬래시 명령 사용법은 /help 로 확인할 수 있습니다.");
            return true;
        }

        String command;
        String arguments;
        int firstSpace = withoutSlash.indexOf(' ');
        if (firstSpace >= 0) {
            command = withoutSlash.substring(0, firstSpace);
            arguments = withoutSlash.substring(firstSpace + 1).trim();
        } else {
            command = withoutSlash;
            arguments = "";
        }

        switch (command.toLowerCase(Locale.ROOT)) {
            case "help" -> handleHelp(state);
            case "rooms" -> handleRooms(state);
            case "hint" -> handleHint(state, arguments);
            default -> state.send("❓ 지원하지 않는 슬래시 명령입니다. /help 로 사용 가능한 목록을 확인하세요.");
        }

        return true;
    }

    private void handleHelp(SessionState state) {
        state.send("📘 슬래시 명령 목록:");
        state.send(" /help : 이 도움말을 표시합니다.");
        state.send(" /rooms : 현재 생성된 방 목록을 확인합니다.");
        state.send(" /hint <명령어> : 특정 명령어의 사용법을 확인합니다.");
    }

    private void handleRooms(SessionState state) {
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

    private void handleHint(SessionState state, String arguments) {
        if (arguments.isBlank()) {
            state.send("ℹ️  사용법: /hint <명령어>. 예) /hint ROOM CREATE");
            return;
        }

        hintRegistry.findHint(arguments)
                .ifPresentOrElse(lines -> {
                    state.send("💡 " + arguments.trim().toUpperCase(Locale.ROOT) + " 명령어 힌트:");
                    lines.forEach(state::send);
                }, () -> state.send("❔ 해당 명령어에 대한 힌트가 등록되어 있지 않습니다."));
    }
}
