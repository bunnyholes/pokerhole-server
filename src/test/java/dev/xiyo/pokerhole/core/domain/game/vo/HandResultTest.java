package dev.xiyo.pokerhole.core.domain.game.vo;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Rank;
import dev.xiyo.pokerhole.core.domain.card.Suit;
import dev.xiyo.pokerhole.core.domain.card.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HandResult 비교 테스트")
class HandResultTest {

    @Nested
    @DisplayName("서로 다른 족보(Tier) 비교")
    class DifferentTierComparison {

        @Test
        @DisplayName("로얄 플러시 > 스트레이트 플러시")
        void royalFlushBeatsStrightFlush() {
            HandResult royalFlush = createHandResult(Tier.ROYAL_FLUSH,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.TEN)
                ));

            HandResult straightFlush = createHandResult(Tier.STRAIGHT_FLUSH,
                Arrays.asList(
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
        @DisplayName("스트레이트 플러시 > 포카드")
        void straightFlushBeatsFourOfAKind() {
            HandResult straightFlush = createHandResult(Tier.STRAIGHT_FLUSH,
                Arrays.asList(
                    new Card(Suit.CLUBS, Rank.SIX),
                    new Card(Suit.CLUBS, Rank.FIVE),
                    new Card(Suit.CLUBS, Rank.FOUR),
                    new Card(Suit.CLUBS, Rank.THREE),
                    new Card(Suit.CLUBS, Rank.TWO)
                ));

            HandResult fourOfAKind = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.KING)
                ));

            assertThat(straightFlush.compareTo(fourOfAKind)).isPositive();
            assertThat(fourOfAKind.compareTo(straightFlush)).isNegative();
        }

        @Test
        @DisplayName("포카드 > 풀하우스")
        void fourOfAKindBeatsFullHouse() {
            HandResult fourOfAKind = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TWO),
                    new Card(Suit.DIAMONDS, Rank.TWO),
                    new Card(Suit.CLUBS, Rank.TWO),
                    new Card(Suit.SPADES, Rank.TWO),
                    new Card(Suit.HEARTS, Rank.THREE)
                ));

            HandResult fullHouse = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.KING)
                ));

            assertThat(fourOfAKind.compareTo(fullHouse)).isPositive();
            assertThat(fullHouse.compareTo(fourOfAKind)).isNegative();
        }

        @Test
        @DisplayName("풀하우스 > 플러시")
        void fullHouseBeatsFlush() {
            HandResult fullHouse = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.FIVE),
                    new Card(Suit.DIAMONDS, Rank.FIVE),
                    new Card(Suit.CLUBS, Rank.FIVE),
                    new Card(Suit.SPADES, Rank.TWO),
                    new Card(Suit.HEARTS, Rank.TWO)
                ));

            HandResult flush = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.SPADES, Rank.ACE),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.SPADES, Rank.NINE)
                ));

            assertThat(fullHouse.compareTo(flush)).isPositive();
            assertThat(flush.compareTo(fullHouse)).isNegative();
        }

        @Test
        @DisplayName("플러시 > 스트레이트")
        void flushBeatsStraight() {
            HandResult flush = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TWO),
                    new Card(Suit.HEARTS, Rank.FOUR),
                    new Card(Suit.HEARTS, Rank.SIX),
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.HEARTS, Rank.TEN)
                ));

            HandResult straight = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.TEN)
                ));

            assertThat(flush.compareTo(straight)).isPositive();
            assertThat(straight.compareTo(flush)).isNegative();
        }

        @Test
        @DisplayName("스트레이트 > 쓰리 카드")
        void straightBeatsThreeOfAKind() {
            HandResult straight = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.FIVE),
                    new Card(Suit.DIAMONDS, Rank.FOUR),
                    new Card(Suit.CLUBS, Rank.THREE),
                    new Card(Suit.SPADES, Rank.TWO),
                    new Card(Suit.HEARTS, Rank.ACE)
                ));

            HandResult threeOfAKind = createHandResult(Tier.THREE_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.QUEEN)
                ));

            assertThat(straight.compareTo(threeOfAKind)).isPositive();
            assertThat(threeOfAKind.compareTo(straight)).isNegative();
        }

        @Test
        @DisplayName("쓰리 카드 > 투 페어")
        void threeOfAKindBeatsTwoPair() {
            HandResult threeOfAKind = createHandResult(Tier.THREE_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TWO),
                    new Card(Suit.DIAMONDS, Rank.TWO),
                    new Card(Suit.CLUBS, Rank.TWO),
                    new Card(Suit.SPADES, Rank.THREE),
                    new Card(Suit.HEARTS, Rank.FOUR)
                ));

            HandResult twoPair = createHandResult(Tier.TWO_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.QUEEN)
                ));

            assertThat(threeOfAKind.compareTo(twoPair)).isPositive();
            assertThat(twoPair.compareTo(threeOfAKind)).isNegative();
        }

        @Test
        @DisplayName("투 페어 > 원 페어")
        void twoPairBeatsOnePair() {
            HandResult twoPair = createHandResult(Tier.TWO_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.THREE),
                    new Card(Suit.DIAMONDS, Rank.THREE),
                    new Card(Suit.CLUBS, Rank.TWO),
                    new Card(Suit.SPADES, Rank.TWO),
                    new Card(Suit.HEARTS, Rank.FOUR)
                ));

            HandResult onePair = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            assertThat(twoPair.compareTo(onePair)).isPositive();
            assertThat(onePair.compareTo(twoPair)).isNegative();
        }

        @Test
        @DisplayName("원 페어 > 하이 카드")
        void onePairBeatsHighCard() {
            HandResult onePair = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TWO),
                    new Card(Suit.DIAMONDS, Rank.TWO),
                    new Card(Suit.CLUBS, Rank.THREE),
                    new Card(Suit.SPADES, Rank.FOUR),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult highCard = createHandResult(Tier.HIGH_CARD,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.NINE)
                ));

            assertThat(onePair.compareTo(highCard)).isPositive();
            assertThat(highCard.compareTo(onePair)).isNegative();
        }
    }

    @Nested
    @DisplayName("같은 족보, 다른 카드 랭크 비교")
    class SameTierDifferentRanks {

        @Test
        @DisplayName("포카드: Ace 포카드 > King 포카드")
        void fourOfAKind_AcesBeatsKings() {
            HandResult acesFour = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.TWO)
                ));

            HandResult kingsFour = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.ACE)
                ));

            assertThat(acesFour.compareTo(kingsFour)).isPositive();
            assertThat(kingsFour.compareTo(acesFour)).isNegative();
        }

        @Test
        @DisplayName("풀하우스: Queens full of Kings > Queens full of Jacks")
        void fullHouse_QueensOverKingsBeatsQueensOverJacks() {
            HandResult queensOverKings = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.DIAMONDS, Rank.QUEEN),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.KING)
                ));

            HandResult queensOverJacks = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.DIAMONDS, Rank.QUEEN),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            assertThat(queensOverKings.compareTo(queensOverJacks)).isPositive();
            assertThat(queensOverJacks.compareTo(queensOverKings)).isNegative();
        }

        @Test
        @DisplayName("플러시: Ace high flush > King high flush")
        void flush_AceHighBeatsKingHigh() {
            HandResult aceHighFlush = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.NINE),
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult kingHighFlush = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.SPADES, Rank.TEN),
                    new Card(Suit.SPADES, Rank.NINE)
                ));

            assertThat(aceHighFlush.compareTo(kingHighFlush)).isPositive();
            assertThat(kingHighFlush.compareTo(aceHighFlush)).isNegative();
        }

        @Test
        @DisplayName("스트레이트: 9-high straight > 8-high straight")
        void straight_NineHighBeatsEightHigh() {
            HandResult nineHighStraight = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.NINE),
                    new Card(Suit.DIAMONDS, Rank.EIGHT),
                    new Card(Suit.CLUBS, Rank.SEVEN),
                    new Card(Suit.SPADES, Rank.SIX),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult eightHighStraight = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.SEVEN),
                    new Card(Suit.CLUBS, Rank.SIX),
                    new Card(Suit.SPADES, Rank.FIVE),
                    new Card(Suit.HEARTS, Rank.FOUR)
                ));

            assertThat(nineHighStraight.compareTo(eightHighStraight)).isPositive();
            assertThat(eightHighStraight.compareTo(nineHighStraight)).isNegative();
        }

        @Test
        @DisplayName("쓰리 카드: Aces > Kings")
        void threeOfAKind_AcesBeatsKings() {
            HandResult acesTrips = createHandResult(Tier.THREE_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.QUEEN)
                ));

            HandResult kingsTrips = createHandResult(Tier.THREE_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.QUEEN)
                ));

            assertThat(acesTrips.compareTo(kingsTrips)).isPositive();
            assertThat(kingsTrips.compareTo(acesTrips)).isNegative();
        }

        @Test
        @DisplayName("투 페어: Aces and Kings > Aces and Queens")
        void twoPair_AcesAndKingsBeatsAcesAndQueens() {
            HandResult acesAndKings = createHandResult(Tier.TWO_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            HandResult acesAndQueens = createHandResult(Tier.TWO_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.KING)
                ));

            assertThat(acesAndKings.compareTo(acesAndQueens)).isPositive();
            assertThat(acesAndQueens.compareTo(acesAndKings)).isNegative();
        }

        @Test
        @DisplayName("원 페어: Pair of Aces > Pair of Kings")
        void onePair_AcesBeatsKings() {
            HandResult pairOfAces = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            HandResult pairOfKings = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            assertThat(pairOfAces.compareTo(pairOfKings)).isPositive();
            assertThat(pairOfKings.compareTo(pairOfAces)).isNegative();
        }

        @Test
        @DisplayName("하이 카드: Ace high > King high")
        void highCard_AceHighBeatsKingHigh() {
            HandResult aceHigh = createHandResult(Tier.HIGH_CARD,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.JACK),
                    new Card(Suit.CLUBS, Rank.NINE),
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult kingHigh = createHandResult(Tier.HIGH_CARD,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.DIAMONDS, Rank.QUEEN),
                    new Card(Suit.CLUBS, Rank.JACK),
                    new Card(Suit.SPADES, Rank.TEN),
                    new Card(Suit.HEARTS, Rank.NINE)
                ));

            assertThat(aceHigh.compareTo(kingHigh)).isPositive();
            assertThat(kingHigh.compareTo(aceHigh)).isNegative();
        }
    }

    @Nested
    @DisplayName("완전 동점 (Tie) 테스트")
    class TieTests {

        @Test
        @DisplayName("포카드: 같은 랭크, 같은 키커 = 동점")
        void fourOfAKind_SameRankSameKicker_Tie() {
            HandResult hand1 = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.DIAMONDS, Rank.QUEEN),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            HandResult hand2 = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.DIAMONDS, Rank.QUEEN),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.DIAMONDS, Rank.JACK)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
            assertThat(hand2.compareTo(hand1)).isZero();
        }

        @Test
        @DisplayName("풀하우스: 같은 쓰리 카드, 같은 페어 = 동점")
        void fullHouse_SameTripsAndPair_Tie() {
            HandResult hand1 = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.EIGHT),
                    new Card(Suit.CLUBS, Rank.EIGHT),
                    new Card(Suit.SPADES, Rank.FIVE),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult hand2 = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.EIGHT),
                    new Card(Suit.SPADES, Rank.EIGHT),
                    new Card(Suit.CLUBS, Rank.FIVE),
                    new Card(Suit.DIAMONDS, Rank.FIVE)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
            assertThat(hand2.compareTo(hand1)).isZero();
        }

        @Test
        @DisplayName("플러시: 모든 카드 랭크 동일 = 동점")
        void flush_AllSameRanks_Tie() {
            HandResult hand1 = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.HEARTS, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.NINE),
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult hand2 = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.SPADES, Rank.NINE),
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.SPADES, Rank.FIVE)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
            assertThat(hand2.compareTo(hand1)).isZero();
        }

        @Test
        @DisplayName("스트레이트: 같은 하이 카드 = 동점")
        void straight_SameHighCard_Tie() {
            HandResult hand1 = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TEN),
                    new Card(Suit.DIAMONDS, Rank.NINE),
                    new Card(Suit.CLUBS, Rank.EIGHT),
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.SIX)
                ));

            HandResult hand2 = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.CLUBS, Rank.TEN),
                    new Card(Suit.SPADES, Rank.NINE),
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.SEVEN),
                    new Card(Suit.CLUBS, Rank.SIX)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
            assertThat(hand2.compareTo(hand1)).isZero();
        }

        @Test
        @DisplayName("투 페어: 같은 두 페어, 같은 키커 = 동점")
        void twoPair_SamePairsAndKicker_Tie() {
            HandResult hand1 = createHandResult(Tier.TWO_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.JACK),
                    new Card(Suit.DIAMONDS, Rank.JACK),
                    new Card(Suit.CLUBS, Rank.SEVEN),
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.FOUR)
                ));

            HandResult hand2 = createHandResult(Tier.TWO_PAIR,
                Arrays.asList(
                    new Card(Suit.CLUBS, Rank.JACK),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.DIAMONDS, Rank.SEVEN),
                    new Card(Suit.CLUBS, Rank.FOUR)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
            assertThat(hand2.compareTo(hand1)).isZero();
        }

        @Test
        @DisplayName("하이 카드: 모든 카드 랭크 동일 = 동점")
        void highCard_AllSameRanks_Tie() {
            HandResult hand1 = createHandResult(Tier.HIGH_CARD,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.DIAMONDS, Rank.TEN),
                    new Card(Suit.CLUBS, Rank.EIGHT),
                    new Card(Suit.SPADES, Rank.SIX),
                    new Card(Suit.HEARTS, Rank.FOUR)
                ));

            HandResult hand2 = createHandResult(Tier.HIGH_CARD,
                Arrays.asList(
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.TEN),
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.SIX),
                    new Card(Suit.CLUBS, Rank.FOUR)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
            assertThat(hand2.compareTo(hand1)).isZero();
        }
    }

    @Nested
    @DisplayName("키커(Kicker) 비교 테스트")
    class KickerTests {

        @Test
        @DisplayName("포카드: 같은 포카드, 다른 키커 - 높은 키커가 이김")
        void fourOfAKind_SameQuads_HigherKickerWins() {
            HandResult handWithAceKicker = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.DIAMONDS, Rank.SEVEN),
                    new Card(Suit.CLUBS, Rank.SEVEN),
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.ACE)
                ));

            HandResult handWithKingKicker = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.DIAMONDS, Rank.SEVEN),
                    new Card(Suit.CLUBS, Rank.SEVEN),
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.KING)
                ));

            assertThat(handWithAceKicker.compareTo(handWithKingKicker)).isPositive();
            assertThat(handWithKingKicker.compareTo(handWithAceKicker)).isNegative();
        }

        @Test
        @DisplayName("원 페어: 같은 페어, 다른 키커 - 첫 번째 키커로 비교")
        void onePair_SamePair_CompareFirstKicker() {
            HandResult handWithAceKicker = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TEN),
                    new Card(Suit.DIAMONDS, Rank.TEN),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.NINE),
                    new Card(Suit.HEARTS, Rank.EIGHT)
                ));

            HandResult handWithKingKicker = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.TEN),
                    new Card(Suit.DIAMONDS, Rank.TEN),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.QUEEN)
                ));

            assertThat(handWithAceKicker.compareTo(handWithKingKicker)).isPositive();
            assertThat(handWithKingKicker.compareTo(handWithAceKicker)).isNegative();
        }

        @Test
        @DisplayName("원 페어: 같은 페어, 첫 키커 동일, 두 번째 키커로 비교")
        void onePair_SamePairAndFirstKicker_CompareSecondKicker() {
            HandResult handWithQueenSecondKicker = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.NINE),
                    new Card(Suit.DIAMONDS, Rank.NINE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.SEVEN)
                ));

            HandResult handWithJackSecondKicker = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.NINE),
                    new Card(Suit.DIAMONDS, Rank.NINE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.TEN)
                ));

            assertThat(handWithQueenSecondKicker.compareTo(handWithJackSecondKicker)).isPositive();
            assertThat(handWithJackSecondKicker.compareTo(handWithQueenSecondKicker)).isNegative();
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("생성자: null 족보는 예외 발생")
        void constructor_NullTier_ThrowsException() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN)
            );

            assertThatThrownBy(() -> new HandResult(null, cards, Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ranking cannot be null");
        }

        @Test
        @DisplayName("생성자: null 카드 리스트는 예외 발생")
        void constructor_NullCards_ThrowsException() {
            assertThatThrownBy(() -> new HandResult(Tier.HIGH_CARD, null, Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cards cannot be null or empty");
        }

        @Test
        @DisplayName("생성자: 빈 카드 리스트는 예외 발생")
        void constructor_EmptyCards_ThrowsException() {
            assertThatThrownBy(() -> new HandResult(Tier.HIGH_CARD, Collections.emptyList(), Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cards cannot be null or empty");
        }

        @Test
        @DisplayName("키커가 null인 경우 빈 리스트로 처리")
        void constructor_NullKickers_TreatedAsEmptyList() {
            List<Card> cards = Arrays.asList(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN)
            );

            HandResult result = new HandResult(Tier.ROYAL_FLUSH, cards, null);

            assertThat(result.getKickers()).isNotNull();
            assertThat(result.getKickers()).isEmpty();
        }

        @Test
        @DisplayName("빈 키커 리스트와 비교 - 정상 동작")
        void compare_EmptyKickers_NoException() {
            HandResult hand1 = createHandResult(Tier.STRAIGHT_FLUSH,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.NINE),
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.SIX),
                    new Card(Suit.HEARTS, Rank.FIVE)
                ));

            HandResult hand2 = createHandResult(Tier.STRAIGHT_FLUSH,
                Arrays.asList(
                    new Card(Suit.DIAMONDS, Rank.NINE),
                    new Card(Suit.DIAMONDS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.SEVEN),
                    new Card(Suit.DIAMONDS, Rank.SIX),
                    new Card(Suit.DIAMONDS, Rank.FIVE)
                ));

            assertThat(hand1.compareTo(hand2)).isZero();
        }

        @Test
        @DisplayName("compareTo는 대칭적이어야 함: a.compareTo(b) = -b.compareTo(a)")
        void compareTo_Symmetry() {
            HandResult stronger = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.HEARTS, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.NINE)
                ));

            HandResult weaker = createHandResult(Tier.STRAIGHT,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.HEARTS, Rank.TEN)
                ));

            int forward = stronger.compareTo(weaker);
            int backward = weaker.compareTo(stronger);

            assertThat(forward).isPositive();
            assertThat(backward).isNegative();
            assertThat(forward).isEqualTo(-backward);
        }

        @Test
        @DisplayName("compareTo는 추이적이어야 함: a > b, b > c => a > c")
        void compareTo_Transitivity() {
            HandResult strongest = createHandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.ACE),
                    new Card(Suit.DIAMONDS, Rank.ACE),
                    new Card(Suit.CLUBS, Rank.ACE),
                    new Card(Suit.SPADES, Rank.ACE),
                    new Card(Suit.HEARTS, Rank.KING)
                ));

            HandResult medium = createHandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.KING),
                    new Card(Suit.DIAMONDS, Rank.KING),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.QUEEN)
                ));

            HandResult weakest = createHandResult(Tier.FLUSH,
                Arrays.asList(
                    new Card(Suit.SPADES, Rank.ACE),
                    new Card(Suit.SPADES, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.SPADES, Rank.JACK),
                    new Card(Suit.SPADES, Rank.NINE)
                ));

            assertThat(strongest.compareTo(medium)).isPositive();
            assertThat(medium.compareTo(weakest)).isPositive();
            assertThat(strongest.compareTo(weakest)).isPositive();
        }

        @Test
        @DisplayName("자기 자신과 비교 시 0 반환")
        void compareTo_SelfComparison_ReturnsZero() {
            HandResult hand = createHandResult(Tier.ONE_PAIR,
                Arrays.asList(
                    new Card(Suit.HEARTS, Rank.EIGHT),
                    new Card(Suit.DIAMONDS, Rank.EIGHT),
                    new Card(Suit.CLUBS, Rank.KING),
                    new Card(Suit.SPADES, Rank.QUEEN),
                    new Card(Suit.HEARTS, Rank.JACK)
                ));

            assertThat(hand.compareTo(hand)).isZero();
        }
    }

    // Helper method to create HandResult with empty kickers
    private HandResult createHandResult(Tier tier, List<Card> cards) {
        return new HandResult(tier, cards, Collections.emptyList());
    }
}
