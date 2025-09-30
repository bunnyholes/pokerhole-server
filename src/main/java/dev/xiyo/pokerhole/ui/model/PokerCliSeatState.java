package dev.xiyo.pokerhole.ui.model;

import dev.xiyo.pokerhole.ui.cli.SeatPosition;

import java.util.List;

/**
 * CLI 렌더링용 좌석 상태.
 */
public record PokerCliSeatState(
        SeatPosition position,
        String name,
        long stack,
        String status,
        List<String> cards,
        boolean hero
) {
}
