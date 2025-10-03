package dev.xiyo.pokerhole.core.domain.game.vo;

import dev.xiyo.pokerhole.core.domain.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Side Pot (사이드 팟) - Texas Hold'em에서 ALL_IN 플레이어가 있을 때 생성되는 별도의 팟
 *
 * <p>사이드 팟 생성 시나리오:</p>
 * <pre>
 * Player A: 1,000 칩 ALL_IN
 * Player B: 2,000 칩 베팅
 * Player C: 2,000 칩 베팅
 *
 * 결과:
 * - Main Pot: 3,000 (A, B, C가 경쟁)
 * - Side Pot: 2,000 (B, C만 경쟁)
 *
 * 승자 결정:
 * - A가 최고 패: A는 Main Pot 3,000만 가져감, Side Pot 2,000은 B/C 중 승자
 * - B가 최고 패: B는 Main Pot 3,000 + Side Pot 2,000 = 5,000 모두 가져감
 * </pre>
 */
public record SidePot(
    int amount,                     // 팟 금액
    List<Player> eligiblePlayers    // 이 팟을 가져갈 수 있는 플레이어 목록
) {

    /**
     * SidePot 생성자 - 방어적 복사를 통해 불변성 보장
     */
    public SidePot {
        if (amount < 0) {
            throw new IllegalArgumentException("팟 금액은 0 이상이어야 합니다: " + amount);
        }

        if (eligiblePlayers == null || eligiblePlayers.isEmpty()) {
            throw new IllegalArgumentException("적격 플레이어 목록은 비어있을 수 없습니다.");
        }

        // 방어적 복사 (외부에서 리스트 변경 방지)
        eligiblePlayers = new ArrayList<>(eligiblePlayers);
    }

    /**
     * 플레이어가 이 팟을 가져갈 자격이 있는지 확인
     */
    public boolean isEligible(Player player) {
        return eligiblePlayers.contains(player);
    }

    /**
     * 방어적 복사된 적격 플레이어 목록 반환
     */
    @Override
    public List<Player> eligiblePlayers() {
        return new ArrayList<>(eligiblePlayers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidePot sidePot = (SidePot) o;
        return amount == sidePot.amount && Objects.equals(eligiblePlayers, sidePot.eligiblePlayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, eligiblePlayers);
    }

    @Override
    public String toString() {
        return "SidePot{amount=" + amount + ", eligiblePlayers=" + eligiblePlayers.size() + "}";
    }
}
