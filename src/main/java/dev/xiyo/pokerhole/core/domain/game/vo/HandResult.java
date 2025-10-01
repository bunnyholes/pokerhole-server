package dev.xiyo.pokerhole.core.domain.game.vo;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Tier;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 포커 핸드 평가 결과를 나타내는 Value Object
 * 텍사스 홀덤에서 7장의 카드(2장 홀카드 + 5장 커뮤니티 카드)로부터
 * 최고의 5장 조합을 평가한 결과를 담습니다.
 */
@Getter
@EqualsAndHashCode
public final class HandResult implements Comparable<HandResult> {
    private final Tier ranking;           // 핸드 랭킹 (족보)
    private final List<Card> cards;       // 족보를 구성하는 5장의 카드
    private final List<Card> kickers;     // 키커 카드들 (족보 구성 카드 제외, 높은 순)

    public HandResult(Tier ranking, List<Card> cards, List<Card> kickers) {
        if (ranking == null) {
            throw new IllegalArgumentException("Ranking cannot be null");
        }
        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Cards cannot be null or empty");
        }
        
        this.ranking = ranking;
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        this.kickers = kickers != null ? 
            Collections.unmodifiableList(new ArrayList<>(kickers)) : 
            Collections.emptyList();
    }

    /**
     * 두 핸드 결과를 비교합니다.
     * 표준 포커 규칙에 따라:
     * 1. 족보(ranking) 우선 비교
     * 2. 같은 족보면 구성 카드의 랭크 비교
     * 3. 그것도 같으면 키커 비교
     * 
     * @return 양수: this가 더 강함, 0: 동등, 음수: other가 더 강함
     */
    @Override
    public int compareTo(HandResult other) {
        // 1. 족보 비교 (높은 족보가 이김)
        int rankingCompare = this.ranking.compareTo(other.ranking);
        if (rankingCompare != 0) {
            return rankingCompare;
        }

        // 2. 같은 족보일 때 구성 카드의 랭크를 비교
        int cardCompare = compareCardsByRank(this.cards, other.cards);
        if (cardCompare != 0) {
            return cardCompare;
        }

        // 3. 키커 비교
        return compareCardsByRank(this.kickers, other.kickers);
    }

    /**
     * 카드 리스트를 랭크 기준으로 비교 (무늬는 무시)
     * 높은 랭크를 가진 카드가 있는 쪽이 이김
     */
    private int compareCardsByRank(List<Card> cards1, List<Card> cards2) {
        // 랭크별로 내림차순 정렬하여 비교
        List<Card> sorted1 = new ArrayList<>(cards1);
        List<Card> sorted2 = new ArrayList<>(cards2);
        
        sorted1.sort(Comparator.comparing(Card::getRank).reversed());
        sorted2.sort(Comparator.comparing(Card::getRank).reversed());

        int minSize = Math.min(sorted1.size(), sorted2.size());
        for (int i = 0; i < minSize; i++) {
            int rankCompare = sorted1.get(i).getRank().compareTo(sorted2.get(i).getRank());
            if (rankCompare != 0) {
                return rankCompare;
            }
        }

        // 모든 비교에서 동일하면 0 (동점)
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ranking);
        sb.append(" [");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(cards.get(i));
        }
        sb.append("]");
        
        if (!kickers.isEmpty()) {
            sb.append(" (Kickers: ");
            for (int i = 0; i < kickers.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(kickers.get(i));
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
}
