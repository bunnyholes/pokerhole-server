package dev.xiyo.pokerhole.cli;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokerCliControllerTest {

    @Test
    void moveSelectionWrapsAround() {
        PokerCliController controller = new PokerCliController(sampleState());

        controller.handleCommand(ArrowCommand.MOVE_LEFT);
        controller.handleCommand(ArrowCommand.MOVE_LEFT);
        controller.handleCommand(ArrowCommand.MOVE_LEFT);

        PokerCliViewModel viewModel = controller.viewModel();
        assertThat(viewModel.actions().get(viewModel.selectedActionIndex())).isEqualTo("레이즈");
    }

    @Test
    void adjustRaiseRespectsBounds() {
        PokerCliController controller = new PokerCliController(sampleState());

        for (int i = 0; i < 50; i++) {
            controller.handleCommand(ArrowCommand.MOVE_UP);
        }
        PokerCliViewModel viewModel = controller.viewModel();
        assertThat(viewModel.raiseAmount()).isEqualTo(600);

        for (int i = 0; i < 200; i++) {
            controller.handleCommand(ArrowCommand.MOVE_DOWN);
        }
        viewModel = controller.viewModel();
        assertThat(viewModel.raiseAmount()).isEqualTo(40);
    }

    private PokerCliTableState sampleState() {
        List<PokerCliSeatState> seats = List.of(
                new PokerCliSeatState(SeatPosition.HERO_BOTTOM, "나", 1000, "행동 대기", List.of("[As]", "[Kh]"), true),
                new PokerCliSeatState(SeatPosition.LEFT, "상대1", 900, "체크", List.of(), false),
                new PokerCliSeatState(SeatPosition.TOP, "상대2", 800, "콜", List.of(), false)
        );
        return new PokerCliTableState(200, Instant.now(), List.of(), seats, 40, 600);
    }
}
