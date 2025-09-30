package dev.xiyo.pokerhole.ui.cli;

import dev.xiyo.pokerhole.ui.model.PokerCliSeatState;
import dev.xiyo.pokerhole.ui.model.PokerCliTableState;
import dev.xiyo.pokerhole.ui.cli.render.PokerCliRenderer;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * 화살표 키와 엔터 입력으로 조작 가능한 CLI 포커 데모.
 */
public final class PokerCliDemo {
    private PokerCliDemo() {
    }

    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .jna(false)
                .jansi(true)
                .build();

        try (terminal) {
            terminal.enterRawMode();
            PokerCliTableState tableState = buildSampleTable();
            PokerCliController controller = new PokerCliController(tableState);
            PokerCliRenderer renderer = new PokerCliRenderer(terminal);
            BindingReader reader = new BindingReader(terminal.reader());
            KeyMap<ArrowCommand> keyMap = buildKeyMap();

            renderer.render(controller.viewModel());

            boolean running = true;
            while (running) {
                ArrowCommand command = reader.readBinding(keyMap, null, true);
                if (command == null) {
                    continue;
                }
                running = controller.handleCommand(command);
                renderer.render(controller.viewModel());
            }
        }
    }

    private static PokerCliTableState buildSampleTable() {
        List<PokerCliSeatState> seats = List.of(
                new PokerCliSeatState(SeatPosition.HERO_BOTTOM, "나", 1520, "행동 대기", List.of("[Ah]", "[Kd]"), true),
                new PokerCliSeatState(SeatPosition.LEFT, "민수", 980, "체크", List.of(), false),
                new PokerCliSeatState(SeatPosition.TOP, "지영", 1240, "레이즈 120", List.of(), false),
                new PokerCliSeatState(SeatPosition.RIGHT, "테스터", 760, "폴드", List.of(), false)
        );

        return new PokerCliTableState(
                240,
                Instant.now(),
                List.of("[7h]", "[7d]", "[Qc]"),
                seats,
                40,
                600
        );
    }

    private static KeyMap<ArrowCommand> buildKeyMap() {
        KeyMap<ArrowCommand> keyMap = new KeyMap<>();
        keyMap.bind(ArrowCommand.MOVE_LEFT, "\u001b[D");
        keyMap.bind(ArrowCommand.MOVE_RIGHT, "\u001b[C");
        keyMap.bind(ArrowCommand.MOVE_UP, "\u001b[A");
        keyMap.bind(ArrowCommand.MOVE_DOWN, "\u001b[B");
        keyMap.bind(ArrowCommand.CONFIRM, "\r", "\n");
        keyMap.bind(ArrowCommand.CANCEL, "\u001b");
        keyMap.bind(ArrowCommand.QUIT, "q", "Q", "\u0003");
        return keyMap;
    }
}
