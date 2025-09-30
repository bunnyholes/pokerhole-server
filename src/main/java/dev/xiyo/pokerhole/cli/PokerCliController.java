package dev.xiyo.pokerhole.cli;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * 입력을 받아 테이블 상태를 업데이트하는 컨트롤러.
 */
public class PokerCliController {
    private static final List<String> DEFAULT_ACTIONS = List.of("폴드", "체크", "콜", "베팅", "레이즈");

    private final PokerCliTableState tableState;
    private final List<String> actions;
    private final Deque<String> logLines = new ArrayDeque<>();
    private final int raiseStep;
    private int selectedActionIndex = 2;
    private int raiseAmount;

    public PokerCliController(PokerCliTableState tableState) {
        this(tableState, DEFAULT_ACTIONS, Math.max(tableState.minimumRaise(), 10));
    }

    public PokerCliController(PokerCliTableState tableState, List<String> actions, int initialRaiseAmount) {
        this.tableState = Objects.requireNonNull(tableState, "tableState");
        this.actions = actions == null || actions.isEmpty() ? DEFAULT_ACTIONS : List.copyOf(actions);
        this.raiseStep = Math.max(tableState.minimumRaise(), 10);
        this.raiseAmount = Math.clamp(Math.max(initialRaiseAmount, raiseStep), tableState.minimumRaise(), tableState.maximumRaise());
        logLines.add("CLI 포커 인터페이스 시작");
        logLines.add("화살표 ←→ : 액션 선택, ↑↓ : 베팅 조정, Enter : 확정, q : 종료");
    }

    public PokerCliViewModel viewModel() {
        return PokerCliViewModel.fromState(tableState, actions, selectedActionIndex, raiseAmount, new ArrayList<>(logLines));
    }

    public boolean handleCommand(ArrowCommand command) {
        return switch (command) {
            case MOVE_LEFT -> {
                moveSelection(-1);
                yield true;
            }
            case MOVE_RIGHT -> {
                moveSelection(1);
                yield true;
            }
            case MOVE_UP -> {
                adjustRaise(raiseStep);
                yield true;
            }
            case MOVE_DOWN -> {
                adjustRaise(-raiseStep);
                yield true;
            }
            case CONFIRM -> {
                confirmAction();
                yield true;
            }
            case CANCEL -> {
                log("액션이 취소되었습니다.");
                yield true;
            }
            case QUIT -> {
                log("게임에서 이탈합니다.");
                yield false;
            }
        };
    }

    private void moveSelection(int delta) {
        selectedActionIndex = Math.floorMod(selectedActionIndex + delta, actions.size());
        log(String.format("선택된 액션: %s", actions.get(selectedActionIndex)));
    }

    private void adjustRaise(int delta) {
        int newAmount = Math.clamp(raiseAmount + delta, tableState.minimumRaise(), tableState.maximumRaise());
        if (newAmount != raiseAmount) {
            raiseAmount = newAmount;
            log(String.format("레이즈 금액 조정: %d", raiseAmount));
        } else {
            log("더 이상 조정할 수 없습니다.");
        }
    }

    private void confirmAction() {
        String action = actions.get(selectedActionIndex);
        if ("레이즈".equals(action) || "베팅".equals(action)) {
            log(String.format("%s %d칩을 선언했습니다.", action, raiseAmount));
        } else {
            log(String.format("%s 액션을 확정했습니다.", action));
        }
    }

    private void log(String message) {
        if (logLines.size() >= 6) {
            logLines.removeFirst();
        }
        logLines.addLast(message);
    }
}
