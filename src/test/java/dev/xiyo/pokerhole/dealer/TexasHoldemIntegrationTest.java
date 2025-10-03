package dev.xiyo.pokerhole.dealer;

import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.game.vo.BettingRound;
import dev.xiyo.pokerhole.core.domain.game.vo.PlayerAction;
import dev.xiyo.pokerhole.core.domain.player.Player;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerStatus;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Texas Hold'em 통합 테스트")
class TexasHoldemIntegrationTest {

    private Dealer dealer;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        dealer = Dealer.newDealer();
        // 기존 통합 테스트에서는 블라인드 없이 시작 (블라인드 테스트는 별도)
        dealer.setSmallBlind(0);
        dealer.setBigBlind(0);

        player1 = Player.newPlayer("Alice");
        player2 = Player.newPlayer("Bob");

        dealer.enrollPlayer(player1);
        dealer.enrollPlayer(player2);
    }

    @AfterEach
    void tearDown() {
        // 닉네임 해제 (다음 테스트를 위해)
        player1.releaseNickname();
        player2.releaseNickname();
    }

    @Nested
    @DisplayName("게임 시작 테스트")
    class GameStartTest {

        @Test
        @DisplayName("startTexasHoldem()은 각 플레이어에게 홀카드 2장을 배분한다")
        void startTexasHoldem_ShouldDealTwoCardsToEachPlayer() {
            // when
            dealer.startTexasHoldem();

            // then
            assertThat((Iterable<Card>) player1.getHand()).hasSize(2);
            assertThat((Iterable<Card>) player2.getHand()).hasSize(2);

            // 0번 플레이어(player1)의 패 출력
            System.out.println("=== Player 0 (Alice)의 패 ===");
            for (Card card : player1.getHand()) {
                System.out.println(card);
            }
        }

        @Test
        @DisplayName("startTexasHoldem()은 PRE_FLOP 라운드로 시작한다")
        void startTexasHoldem_ShouldStartWithPreFlop() {
            // when
            dealer.startTexasHoldem();

            // then
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.PRE_FLOP);
            assertThat(dealer.getCommunityCards()).isEmpty();
        }

        @Test
        @DisplayName("startTexasHoldem()은 모든 플레이어를 ACTIVE 상태로 설정한다")
        void startTexasHoldem_ShouldSetAllPlayersActive() {
            // when
            dealer.startTexasHoldem();

            // then
            assertThat(player1.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
            assertThat(player2.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("플레이어 액션 테스트")
    class PlayerActionTest {

        @BeforeEach
        void startGame() {
            dealer.startTexasHoldem();
        }

        @Test
        @DisplayName("CHECK - 현재 베팅이 0일 때 CHECK 가능")
        void check_ShouldWorkWhenNoBet() {
            // given
            Player currentPlayer = dealer.getCurrentPlayer();

            // when & then
            assertThatCode(() ->
                dealer.processPlayerAction(currentPlayer, PlayerAction.CHECK, 0)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("BET - 현재 베팅이 0일 때 BET 가능")
        void bet_ShouldWorkWhenNoBet() {
            // given
            Player currentPlayer = dealer.getCurrentPlayer();

            // when
            dealer.processPlayerAction(currentPlayer, PlayerAction.BET, 100);

            // then
            assertThat(dealer.getCurrentBet()).isEqualTo(100);
            assertThat(dealer.getPot()).isEqualTo(100);
        }

        @Test
        @DisplayName("CALL - 현재 베팅에 맞춰서 CALL 가능")
        void call_ShouldMatchCurrentBet() {
            // given
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();

            // when
            dealer.processPlayerAction(secondPlayer, PlayerAction.CALL, 0);

            // then
            assertThat(dealer.getPot()).isEqualTo(200);
        }

        @Test
        @DisplayName("RAISE - 현재 베팅보다 높게 RAISE 가능")
        void raise_ShouldIncreaseBet() {
            // given
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();

            // when
            dealer.processPlayerAction(secondPlayer, PlayerAction.RAISE, 200);

            // then
            assertThat(dealer.getCurrentBet()).isEqualTo(200);
            assertThat(dealer.getPot()).isEqualTo(300);
        }

        @Test
        @DisplayName("FOLD - 플레이어가 FOLD하면 상태가 FOLDED로 변경")
        void fold_ShouldChangeStatusToFolded() {
            // given
            Player currentPlayer = dealer.getCurrentPlayer();

            // when
            dealer.processPlayerAction(currentPlayer, PlayerAction.FOLD, 0);

            // then
            assertThat(currentPlayer.getStatus()).isEqualTo(PlayerStatus.FOLDED);
        }

        @Test
        @DisplayName("ALL_IN - 모든 칩을 베팅하면 상태가 ALL_IN으로 변경")
        void allIn_ShouldChangeStatusToAllIn() {
            // given
            Player currentPlayer = dealer.getCurrentPlayer();
            int allInAmount = currentPlayer.getChips();

            // when
            dealer.processPlayerAction(currentPlayer, PlayerAction.ALL_IN, 0);

            // then
            assertThat(currentPlayer.getStatus()).isEqualTo(PlayerStatus.ALL_IN);
            assertThat(currentPlayer.getChips()).isEqualTo(0);
            assertThat(dealer.getPot()).isEqualTo(allInAmount);
        }
    }

    @Nested
    @DisplayName("라운드 진행 테스트")
    class RoundProgressionTest {

        @BeforeEach
        void startGame() {
            dealer.startTexasHoldem();
        }

        @Test
        @DisplayName("PRE_FLOP에서 양쪽 CHECK하면 FLOP으로 진행, 커뮤니티 카드 3장 공개")
        void progression_PreFlopToFlop_ShouldRevealThreeCards() {
            // when - 두 플레이어 모두 CHECK
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);

            // then
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.FLOP);
            assertThat(dealer.getCommunityCards()).hasSize(3);
        }

        @Test
        @DisplayName("FLOP → TURN → RIVER → SHOWDOWN 순서로 진행")
        void progression_AllRoundsInOrder() {
            // PRE_FLOP → FLOP
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.FLOP);
            assertThat(dealer.getCommunityCards()).hasSize(3);

            // FLOP → TURN
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.TURN);
            assertThat(dealer.getCommunityCards()).hasSize(4);

            // TURN → RIVER
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.RIVER);
            assertThat(dealer.getCommunityCards()).hasSize(5);

            // RIVER → SHOWDOWN
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            dealer.processPlayerAction(dealer.getCurrentPlayer(), PlayerAction.CHECK, 0);
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.SHOWDOWN);
        }
    }

    @Nested
    @DisplayName("2인 게임 시나리오 테스트")
    class TwoPlayerScenarioTest {

        @Test
        @DisplayName("시나리오: 한 플레이어가 BET, 다른 플레이어가 CALL 후 라운드 진행")
        void scenario_BetAndCall() {
            // given
            dealer.startTexasHoldem();

            // when - PRE_FLOP: First player BET 100, Second player CALL
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(secondPlayer, PlayerAction.CALL, 0);

            // then - FLOP으로 진행
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.FLOP);
            assertThat(dealer.getPot()).isEqualTo(200);
            assertThat(dealer.getCommunityCards()).hasSize(3);
        }

        @Test
        @DisplayName("시나리오: 한 플레이어가 FOLD하면 다른 플레이어가 팟 획득")
        void scenario_FoldEndsGame() {
            // given
            dealer.startTexasHoldem();

            // when - First player BET 100, Second player FOLD
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(secondPlayer, PlayerAction.FOLD, 0);

            // then - 두 번째 플레이어가 FOLD
            assertThat(secondPlayer.getStatus()).isEqualTo(PlayerStatus.FOLDED);
            // 베팅 라운드 완료로 진행됨
        }

        @Test
        @DisplayName("시나리오: RAISE 후 CALL, 라운드 진행")
        void scenario_RaiseAndCall() {
            // given
            dealer.startTexasHoldem();

            // when - PRE_FLOP: First player BET 100, Second player RAISE 200, First player CALL
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(secondPlayer, PlayerAction.RAISE, 200);

            // First player's turn again
            Player callPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(callPlayer, PlayerAction.CALL, 0);

            // then - FLOP으로 진행
            assertThat(dealer.getCurrentRound()).isEqualTo(BettingRound.FLOP);
            assertThat(dealer.getPot()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("에러 케이스 테스트")
    class ErrorCaseTest {

        @BeforeEach
        void startGame() {
            dealer.startTexasHoldem();
        }

        @Test
        @DisplayName("CHECK - 현재 베팅이 있을 때 CHECK 시도하면 예외 발생")
        void check_ShouldFailWhenBetExists() {
            // given
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();

            // when & then
            assertThatThrownBy(() ->
                dealer.processPlayerAction(secondPlayer, PlayerAction.CHECK, 0)
            ).isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("CHECK");
        }

        @Test
        @DisplayName("BET - 현재 베팅이 있을 때 BET 시도하면 예외 발생")
        void bet_ShouldFailWhenBetExists() {
            // given
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 100);

            Player secondPlayer = dealer.getCurrentPlayer();

            // when & then
            assertThatThrownBy(() ->
                dealer.processPlayerAction(secondPlayer, PlayerAction.BET, 100)
            ).isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("BET");
        }

        @Test
        @DisplayName("CALL - 현재 베팅이 없을 때 CALL 시도하면 예외 발생")
        void call_ShouldFailWhenNoBet() {
            // given
            Player currentPlayer = dealer.getCurrentPlayer();

            // when & then
            assertThatThrownBy(() ->
                dealer.processPlayerAction(currentPlayer, PlayerAction.CALL, 0)
            ).isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("CALL");
        }

        @Test
        @DisplayName("RAISE - 현재 베팅보다 낮거나 같은 금액으로 RAISE 시도하면 예외 발생")
        void raise_ShouldFailWhenAmountTooLow() {
            // given
            Player firstPlayer = dealer.getCurrentPlayer();
            dealer.processPlayerAction(firstPlayer, PlayerAction.BET, 200);

            Player secondPlayer = dealer.getCurrentPlayer();

            // when & then
            assertThatThrownBy(() ->
                dealer.processPlayerAction(secondPlayer, PlayerAction.RAISE, 200)
            ).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("RAISE");
        }
    }
}
