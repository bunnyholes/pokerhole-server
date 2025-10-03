package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.game.vo.HandResult;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 승자 결정 도메인 서비스
 * 여러 플레이어의 핸드를 비교하여 승자를 결정합니다.
 * 동점(tie)인 경우 모든 승자를 반환합니다.
 */
public class WinnerResolver {

    /**
     * 주어진 플레이어들의 핸드 중에서 모든 승자를 찾습니다.
     * 동점인 경우 여러 명의 승자가 반환될 수 있습니다.
     *
     * @param playerHands 플레이어 ID와 핸드 결과의 맵
     * @return 승자 플레이어 ID 리스트 (동점 시 여러 명)
     * @throws IllegalArgumentException 입력이 null이거나 비어있는 경우
     */
    public List<PlayerId> findWinners(Map<PlayerId, HandResult> playerHands) {
        if (playerHands == null || playerHands.isEmpty()) {
            throw new IllegalArgumentException("Player hands cannot be null or empty");
        }

        // 1. 최고 핸드 찾기
        HandResult bestHand = findBestHand(playerHands.values());

        // 2. 최고 핸드와 동일한 핸드를 가진 모든 플레이어 찾기
        return playerHands.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(bestHand) == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 주어진 핸드들 중 최고의 핸드를 찾습니다.
     *
     * @param hands 핸드 결과 컬렉션
     * @return 최고의 핸드
     */
    private HandResult findBestHand(Collection<HandResult> hands) {
        return hands.stream()
                .max(HandResult::compareTo)
                .orElseThrow(() -> new IllegalStateException("No hands to evaluate"));
    }

    /**
     * 특정 플레이어가 승자인지 확인합니다.
     *
     * @param playerId 확인할 플레이어 ID
     * @param playerHands 모든 플레이어의 핸드 맵
     * @return 승자이면 true, 아니면 false
     */
    public boolean isWinner(PlayerId playerId, Map<PlayerId, HandResult> playerHands) {
        List<PlayerId> winners = findWinners(playerHands);
        return winners.contains(playerId);
    }

    /**
     * 동점 여부를 확인합니다.
     *
     * @param playerHands 모든 플레이어의 핸드 맵
     * @return 승자가 2명 이상이면 true (동점)
     */
    public boolean isTie(Map<PlayerId, HandResult> playerHands) {
        return findWinners(playerHands).size() > 1;
    }

    /**
     * 각 플레이어의 순위를 반환합니다.
     * 동점인 경우 같은 순위를 가집니다.
     *
     * @param playerHands 모든 플레이어의 핸드 맵
     * @return 플레이어 ID와 순위의 맵 (1위, 2위, ...)
     */
    public Map<PlayerId, Integer> getRankings(Map<PlayerId, HandResult> playerHands) {
        if (playerHands == null || playerHands.isEmpty()) {
            throw new IllegalArgumentException("Player hands cannot be null or empty");
        }

        // 플레이어를 핸드 결과 기준으로 내림차순 정렬
        List<Map.Entry<PlayerId, HandResult>> sortedEntries = playerHands.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // 순위 할당 (compareTo를 사용하여 동점 감지)
        Map<PlayerId, Integer> rankings = new HashMap<>();
        int currentRank = 1;
        HandResult previousHand = null;
        int playersAtCurrentRank = 0;

        for (Map.Entry<PlayerId, HandResult> entry : sortedEntries) {
            PlayerId playerId = entry.getKey();
            HandResult currentHand = entry.getValue();

            // 이전 핸드와 비교하여 동점인지 확인
            if (previousHand != null && currentHand.compareTo(previousHand) != 0) {
                // 다른 핸드이면 순위 증가
                currentRank += playersAtCurrentRank;
                playersAtCurrentRank = 0;
            }

            rankings.put(playerId, currentRank);
            playersAtCurrentRank++;
            previousHand = currentHand;
        }

        return rankings;
    }
}
