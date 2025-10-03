package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Rank;
import dev.xiyo.pokerhole.core.domain.card.Suit;
import dev.xiyo.pokerhole.core.domain.card.Tier;
import dev.xiyo.pokerhole.core.domain.game.vo.HandResult;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class WinnerResolverTest {

    private WinnerResolver resolver;
    private PlayerId playerA;
    private PlayerId playerB;
    private PlayerId playerC;
    private PlayerId playerD;

    @BeforeEach
    void setUp() {
        resolver = new WinnerResolver();
        playerA = PlayerId.generate();
        playerB = PlayerId.generate();
        playerC = PlayerId.generate();
        playerD = PlayerId.generate();
    }

    // ===== Single Winner Tests =====

    @Test
    @DisplayName("단독 승자: Player A (Royal Flush) > Player B (Straight Flush)")
    void singleWinner_RoyalFlushVsStraightFlush() {
        // Given: Player A has Royal Flush, Player B has Straight Flush
        HandResult royalFlush = new HandResult(
                Tier.ROYAL_FLUSH,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.HEARTS, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.TEN)
                ),
                Collections.emptyList()
        );

        HandResult straightFlush = new HandResult(
                Tier.STRAIGHT_FLUSH,
                Arrays.asList(
                        new Card(Suit.SPADES, Rank.NINE),
                        new Card(Suit.SPADES, Rank.EIGHT),
                        new Card(Suit.SPADES, Rank.SEVEN),
                        new Card(Suit.SPADES, Rank.SIX),
                        new Card(Suit.SPADES, Rank.FIVE)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, royalFlush);
        hands.put(playerB, straightFlush);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(1);
        assertThat(winners).containsExactly(playerA);
        assertThat(resolver.isTie(hands)).isFalse();
    }

    @Test
    @DisplayName("단독 승자: Player A (Four of a Kind) > Player B (Full House)")
    void singleWinner_FourOfAKindVsFullHouse() {
        // Given
        HandResult fourOfAKind = new HandResult(
                Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.ACE),
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.SPADES, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING)
                ),
                Collections.emptyList()
        );

        HandResult fullHouse = new HandResult(
                Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.SPADES, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.QUEEN)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, fourOfAKind);
        hands.put(playerB, fullHouse);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(1);
        assertThat(winners).containsExactly(playerA);
    }

    @Test
    @DisplayName("단독 승자: 같은 족보, 다른 카드 (Ace High Flush > King High Flush)")
    void singleWinner_SameTier_DifferentCards() {
        // Given: Both have Flush, but different high cards
        HandResult aceHighFlush = new HandResult(
                Tier.FLUSH,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.NINE),
                        new Card(Suit.HEARTS, Rank.SEVEN),
                        new Card(Suit.HEARTS, Rank.FIVE)
                ),
                Collections.emptyList()
        );

        HandResult kingHighFlush = new HandResult(
                Tier.FLUSH,
                Arrays.asList(
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.QUEEN),
                        new Card(Suit.DIAMONDS, Rank.TEN),
                        new Card(Suit.DIAMONDS, Rank.EIGHT),
                        new Card(Suit.DIAMONDS, Rank.SIX)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, aceHighFlush);
        hands.put(playerB, kingHighFlush);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(1);
        assertThat(winners).containsExactly(playerA);
    }

    // ===== Two-Way Tie Tests =====

    @Test
    @DisplayName("2-way tie: Player A = Player B (동일한 Ace High Flush)")
    void twoWayTie_IdenticalFlush() {
        // Given: Both have identical Ace High Flush (different suits)
        HandResult flushA = new HandResult(
                Tier.FLUSH,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.NINE),
                        new Card(Suit.HEARTS, Rank.SEVEN),
                        new Card(Suit.HEARTS, Rank.FIVE)
                ),
                Collections.emptyList()
        );

        HandResult flushB = new HandResult(
                Tier.FLUSH,
                Arrays.asList(
                        new Card(Suit.DIAMONDS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.JACK),
                        new Card(Suit.DIAMONDS, Rank.NINE),
                        new Card(Suit.DIAMONDS, Rank.SEVEN),
                        new Card(Suit.DIAMONDS, Rank.FIVE)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, flushA);
        hands.put(playerB, flushB);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(2);
        assertThat(winners).containsExactlyInAnyOrder(playerA, playerB);
        assertThat(resolver.isTie(hands)).isTrue();
    }

    @Test
    @DisplayName("2-way tie: Player A = Player B (동일한 Full House)")
    void twoWayTie_IdenticalFullHouse() {
        // Given: Both have identical Full House (Kings over Queens)
        HandResult fullHouseA = new HandResult(
                Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.SPADES, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.QUEEN)
                ),
                Collections.emptyList()
        );

        HandResult fullHouseB = new HandResult(
                Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.QUEEN),
                        new Card(Suit.CLUBS, Rank.QUEEN)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, fullHouseA);
        hands.put(playerB, fullHouseB);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(2);
        assertThat(winners).containsExactlyInAnyOrder(playerA, playerB);
        assertThat(resolver.isTie(hands)).isTrue();
    }

    // ===== Three-Way Tie Tests =====

    @Test
    @DisplayName("3-way tie: 세 명의 플레이어가 모두 동일한 Straight를 가짐")
    void threeWayTie_IdenticalStraight() {
        // Given: All three have identical Ace-high straight
        HandResult straightA = new HandResult(
                Tier.STRAIGHT,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.QUEEN),
                        new Card(Suit.SPADES, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.TEN)
                ),
                Collections.emptyList()
        );

        HandResult straightB = new HandResult(
                Tier.STRAIGHT,
                Arrays.asList(
                        new Card(Suit.SPADES, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.QUEEN),
                        new Card(Suit.CLUBS, Rank.JACK),
                        new Card(Suit.SPADES, Rank.TEN)
                ),
                Collections.emptyList()
        );

        HandResult straightC = new HandResult(
                Tier.STRAIGHT,
                Arrays.asList(
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.HEARTS, Rank.QUEEN),
                        new Card(Suit.DIAMONDS, Rank.JACK),
                        new Card(Suit.CLUBS, Rank.TEN)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, straightA);
        hands.put(playerB, straightB);
        hands.put(playerC, straightC);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(3);
        assertThat(winners).containsExactlyInAnyOrder(playerA, playerB, playerC);
        assertThat(resolver.isTie(hands)).isTrue();
    }

    // ===== Mixed Scenario Tests =====

    @Test
    @DisplayName("혼합: Player A & B tie (동점), Player C는 패배")
    void mixedScenario_TwoWinners_OneLose() {
        // Given: A and B have identical Three of a Kind (Aces), C has Two Pair
        HandResult threeAcesA = new HandResult(
                Tier.THREE_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.ACE),
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.HEARTS, Rank.QUEEN)
                ),
                Collections.emptyList()
        );

        HandResult threeAcesB = new HandResult(
                Tier.THREE_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.SPADES, Rank.ACE),
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.QUEEN)
                ),
                Collections.emptyList()
        );

        HandResult twoPairC = new HandResult(
                Tier.TWO_PAIR,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.QUEEN),
                        new Card(Suit.SPADES, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.JACK)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, threeAcesA);
        hands.put(playerB, threeAcesB);
        hands.put(playerC, twoPairC);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(2);
        assertThat(winners).containsExactlyInAnyOrder(playerA, playerB);
        assertThat(winners).doesNotContain(playerC);
        assertThat(resolver.isTie(hands)).isTrue();
    }

    @Test
    @DisplayName("4명 플레이: Player A 단독 승리, B & C 동점 2위, D 4위")
    void fourPlayers_OneClear_TwoTied_OneLast() {
        // Given
        HandResult fourOfAKind = new HandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.ACE),
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.SPADES, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING)
                ), Collections.emptyList());

        HandResult fullHouseB = new HandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.SPADES, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.QUEEN)
                ), Collections.emptyList());

        HandResult fullHouseC = new HandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.QUEEN),
                        new Card(Suit.CLUBS, Rank.QUEEN)
                ), Collections.emptyList());

        HandResult flushD = new HandResult(Tier.FLUSH,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.NINE),
                        new Card(Suit.HEARTS, Rank.SEVEN),
                        new Card(Suit.HEARTS, Rank.FIVE)
                ), Collections.emptyList());

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, fourOfAKind);
        hands.put(playerB, fullHouseB);
        hands.put(playerC, fullHouseC);
        hands.put(playerD, flushD);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);
        Map<PlayerId, Integer> rankings = resolver.getRankings(hands);

        // Then
        assertThat(winners).hasSize(1);
        assertThat(winners).containsExactly(playerA);

        // Check rankings
        assertThat(rankings.get(playerA)).isEqualTo(1);
        assertThat(rankings.get(playerB)).isEqualTo(2);
        assertThat(rankings.get(playerC)).isEqualTo(2);
        assertThat(rankings.get(playerD)).isEqualTo(4);
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Edge case: 빈 맵 입력 시 예외 발생")
    void edgeCase_EmptyMap_ThrowsException() {
        // Given
        Map<PlayerId, HandResult> emptyHands = new HashMap<>();

        // When & Then
        assertThatThrownBy(() -> resolver.findWinners(emptyHands))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("Edge case: null 입력 시 예외 발생")
    void edgeCase_NullMap_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> resolver.findWinners(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("Edge case: 단일 플레이어 (자동 승리)")
    void edgeCase_SinglePlayer_AutoWin() {
        // Given
        HandResult onePair = new HandResult(
                Tier.ONE_PAIR,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.FIVE),
                        new Card(Suit.DIAMONDS, Rank.FIVE),
                        new Card(Suit.CLUBS, Rank.THREE),
                        new Card(Suit.SPADES, Rank.TWO),
                        new Card(Suit.HEARTS, Rank.ACE)
                ),
                Collections.emptyList()
        );

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, onePair);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);

        // Then
        assertThat(winners).hasSize(1);
        assertThat(winners).containsExactly(playerA);
        assertThat(resolver.isTie(hands)).isFalse();
    }

    @Test
    @DisplayName("Edge case: 모든 플레이어가 서로 다른 핸드")
    void edgeCase_AllDifferentHands() {
        // Given
        HandResult royalFlush = new HandResult(Tier.ROYAL_FLUSH,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.HEARTS, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.TEN)
                ), Collections.emptyList());

        HandResult fourOfAKind = new HandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.HEARTS, Rank.ACE)
                ), Collections.emptyList());

        HandResult fullHouse = new HandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.QUEEN),
                        new Card(Suit.DIAMONDS, Rank.QUEEN),
                        new Card(Suit.CLUBS, Rank.QUEEN),
                        new Card(Suit.SPADES, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.JACK)
                ), Collections.emptyList());

        HandResult highCard = new HandResult(Tier.HIGH_CARD,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.QUEEN),
                        new Card(Suit.SPADES, Rank.NINE),
                        new Card(Suit.HEARTS, Rank.SEVEN)
                ), Collections.emptyList());

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, royalFlush);
        hands.put(playerB, fourOfAKind);
        hands.put(playerC, fullHouse);
        hands.put(playerD, highCard);

        // When
        List<PlayerId> winners = resolver.findWinners(hands);
        Map<PlayerId, Integer> rankings = resolver.getRankings(hands);

        // Then
        assertThat(winners).hasSize(1);
        assertThat(winners).containsExactly(playerA);
        assertThat(resolver.isTie(hands)).isFalse();

        // Rankings should be 1, 2, 3, 4
        assertThat(rankings.get(playerA)).isEqualTo(1);
        assertThat(rankings.get(playerB)).isEqualTo(2);
        assertThat(rankings.get(playerC)).isEqualTo(3);
        assertThat(rankings.get(playerD)).isEqualTo(4);
    }

    // ===== Utility Method Tests =====

    @Test
    @DisplayName("isWinner: 승자 확인 메서드 테스트")
    void isWinner_ReturnsCorrectResult() {
        // Given
        HandResult fourOfAKind = new HandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.DIAMONDS, Rank.ACE),
                        new Card(Suit.CLUBS, Rank.ACE),
                        new Card(Suit.SPADES, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING)
                ), Collections.emptyList());

        HandResult fullHouse = new HandResult(Tier.FULL_HOUSE,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.SPADES, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.QUEEN)
                ), Collections.emptyList());

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, fourOfAKind);
        hands.put(playerB, fullHouse);

        // When & Then
        assertThat(resolver.isWinner(playerA, hands)).isTrue();
        assertThat(resolver.isWinner(playerB, hands)).isFalse();
    }

    @Test
    @DisplayName("getRankings: 정확한 순위 계산")
    void getRankings_CalculatesCorrectRankings() {
        // Given: A=1st, B&C=2nd (tie), D=4th
        HandResult bestHand = new HandResult(Tier.ROYAL_FLUSH,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.ACE),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.HEARTS, Rank.QUEEN),
                        new Card(Suit.HEARTS, Rank.JACK),
                        new Card(Suit.HEARTS, Rank.TEN)
                ), Collections.emptyList());

        HandResult secondHandB = new HandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.HEARTS, Rank.ACE)
                ), Collections.emptyList());

        HandResult secondHandC = new HandResult(Tier.FOUR_OF_A_KIND,
                Arrays.asList(
                        new Card(Suit.SPADES, Rank.KING),
                        new Card(Suit.CLUBS, Rank.KING),
                        new Card(Suit.HEARTS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.KING),
                        new Card(Suit.DIAMONDS, Rank.ACE)
                ), Collections.emptyList());

        HandResult lastHand = new HandResult(Tier.HIGH_CARD,
                Arrays.asList(
                        new Card(Suit.HEARTS, Rank.NINE),
                        new Card(Suit.DIAMONDS, Rank.SEVEN),
                        new Card(Suit.CLUBS, Rank.FIVE),
                        new Card(Suit.SPADES, Rank.THREE),
                        new Card(Suit.HEARTS, Rank.TWO)
                ), Collections.emptyList());

        Map<PlayerId, HandResult> hands = new HashMap<>();
        hands.put(playerA, bestHand);
        hands.put(playerB, secondHandB);
        hands.put(playerC, secondHandC);
        hands.put(playerD, lastHand);

        // When
        Map<PlayerId, Integer> rankings = resolver.getRankings(hands);

        // Then
        assertThat(rankings.get(playerA)).isEqualTo(1);
        assertThat(rankings.get(playerB)).isEqualTo(2);
        assertThat(rankings.get(playerC)).isEqualTo(2);
        assertThat(rankings.get(playerD)).isEqualTo(4); // 2nd place has 2 players, so next is 4th
    }
}
