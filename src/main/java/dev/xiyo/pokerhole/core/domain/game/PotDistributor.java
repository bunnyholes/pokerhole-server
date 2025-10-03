package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 팟 분배 유틸리티
 * 동점자들 사이에 팟을 균등하게 분배하고 나머지 칩을 처리합니다.
 *
 * 분배 규칙:
 * 1. 팟을 승자 수로 나눈 몫을 각 승자에게 분배
 * 2. 나머지 칩은 딜러 버튼 다음 위치부터 순서대로 1칩씩 분배
 *
 * 예시:
 * - 1000칩, 2명 승자: 500, 500
 * - 1001칩, 2명 승자: 501, 500 (딜러 버튼 다음 플레이어가 501)
 * - 1000칩, 3명 승자: 334, 333, 333
 */
public class PotDistributor {

    /**
     * 팟을 승자들에게 분배합니다.
     *
     * @param pot 분배할 팟 금액
     * @param winners 승자 목록 (딜러 버튼 기준 순서대로 정렬되어야 함)
     * @param dealerButtonPosition 딜러 버튼 위치 (0부터 시작)
     * @return 각 승자가 받을 칩 수 (PlayerId -> 칩 수)
     * @throws IllegalArgumentException 팟이 음수이거나 승자 목록이 비어있는 경우
     */
    public Map<PlayerId, Integer> distribute(int pot, List<PlayerId> winners, int dealerButtonPosition) {
        validateInput(pot, winners);

        if (pot == 0) {
            return createEmptyDistribution(winners);
        }

        if (winners.size() == 1) {
            return createSingleWinnerDistribution(winners.get(0), pot);
        }

        return distributeAmongMultipleWinners(pot, winners, dealerButtonPosition);
    }

    /**
     * 입력값 검증
     */
    private void validateInput(int pot, List<PlayerId> winners) {
        if (pot < 0) {
            throw new IllegalArgumentException("Pot cannot be negative: " + pot);
        }

        if (winners == null || winners.isEmpty()) {
            throw new IllegalArgumentException("Winners list cannot be null or empty");
        }

        // 중복된 승자가 없는지 확인
        long distinctCount = winners.stream().distinct().count();
        if (distinctCount != winners.size()) {
            throw new IllegalArgumentException("Winners list contains duplicates");
        }
    }

    /**
     * 팟이 0일 때 빈 분배 생성
     */
    private Map<PlayerId, Integer> createEmptyDistribution(List<PlayerId> winners) {
        Map<PlayerId, Integer> distribution = new LinkedHashMap<>();
        for (PlayerId winner : winners) {
            distribution.put(winner, 0);
        }
        return distribution;
    }

    /**
     * 승자가 1명일 때 전체 팟 분배
     */
    private Map<PlayerId, Integer> createSingleWinnerDistribution(PlayerId winner, int pot) {
        Map<PlayerId, Integer> distribution = new LinkedHashMap<>();
        distribution.put(winner, pot);
        return distribution;
    }

    /**
     * 여러 승자에게 팟 분배
     * 나머지 칩은 딜러 버튼 다음 위치부터 순서대로 분배
     */
    private Map<PlayerId, Integer> distributeAmongMultipleWinners(
            int pot, List<PlayerId> winners, int dealerButtonPosition) {

        int winnerCount = winners.size();
        int baseAmount = pot / winnerCount;
        int remainder = pot % winnerCount;

        Map<PlayerId, Integer> distribution = new LinkedHashMap<>();

        // 딜러 버튼 다음 위치부터 순서대로 분배
        // winners 리스트는 이미 딜러 버튼 기준으로 정렬되어 있다고 가정
        for (int i = 0; i < winnerCount; i++) {
            PlayerId winner = winners.get(i);
            int amount = baseAmount;

            // 나머지 칩은 앞쪽 승자들에게 1칩씩 추가
            if (i < remainder) {
                amount += 1;
            }

            distribution.put(winner, amount);
        }

        return distribution;
    }

    /**
     * 승자 목록을 딜러 버튼 기준으로 재정렬합니다.
     *
     * @param winners 원본 승자 목록
     * @param allPlayers 모든 플레이어 목록 (게임 시작 시 순서)
     * @param dealerButtonPosition 딜러 버튼 위치
     * @return 딜러 버튼 다음 위치부터 시작하는 승자 목록
     */
    public List<PlayerId> reorderWinnersByDealerButton(
            List<PlayerId> winners,
            List<PlayerId> allPlayers,
            int dealerButtonPosition) {

        if (winners == null || allPlayers == null) {
            throw new IllegalArgumentException("Winners and allPlayers cannot be null");
        }

        if (dealerButtonPosition < 0 || dealerButtonPosition >= allPlayers.size()) {
            throw new IllegalArgumentException(
                "Invalid dealer button position: " + dealerButtonPosition +
                " (total players: " + allPlayers.size() + ")");
        }

        // 딜러 버튼 다음 위치부터 순환하면서 승자 순서 결정
        return winners.stream()
            .sorted((w1, w2) -> {
                int pos1 = allPlayers.indexOf(w1);
                int pos2 = allPlayers.indexOf(w2);

                // 딜러 버튼 기준 상대 위치 계산
                int relativePos1 = (pos1 - dealerButtonPosition + allPlayers.size()) % allPlayers.size();
                int relativePos2 = (pos2 - dealerButtonPosition + allPlayers.size()) % allPlayers.size();

                return Integer.compare(relativePos1, relativePos2);
            })
            .toList();
    }
}
