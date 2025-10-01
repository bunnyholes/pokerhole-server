package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Rank;
import dev.xiyo.pokerhole.core.domain.card.Suit;
import dev.xiyo.pokerhole.core.domain.card.Tier;
import dev.xiyo.pokerhole.core.domain.game.vo.HandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HandEvaluator 테스트")
class HandEvaluatorTest {

    private HandEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new HandEvaluatorImpl();
    }

    @Nested
    @DisplayName("evaluateFiveCards 테스트")
    class EvaluateFiveCardsTest {

        @Test
        @DisplayName("로얄 플러시 - A, K, Q, J, 10 같은 무늬")
        void testRoyalFlush() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.ROYAL_FLUSH);
            assertThat(result.getCards()).hasSize(5);
        }

        @Test
        @DisplayName("스트레이트 플러시 - 연속된 5장, 같은 무늬")
        void testStraightFlush() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.EIGHT),
                new Card(Suit.DIAMONDS, Rank.SEVEN),
                new Card(Suit.DIAMONDS, Rank.SIX),
                new Card(Suit.DIAMONDS, Rank.FIVE)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.STRAIGHT_FLUSH);
        }

        @Test
        @DisplayName("포카드 - 같은 랭크 4장")
        void testFourOfAKind() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.ACE)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.FOUR_OF_A_KIND);
        }

        @Test
        @DisplayName("풀하우스 - 3장 + 2장")
        void testFullHouse() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.DIAMONDS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.JACK)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.FULL_HOUSE);
        }

        @Test
        @DisplayName("플러시 - 같은 무늬 5장")
        void testFlush() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.FIVE),
                new Card(Suit.CLUBS, Rank.TWO)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.FLUSH);
        }

        @Test
        @DisplayName("스트레이트 - 연속된 5장")
        void testStraight() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.EIGHT),
                new Card(Suit.SPADES, Rank.SEVEN),
                new Card(Suit.HEARTS, Rank.SIX)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.STRAIGHT);
        }

        @Test
        @DisplayName("스트레이트 (A-2-3-4-5) - 백 스트레이트")
        void testWheelStraight() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.TWO),
                new Card(Suit.CLUBS, Rank.THREE),
                new Card(Suit.SPADES, Rank.FOUR),
                new Card(Suit.HEARTS, Rank.FIVE)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.STRAIGHT);
        }

        @Test
        @DisplayName("쓰리카드 - 같은 랭크 3장")
        void testThreeOfAKind() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.HEARTS, Rank.KING)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.THREE_OF_A_KIND);
        }

        @Test
        @DisplayName("투페어 - 2장 페어 2개")
        void testTwoPair() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.DIAMONDS, Rank.JACK),
                new Card(Suit.CLUBS, Rank.EIGHT),
                new Card(Suit.SPADES, Rank.EIGHT),
                new Card(Suit.HEARTS, Rank.ACE)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.TWO_PAIR);
        }

        @Test
        @DisplayName("원페어 - 2장 페어 1개")
        void testOnePair() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.QUEEN)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.ONE_PAIR);
        }

        @Test
        @DisplayName("하이카드 - 아무 조합도 없음")
        void testHighCard() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.NINE)
            );

            HandResult result = evaluator.evaluateFiveCards(cards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.HIGH_CARD);
        }

        @Test
        @DisplayName("5장이 아닌 경우 예외 발생")
        void testInvalidCardCount() {
            List<Card> fourCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK)
            );

            assertThatThrownBy(() -> evaluator.evaluateFiveCards(fourCards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정확히 5장");
        }
    }

    @Nested
    @DisplayName("evaluate (7장에서 최고 조합) 테스트")
    class EvaluateSevenCardsTest {

        @Test
        @DisplayName("7장에서 로얄 플러시 찾기")
        void testFindRoyalFlushFromSeven() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.HEARTS, Rank.KING)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.TWO),
                new Card(Suit.CLUBS, Rank.THREE)
            );

            HandResult result = evaluator.evaluate(playerCards, communityCards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.ROYAL_FLUSH);
        }

        @Test
        @DisplayName("7장에서 포카드 찾기")
        void testFindFourOfAKindFromSeven() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.DIAMONDS, Rank.QUEEN)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.JACK)
            );

            HandResult result = evaluator.evaluate(playerCards, communityCards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.FOUR_OF_A_KIND);
        }

        @Test
        @DisplayName("7장에서 풀하우스 찾기")
        void testFindFullHouseFromSeven() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.KING)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.DIAMONDS, Rank.TWO),
                new Card(Suit.CLUBS, Rank.THREE)
            );

            HandResult result = evaluator.evaluate(playerCards, communityCards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.FULL_HOUSE);
        }

        @Test
        @DisplayName("6장에서 최고 조합 찾기 (플랍)")
        void testEvaluateWithSixCards() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.TWO)
            );

            HandResult result = evaluator.evaluate(playerCards, communityCards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.STRAIGHT);
        }

        @Test
        @DisplayName("5장만 있을 때 평가")
        void testEvaluateWithFiveCards() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.ACE)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.QUEEN)
            );

            HandResult result = evaluator.evaluate(playerCards, communityCards);
            
            assertThat(result.getRanking()).isEqualTo(Tier.THREE_OF_A_KIND);
        }

        @Test
        @DisplayName("플레이어 카드가 2장이 아닌 경우 예외")
        void testInvalidPlayerCardCount() {
            List<Card> oneCard = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.NINE)
            );

            assertThatThrownBy(() -> evaluator.evaluate(oneCard, communityCards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정확히 2장");
        }

        @Test
        @DisplayName("총 카드 수가 5장 미만인 경우 예외")
        void testTooFewCards() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK)
            );

            assertThatThrownBy(() -> evaluator.evaluate(playerCards, communityCards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 5장");
        }

        @Test
        @DisplayName("총 카드 수가 7장 초과인 경우 예외")
        void testTooManyCards() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.EIGHT),
                new Card(Suit.SPADES, Rank.SEVEN)
            );

            assertThatThrownBy(() -> evaluator.evaluate(playerCards, communityCards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 7장");
        }

        @Test
        @DisplayName("중복 카드가 있는 경우 예외")
        void testDuplicateCards() {
            List<Card> playerCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING)
            );
            
            List<Card> communityCards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),  // 중복
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.EIGHT)
            );

            assertThatThrownBy(() -> evaluator.evaluate(playerCards, communityCards))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("중복");
        }
    }

    @Nested
    @DisplayName("HandResult 비교 테스트")
    class HandResultComparisonTest {

        @Test
        @DisplayName("로얄 플러시가 스트레이트 플러시보다 강함")
        void testRoyalFlushBeatsStrightFlush() {
            HandResult royalFlush = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN)
            ));

            HandResult straightFlush = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.DIAMONDS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.EIGHT),
                new Card(Suit.DIAMONDS, Rank.SEVEN),
                new Card(Suit.DIAMONDS, Rank.SIX),
                new Card(Suit.DIAMONDS, Rank.FIVE)
            ));

            assertThat(royalFlush.compareTo(straightFlush)).isPositive();
            assertThat(straightFlush.compareTo(royalFlush)).isNegative();
        }

        @Test
        @DisplayName("포카드가 풀하우스보다 강함")
        void testFourOfAKindBeatsFullHouse() {
            HandResult fourOfAKind = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.HEARTS, Rank.SEVEN),
                new Card(Suit.DIAMONDS, Rank.SEVEN),
                new Card(Suit.CLUBS, Rank.SEVEN),
                new Card(Suit.SPADES, Rank.SEVEN),
                new Card(Suit.HEARTS, Rank.ACE)
            ));

            HandResult fullHouse = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.ACE),
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.KING)
            ));

            assertThat(fourOfAKind.compareTo(fullHouse)).isPositive();
        }

        @Test
        @DisplayName("풀하우스가 플러시보다 강함")
        void testFullHouseBeatsFlush() {
            HandResult fullHouse = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.HEARTS, Rank.SIX),
                new Card(Suit.DIAMONDS, Rank.SIX),
                new Card(Suit.CLUBS, Rank.SIX),
                new Card(Suit.SPADES, Rank.TWO),
                new Card(Suit.HEARTS, Rank.TWO)
            ));

            HandResult flush = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.CLUBS, Rank.NINE)
            ));

            assertThat(fullHouse.compareTo(flush)).isPositive();
        }

        @Test
        @DisplayName("높은 원페어가 낮은 원페어보다 강함")
        void testHigherPairBeatsLowerPair() {
            HandResult kingPair = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.SPADES, Rank.QUEEN),
                new Card(Suit.HEARTS, Rank.JACK)
            ));

            HandResult queenPair = evaluator.evaluateFiveCards(Arrays.asList(
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.DIAMONDS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.JACK)
            ));

            assertThat(kingPair.compareTo(queenPair)).isPositive();
        }
    }
}
