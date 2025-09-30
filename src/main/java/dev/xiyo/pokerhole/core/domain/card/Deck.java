package dev.xiyo.pokerhole.core.domain.card;

import java.util.*;

/**
 * 덱의 접근은 딜러만 할 수 있도록 제한하기 위해 접근 제어자를 default로 설정
 * 이 클래스는 같은 패키지 내에서만 접근 가능합니다.
 * 외부에서 직접 Deck을 수정하거나 생성하지 못하게 하여 일관된 덱 관리가 가능하게 합니다.
 */
public class Deck {
    /**
     * 덱을 구성하는 카드들
     * 재사용이 불가능하도록 카드를 뽑을 때마다 제거되도록 큐의 특성을 활용하는 리스트로 구성
     */
    private final List<Card> cards;

    /**
     * Deck 생성자 - 프라이빗으로 선언하여 외부에서 Deck 인스턴스를 직접 생성하지 못하도록 제약합니다.
     * 새로운 덱을 생성하며, 생성과 동시에 카드들을 순서대로 추가합니다.
     * 이 생성자를 통해 덱은 항상 올바른 상태로 초기화되며, 이후 셔플 메서드를 통해 섞을 수 있습니다.
     */
    private Deck() {
        cards = new ArrayList<>();

        // 덱에 넣을 카드 순서대로 생성 (각 무늬별로 각 랭크를 순회하며 생성)
        // 동적 확장이 필요 없도록 처음부터 카드 전체 갯수인 52장을 확보하여 초기화
        List<Card> cardCase = new ArrayList<>(52);
        for (Suit suit : Suit.values())
            for (Rank rank : Rank.values())
                cardCase.add(new Card(suit, rank));

        // 덱에 카드들을 추가 (초기 순서로)
        this.cards.addAll(cardCase);
    }

    /**
     * 새로운 Deck 인스턴스를 생성하여 반환합니다.
     * Deck 생성자를 직접 호출하지 않고 이 정적 메서드를 통해서만 덱을 만들 수 있습니다.
     * 이렇게 함으로써 덱이 생성되는 방식을 제어할 수 있습니다.
     */
    public static Deck newDeck() {
        return new Deck();
    }

    /**
     * 덱에서 카드 한 장을 뽑습니다.
     * 덱이 비어 있는 경우 IllegalStateException을 던져 덱에 더 이상 카드가 없음을 알립니다.
     *
     * @return 뽑은 카드
     * @throws IllegalStateException 덱에 카드가 없을 때 호출될 경우
     */
    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("더 이상 카드가 없습니다.");
        }
        // 덱에서 마지막 카드를 뽑아 반환하고 리스트에서 제거
        return cards.removeLast();
    }

    /**
     * 덱을 무작위로 섞습니다.
     * 딜러만 이 메서드를 호출할 수 있도록 default 접근 제어자를 사용하여 같은 패키지 내에서만 접근 가능하게 합니다.
     * Random 객체를 사용하여 카드의 순서를 무작위로 섞습니다.
     */
    public void shuffle() {
        Collections.shuffle(this.cards, new Random());
    }
}
