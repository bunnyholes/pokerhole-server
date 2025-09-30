package dev.xiyo.pokerhole.ui.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 렌더러가 소비하는 최종 뷰 모델.
 */
public record PokerCliViewModel(
        long pot,
        String clockText,
        List<String> communityCards,
        PokerCliSeatState hero,
        List<PokerCliSeatState> opponents,
        List<String> actions,
        int selectedActionIndex,
        int raiseAmount,
        List<String> logLines
) {
    private static final DateTimeFormatter CLOCK_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static PokerCliViewModel fromState(PokerCliTableState tableState,
                                              List<String> actions,
                                              int selectedActionIndex,
                                              int raiseAmount,
                                              List<String> logLines) {
        return new PokerCliViewModel(
                tableState.pot(),
                CLOCK_FORMATTER.format(ZonedDateTime.now()),
                tableState.communityCards(),
                tableState.heroSeat(),
                tableState.opponentSeats(),
                actions,
                selectedActionIndex,
                raiseAmount,
                logLines
        );
    }
}
