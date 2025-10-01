package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.game.vo.HandResult;

import java.util.List;

/**
 * 포커 핸드 평가 서비스 인터페이스
 * 텍사스 홀덤에서 플레이어의 2장 홀카드와 5장 커뮤니티 카드를 조합하여
 * 가능한 최고의 5장 조합을 찾아 평가합니다.
 */
public interface HandEvaluator {
    
    /**
     * 주어진 카드들로부터 최고의 5장 조합을 찾아 평가합니다.
     * 
     * @param playerCards 플레이어의 홀 카드 (2장)
     * @param communityCards 커뮤니티 카드 (최대 5장)
     * @return 최고의 핸드 평가 결과
     * @throws IllegalArgumentException 카드 수가 부족하거나 잘못된 경우
     */
    HandResult evaluate(List<Card> playerCards, List<Card> communityCards);
    
    /**
     * 정확히 5장의 카드로 핸드를 평가합니다.
     * 
     * @param fiveCards 평가할 5장의 카드
     * @return 핸드 평가 결과
     * @throws IllegalArgumentException 카드 수가 5장이 아닌 경우
     */
    HandResult evaluateFiveCards(List<Card> fiveCards);
}
