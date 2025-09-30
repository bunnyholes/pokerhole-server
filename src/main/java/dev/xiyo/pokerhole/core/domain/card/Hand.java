package dev.xiyo.pokerhole.core.domain.card;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Hand implements Iterable<Card>, Comparable<Hand> {

    SortedMap<Card, Boolean> cards; // 패를 구성하는 카드의 집합
    private boolean isOpened = false; // 핸드 개방 상태

    private Tier tier;
    private final SortedSet<Card> tierValues;     // 티어 밸류, 보조 점수 계산 1, 티어를 구성하는 카드를 담는다.
    private final SortedSet<Card> kickers;        // 키커, 보조 점수 계산 2, 티어를 구성하지 않는 카드를 담는다.

    public Tier getTier() {
        if (!this.isOpened) {
            throw new IllegalStateException("개방되지 않은 핸드에서는 Tier를 조회할 수 없습니다");
        }
        return tier;
    }

    private final Map<Rank, Integer> rankCount; // 랭크값을 카운트, 최대 5가지 값이 들어감, 숫자 5
    private final Map<Suit, Integer> suitCount; // 수트값을 카운트, 최대 4가지 값이 들어감, 플러시 판단에 사용

    public Hand() {
        this.cards = new TreeMap<>();
        this.tierValues = new TreeSet<>();
        this.kickers = new TreeSet<>();
        this.rankCount = new EnumMap<>(Rank.class);
        this.suitCount = new EnumMap<>(Suit.class);
        this.tier = Tier.HIGH_CARD; // 초기 상태 기본값
    }

    public boolean add(Card card) {
        if (this.cards.size() >= 5) {
            throw new IllegalStateException("손에 들 수 있는 카드는 5장까지입니다.");
        }

        // 중복 카드 검증 추가
        if (this.cards.containsKey(card)) {
            throw new IllegalArgumentException("이미 존재하는 카드입니다: " + card);
        }

        this.cards.put(card, false); // 새로운 카드 추가
        this.rankCount.merge(card.getRank(), 1, Integer::sum); // 랭크 카운트
        this.suitCount.merge(card.getSuit(), 1, Integer::sum); // 수트 카운트

        return true;
    }

    // Reflection-based tests require this overload as part of the contract
    public boolean add(Suit suit, Rank rank) {
        return add(new Card(suit, rank));
    }

    public void clear() {
        cards.clear(); // 모든 카드 제거
        tier = Tier.HIGH_CARD; // 티어 초기화
        tierValues.clear(); // 티어를 구성하는 카드의 값 초기화
        kickers.clear(); // 패를 구성하는 키커 값 초기화

        rankCount.clear(); // 랭크 카운트 초기화
        suitCount.clear(); // 수트 카운트 초기화
    }

    @Override
    public Iterator<Card> iterator() {
        return cards.keySet().iterator();
    }

    @Override
    public String toString() {
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_BOLD = "\u001B[1m";
        final String ANSI_UNDERLINE = "\u001B[4m";
        final String ANSI_RESET = "\u001B[0m";

        String cardsString = this.cards.entrySet().stream()
                .map(entry -> {
                    String cardStr = entry.getKey().toString();
                    if (entry.getValue()) {
                        // true인 카드에 녹색, 볼드, 밑줄 스타일 적용
                        return ANSI_GREEN + ANSI_BOLD + ANSI_UNDERLINE + cardStr + ANSI_RESET;
                    }
                    return cardStr;
                })
                .collect(Collectors.joining(", "));
        if (this.cards.size() < 5) {
            return "미완성 패 (" + this.cards.size() + "/5) " + cardsString;
        }
        return this.tier.toString() + " " + cardsString;
    }

    @Override
    public int compareTo(Hand o) {
        // 개방되지 않은 핸드는 비교할 수 없음
        if (!this.isOpened || !o.isOpened) {
            throw new IllegalStateException("개방되지 않은 핸드는 비교할 수 없습니다");
        }
        
        // 1) 티어 비교: 높은 티어가 우선
        if (this.tier != o.tier) return o.tier.compareTo(this.tier);

        // 2) 같은 티어일 때, 표준 포커 룰에 따른 비교 (무늬는 절대 비교하지 않음)
        return switch (this.tier) {
            case ROYAL_FLUSH -> 0; // 로열 플러시는 모두 동급 (무승부)
            case STRAIGHT_FLUSH -> Integer.compare(
                    o.straightHighRankOrdinal(), this.straightHighRankOrdinal());
            case FOUR_OF_A_KIND -> {
                int fourCmp = Integer.compare(o.ofAKindRankOrdinal(4), this.ofAKindRankOrdinal(4));
                if (fourCmp != 0) yield fourCmp;
                yield compareRankLists(o.kickerOrdinals(), this.kickerOrdinals());
            }
            case FULL_HOUSE -> {
                int threeCmp = Integer.compare(o.ofAKindRankOrdinal(3), this.ofAKindRankOrdinal(3));
                if (threeCmp != 0) yield threeCmp;
                yield Integer.compare(o.ofAKindRankOrdinal(2), this.ofAKindRankOrdinal(2));
            }
            case FLUSH -> compareRankLists(o.allCardOrdinalsDesc(), this.allCardOrdinalsDesc());
            case STRAIGHT -> Integer.compare(
                    o.straightHighRankOrdinal(), this.straightHighRankOrdinal());
            case THREE_OF_A_KIND -> {
                int threeCmp = Integer.compare(o.ofAKindRankOrdinal(3), this.ofAKindRankOrdinal(3));
                if (threeCmp != 0) yield threeCmp;
                yield compareRankLists(o.kickerOrdinals(), this.kickerOrdinals());
            }
            case TWO_PAIR -> {
                List<Integer> oPairs = o.pairOrdinalsDesc();
                List<Integer> thisPairs = this.pairOrdinalsDesc();
                int pairCmp = compareRankLists(oPairs, thisPairs);
                if (pairCmp != 0) yield pairCmp;
                yield compareRankLists(o.kickerOrdinals(), this.kickerOrdinals());
            }
            case ONE_PAIR -> {
                int pairCmp = Integer.compare(o.ofAKindRankOrdinal(2), this.ofAKindRankOrdinal(2));
                if (pairCmp != 0) yield pairCmp;
                yield compareRankLists(o.kickerOrdinals(), this.kickerOrdinals());
            }
            case HIGH_CARD -> compareRankLists(o.allCardOrdinalsDesc(), this.allCardOrdinalsDesc());
        };
    }

    // ===== Helpers for hand comparison (ignore suits) =====

    // 모든 카드의 랭크를 높은 순으로 정렬하여 반환
    private List<Integer> allCardOrdinalsDesc() {
        List<Integer> list = new ArrayList<>(5);
        for (Card c : this.cards.keySet()) list.add(c.getRank().ordinal());
        list.sort(Comparator.reverseOrder());
        return list;
    }

    // n-of-a-kind의 랭크(ordinal)를 반환, 없으면 -1
    private int ofAKindRankOrdinal(int n) {
        for (Map.Entry<Rank, Integer> e : this.rankCount.entrySet())
            if (e.getValue() == n) return e.getKey().ordinal();
        return -1;
    }

    // 현재 핸드의 키커 랭크(ordinal)를 높은 순으로 반환
    private List<Integer> kickerOrdinals() {
        // 킥커는 족보를 구성하지 않는 카드들. rankCount 기준으로 n-of-a-kind가 아닌 것들 수집
        List<Integer> ks = new ArrayList<>(5);
        for (Map.Entry<Rank, Integer> e : this.rankCount.entrySet())
            if (e.getValue() == 1) ks.add(e.getKey().ordinal());
        ks.sort(Comparator.reverseOrder());
        return ks;
    }

    // 두 리스트를 같은 인덱스부터 높은 카드 기준으로 비교 (내림차순 가정)
    private static int compareRankLists(List<Integer> o, List<Integer> t) {
        int len = Math.min(o.size(), t.size());
        for (int i = 0; i < len; i++) {
            int cmp = Integer.compare(o.get(i), t.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(o.size(), t.size());
    }

    // 투페어인 경우, 두 페어의 랭크를 높은 순으로 반환
    private List<Integer> pairOrdinalsDesc() {
        List<Integer> pairs = new ArrayList<>(2);
        for (Map.Entry<Rank, Integer> e : this.rankCount.entrySet())
            if (e.getValue() == 2) pairs.add(e.getKey().ordinal());
        pairs.sort(Comparator.reverseOrder());
        return pairs;
    }

    // 스트레이트의 최고 랭크(ordinal). A-2-3-4-5는 5를 최고로 취급.
    private int straightHighRankOrdinal() {
        // 랭크만 추출하여 오름차순 정렬
        List<Integer> ranks = new ArrayList<>(5);
        for (Card c : this.cards.keySet()) ranks.add(c.getRank().ordinal());
        Collections.sort(ranks);

        // A-2-3-4-5 인지 확인: 0,1,2,3,12
        boolean isWheel = ranks.contains(Rank.ACE.ordinal())
                && ranks.contains(Rank.TWO.ordinal())
                && ranks.contains(Rank.THREE.ordinal())
                && ranks.contains(Rank.FOUR.ordinal())
                && ranks.contains(Rank.FIVE.ordinal());
        if (isWheel) return Rank.FIVE.ordinal();

        return ranks.getLast(); // 일반 스트레이트는 최댓값이 최고 랭크
    }

    private void evaluate() {
        // 02. 티어를 판단한다.
        if (this.isRoyalFlush()) {
            this.tier = Tier.ROYAL_FLUSH;
        } else if (this.isStraightFlush()) {
            this.tier = Tier.STRAIGHT_FLUSH;
        } else if (this.isFourOfAKind()) {
            this.tier = Tier.FOUR_OF_A_KIND;
        } else if (this.isFullHouse()) {
            this.tier = Tier.FULL_HOUSE;
        } else if (this.isFlush()) {
            this.tier = Tier.FLUSH;
        } else if (this.isStraight()) {
            this.tier = Tier.STRAIGHT;
        } else if (this.isThreeOfAKind()) {
            this.tier = Tier.THREE_OF_A_KIND;
        } else if (this.isTwoPair()) {
            this.tier = Tier.TWO_PAIR;
        } else if (this.isOnePair()) {
            this.tier = Tier.ONE_PAIR;
        } else {
            this.tier = Tier.HIGH_CARD;
        }

        // 03. 메인밸류와 키커를 구분한다.
        for (Card card : this.cards.keySet()) {
            if (Boolean.TRUE.equals(this.cards.get(card))) {
                this.tierValues.add(card); // 티어를 구성하는 카드로 추가
            } else {
                this.kickers.add(card); // 티어를 구성하지 않는 카드로 추가
            }
        }
    }

    private int countPair() {
        int pairCount = 0;
        for (int sameCardRankCount : rankCount.values())
            if (sameCardRankCount == 2) pairCount++;
        if (pairCount == 0) return 0; // 페어가 없음

        this.setRankCard(card -> rankCount.get(card.getRank()) == 2); // 페어를 구성하는 카드를 체크
        return pairCount;
    }

    private boolean isOnePair() {
        return countPair() == 1;
    }

    private boolean isTwoPair() {
        return countPair() == 2;
    }

    private boolean isThreeOfAKind() {
        Optional<Rank> threeOfAKindRank = rankCount.entrySet().stream()
                .filter(entry -> entry.getValue() == 3)
                .map(Map.Entry::getKey)
                .findFirst();
        if (threeOfAKindRank.isEmpty()) return false; // 비어있다면 쓰리카드 아님

        this.setRankCard(card -> card.getRank() == threeOfAKindRank.get()); // 쓰리카드를 구성하는 카드를 체크
        return true;
    }

    private boolean isFullHouse() {
        // 1. 랭크별 카드 개수를 필터링하여 분류
        boolean isThreeOfAKind = isThreeOfAKind();
        boolean isOnePair = isOnePair();

        return isThreeOfAKind && isOnePair;  // 풀 하우스의 조건을 성립하지 못함
    }

    private boolean isFourOfAKind() {
        // 1. 포카드 랭크를 찾는다. 단 하나만 나온다.
        Optional<Rank> optionalFourOfAKindRank = this.rankCount.entrySet().stream()
                .filter(entry -> entry.getValue() == 4)
                .map(Map.Entry::getKey)
                .findFirst();
        if (optionalFourOfAKindRank.isEmpty()) return false;  // 포카드 아님

        this.setRankCard(card -> card.getRank() == optionalFourOfAKindRank.get()); // 포카드를 구성하는 카드를 체크
        return true;
    }

    private boolean isRoyalFlush() {
        boolean isStraight = isStraight();
        boolean isFirstCardTen = this.cards.firstKey().getRank() == Rank.TEN; // 반드시 10과 비교해야 한다
        boolean isFlush = isFlush();
        return isStraight && isFirstCardTen && isFlush;
    }

    private boolean isStraightFlush() {
        return isFlush() && isStraight();
    }

    // 패가 스트레이트인지 확인
    private boolean isStraight() {
        Iterator<Card> iterator = this.cards.keySet().iterator();
        Card beforeCard = iterator.next(); // 최초의 카드, 반드시 존재하기 때문에 null 체크 생략

        // 연속된 숫자를 검사하고, 5, A가 연속되면 스트레이트로 판단
        while (iterator.hasNext()) {
            Card nextCard = iterator.next();

            // 5, A가 연속되면 스트레이트로 판단, 아래에서 2 > 3 > 4 > 5 까지의 검사하기 때문에 여기서는 5 > A 만 검사
            if (beforeCard.getRank().ordinal() == Rank.FIVE.ordinal() && nextCard.getRank().ordinal() == Rank.ACE.ordinal()) {
                break; // 루프를 탈출하여 와일 밖의 로직을 계속 실행한다.
            } else if (beforeCard.getRank().ordinal() + 1 != nextCard.getRank().ordinal()) return false; // 스트레이트 아님
            beforeCard = nextCard;
        }

        this.cards.replaceAll((c, v) -> true); // 모든 카드를 족보를 이루는 구성으로 변경
        return true;
    }

    private boolean isFlush() {
        if (!this.suitCount.containsValue(5)) return false;

        this.cards.replaceAll((c, v) -> true);
        return true;
    }

    private void setRankCard(Predicate<Card> predicate) {
        for (Card card : this.cards.keySet())
            if (predicate.test(card)) this.cards.put(card, true); // 족보를 이루는 구성으로 변경
    }

    public Hand open() {
        if (this.cards.size() != 5) {
            throw new IllegalStateException("패를 공개하기 위해서는 5장의 카드가 필요합니다.");
        }
        this.evaluate(); // 패의 티어를 판단
        this.isOpened = true; // 개방 상태 설정
        return this;
    }
}
