package dev.xiyo.pokerhole.ui.model;

import java.time.Instant;
import java.util.List;

/**
 * 테이블 상태 스냅샷.
 */
public record PokerCliTableState(
        long pot,
        Instant createdAt,
        List<String> communityCards,
        List<PokerCliSeatState> seats,
        int minimumRaise,
        int maximumRaise
) {
    public PokerCliTableState {
        communityCards = communityCards == null ? List.of() : List.copyOf(communityCards);
        seats = seats == null ? List.of() : List.copyOf(seats);
        minimumRaise = Math.max(minimumRaise, 0);
        maximumRaise = Math.max(maximumRaise, minimumRaise);
    }

    public PokerCliSeatState heroSeat() {
        return seats.stream()
                .filter(PokerCliSeatState::hero)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("히어로 좌석이 정의되지 않았습니다."));
    }

    public List<PokerCliSeatState> opponentSeats() {
        return seats.stream().filter(seat -> !seat.hero()).toList();
    }
}
