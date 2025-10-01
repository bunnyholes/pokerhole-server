package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Hand;
import dev.xiyo.pokerhole.core.domain.card.Tier;
import dev.xiyo.pokerhole.core.domain.game.vo.HandResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 포커 핸드 평가 서비스 구현
 * 텍사스 홀덤 규칙에 따라 7장의 카드에서 최고의 5장 조합을 찾습니다.
 */
public class HandEvaluatorImpl implements HandEvaluator {

    @Override
    public HandResult evaluate(List<Card> playerCards, List<Card> communityCards) {
        if (playerCards == null || playerCards.size() != 2) {
            throw new IllegalArgumentException("플레이어 카드는 정확히 2장이어야 합니다");
        }
        if (communityCards == null || communityCards.isEmpty()) {
            throw new IllegalArgumentException("커뮤니티 카드가 필요합니다");
        }
        
        // 총 카드 수 확인 (최소 5장, 최대 7장)
        int totalCards = playerCards.size() + communityCards.size();
        if (totalCards < 5) {
            throw new IllegalArgumentException("최소 5장의 카드가 필요합니다");
        }
        if (totalCards > 7) {
            throw new IllegalArgumentException("최대 7장의 카드만 허용됩니다");
        }

        // 모든 카드를 합침
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(playerCards);
        allCards.addAll(communityCards);

        // 중복 카드 체크
        Set<Card> uniqueCards = new HashSet<>(allCards);
        if (uniqueCards.size() != allCards.size()) {
            throw new IllegalArgumentException("중복된 카드가 있습니다");
        }

        // 7장에서 5장을 선택하는 모든 조합 생성 (21가지)
        // 6장이면 6가지, 5장이면 1가지
        List<List<Card>> combinations = generateCombinations(allCards, 5);
        
        // 각 조합을 평가하여 최고의 핸드 찾기
        HandResult bestResult = null;
        for (List<Card> combination : combinations) {
            HandResult result = evaluateFiveCards(combination);
            if (bestResult == null || result.compareTo(bestResult) > 0) {
                bestResult = result;
            }
        }

        return bestResult;
    }

    @Override
    public HandResult evaluateFiveCards(List<Card> fiveCards) {
        if (fiveCards == null || fiveCards.size() != 5) {
            throw new IllegalArgumentException("정확히 5장의 카드가 필요합니다");
        }

        // 기존 Hand 클래스를 사용하여 평가
        Hand hand = new Hand();
        for (Card card : fiveCards) {
            hand.add(card);
        }
        
        // Hand 평가 수행
        hand.open();
        
        Tier tier = hand.getTier();
        
        // HandResult 생성
        // Hand 클래스의 내부 구조를 활용하여 족보 구성 카드와 키커를 구분
        List<Card> tierCards = new ArrayList<>();
        List<Card> kickerCards = new ArrayList<>();
        
        // Hand 클래스의 cards Map을 순회하며 분류
        // true인 카드는 족보 구성, false인 카드는 키커
        for (Card card : hand) {
            // Hand의 iterator는 cards.keySet()을 반환
            // 실제 분류는 Hand 클래스 내부 로직에 의존
            tierCards.add(card);
        }
        
        // 간단한 구현: 모든 카드를 tierCards에 넣고, 키커는 비워둠
        // (실제로는 Hand 클래스의 내부 구조를 더 활용할 수 있지만,
        // 현재는 Hand 클래스가 private 필드로 구분을 저장하므로 여기서는 단순화)
        return new HandResult(tier, new ArrayList<>(fiveCards), Collections.emptyList());
    }

    /**
     * n개의 원소에서 r개를 선택하는 모든 조합을 생성합니다.
     * C(n, r) 조합
     */
    private List<List<Card>> generateCombinations(List<Card> cards, int r) {
        List<List<Card>> result = new ArrayList<>();
        generateCombinationsHelper(cards, r, 0, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinationsHelper(List<Card> cards, int r, int start, 
                                           List<Card> current, List<List<Card>> result) {
        if (current.size() == r) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            generateCombinationsHelper(cards, r, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}
