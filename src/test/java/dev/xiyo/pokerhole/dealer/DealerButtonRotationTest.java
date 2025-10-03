package dev.xiyo.pokerhole.dealer;

import dev.xiyo.pokerhole.core.domain.player.Player;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * 딜러 버튼 로테이션 관련 단위 테스트
 */
@DisplayName("딜러 버튼 로테이션 테스트")
class DealerButtonRotationTest {

    private Dealer dealer;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;

    @BeforeEach
    void setUp() {
        dealer = Dealer.newDealer();
        dealer.setSmallBlind(0);
        dealer.setBigBlind(0);

        player1 = Player.newPlayer("Alice");
        player2 = Player.newPlayer("Bob");
        player3 = Player.newPlayer("Charlie");
        player4 = Player.newPlayer("Diana");
    }

    @AfterEach
    void tearDown() {
        player1.releaseNickname();
        player2.releaseNickname();
        player3.releaseNickname();
        player4.releaseNickname();
    }

    @Nested
    @DisplayName("rotateDealerButton() 기본 동작")
    class RotateDealerButtonBasicTest {

        @Test
        @DisplayName("딜러 버튼을 시계방향으로 1칸 회전시킨다")
        void rotateDealerButton_ShouldRotateClockwise() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);
            dealer.enrollPlayer(player3);
            dealer.enrollPlayer(player4);

            int initialPosition = dealer.getDealerButtonPosition();
            assertThat(initialPosition).isEqualTo(0);

            // when
            dealer.rotateDealerButton();

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
        }

        @Test
        @DisplayName("핸드 번호를 1 증가시킨다")
        void rotateDealerButton_ShouldIncrementHandNumber() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);

            int initialHandNumber = dealer.getCurrentHandNumber();
            assertThat(initialHandNumber).isEqualTo(0);

            // when
            dealer.rotateDealerButton();

            // then
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("마지막 위치에서 회전 시 0번 위치로 순환한다 (4명)")
        void rotateDealerButton_ShouldWrapAround_FourPlayers() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);
            dealer.enrollPlayer(player3);
            dealer.enrollPlayer(player4);

            // 버튼을 3번 위치로 이동 (0 -> 1 -> 2 -> 3)
            dealer.rotateDealerButton();
            dealer.rotateDealerButton();
            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(3);

            // when
            dealer.rotateDealerButton();

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0);
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(4);
        }

        @Test
        @DisplayName("2명 게임에서 딜러 버튼이 교대로 회전한다")
        void rotateDealerButton_ShouldAlternate_TwoPlayers() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);

            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0);

            // when & then
            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);

            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0);

            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
        }

        @Test
        @DisplayName("3명 게임에서 딜러 버튼이 순환한다")
        void rotateDealerButton_ShouldCycle_ThreePlayers() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);
            dealer.enrollPlayer(player3);

            // when & then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0);

            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);

            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(2);

            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0); // 순환
        }
    }

    @Nested
    @DisplayName("rotateDealerButton() 예외 상황")
    class RotateDealerButtonEdgeCaseTest {

        @Test
        @DisplayName("플레이어가 없을 때 회전하지 않는다")
        void rotateDealerButton_WithNoPlayers_ShouldNotRotate() {
            // given
            assertThat(dealer.getPlayers()).isEmpty();

            int initialPosition = dealer.getDealerButtonPosition();
            int initialHandNumber = dealer.getCurrentHandNumber();

            // when
            dealer.rotateDealerButton();

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(initialPosition);
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(initialHandNumber);
        }

        @Test
        @DisplayName("플레이어가 1명일 때 위치는 유지하고 핸드 번호만 증가")
        void rotateDealerButton_WithOnePlayer_ShouldOnlyIncrementHandNumber() {
            // given
            dealer.enrollPlayer(player1);

            int initialPosition = dealer.getDealerButtonPosition();
            int initialHandNumber = dealer.getCurrentHandNumber();

            // when
            dealer.rotateDealerButton();

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(initialPosition); // 위치 유지
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(initialHandNumber + 1); // 핸드 번호만 증가
        }
    }

    @Nested
    @DisplayName("endHand() 테스트")
    class EndHandTest {

        @Test
        @DisplayName("endHand()는 딜러 버튼을 회전시킨다")
        void endHand_ShouldRotateDealerButton() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);
            dealer.enrollPlayer(player3);

            int initialPosition = dealer.getDealerButtonPosition();

            // when
            dealer.endHand();

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo((initialPosition + 1) % 3);
        }

        @Test
        @DisplayName("endHand()는 핸드 번호를 증가시킨다")
        void endHand_ShouldIncrementHandNumber() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);

            int initialHandNumber = dealer.getCurrentHandNumber();

            // when
            dealer.endHand();

            // then
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(initialHandNumber + 1);
        }
    }

    @Nested
    @DisplayName("startNewHand() 테스트")
    class StartNewHandTest {

        @Test
        @DisplayName("startNewHand()는 딜러 버튼을 회전시킨 후 새 게임을 시작한다")
        void startNewHand_ShouldRotateButtonAndStartNewGame() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);
            dealer.enrollPlayer(player3);

            int initialPosition = dealer.getDealerButtonPosition();
            int initialHandNumber = dealer.getCurrentHandNumber();

            // when
            dealer.startNewHand();

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo((initialPosition + 1) % 3);
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(initialHandNumber + 1);

            // 새 게임이 시작되었는지 확인 (홀카드 배분)
            // Hand는 Iterable<Card>이고 Comparable도 구현하므로 직접 iterate해서 확인
            long player1CardCount = java.util.stream.StreamSupport.stream(player1.getHand().spliterator(), false).count();
            long player2CardCount = java.util.stream.StreamSupport.stream(player2.getHand().spliterator(), false).count();
            long player3CardCount = java.util.stream.StreamSupport.stream(player3.getHand().spliterator(), false).count();
            assertThat(player1CardCount).isEqualTo(2);
            assertThat(player2CardCount).isEqualTo(2);
            assertThat(player3CardCount).isEqualTo(2);
        }

        @Test
        @DisplayName("startNewHand()를 연속으로 호출하면 딜러 버튼이 계속 회전한다")
        void startNewHand_MultipleCallsShouldKeepRotating() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);
            dealer.enrollPlayer(player3);
            dealer.enrollPlayer(player4);

            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0);

            // when & then
            dealer.startNewHand();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(1);

            dealer.startNewHand();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(2);
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(2);

            dealer.startNewHand();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(3);
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(3);

            dealer.startNewHand();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0); // 순환
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("adjustDealerButtonOnPlayerLeave() 테스트")
    class AdjustDealerButtonOnPlayerLeaveTest {

        @Test
        @DisplayName("딜러 버튼 앞의 플레이어가 떠나면 버튼 위치를 1 감소시킨다")
        void adjustDealerButtonOnPlayerLeave_PlayerBeforeButton_ShouldDecrementPosition() {
            // given
            dealer.enrollPlayer(player1); // 0
            dealer.enrollPlayer(player2); // 1
            dealer.enrollPlayer(player3); // 2
            dealer.enrollPlayer(player4); // 3

            // 딜러 버튼을 2번 위치로 이동
            dealer.rotateDealerButton();
            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(2);

            // when: 0번 플레이어(player1)가 떠남
            dealer.adjustDealerButtonOnPlayerLeave(player1);
            dealer.removePlayer(player1);

            // then: 버튼 위치가 1로 조정됨 (2 - 1 = 1)
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
        }

        @Test
        @DisplayName("딜러 버튼 위치의 플레이어가 떠나면 버튼 위치는 유지된다")
        void adjustDealerButtonOnPlayerLeave_PlayerAtButton_ShouldMaintainPosition() {
            // given
            dealer.enrollPlayer(player1); // 0
            dealer.enrollPlayer(player2); // 1
            dealer.enrollPlayer(player3); // 2
            dealer.enrollPlayer(player4); // 3

            dealer.rotateDealerButton(); // 버튼 위치 = 1
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);

            // when: 1번 플레이어(player2)가 떠남
            dealer.adjustDealerButtonOnPlayerLeave(player2);
            dealer.removePlayer(player2);

            // then: 버튼 위치는 그대로 1 (다음 플레이어가 자연스럽게 딜러가 됨)
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
        }

        @Test
        @DisplayName("딜러 버튼 뒤의 플레이어가 떠나면 버튼 위치는 변경되지 않는다")
        void adjustDealerButtonOnPlayerLeave_PlayerAfterButton_ShouldNotChange() {
            // given
            dealer.enrollPlayer(player1); // 0
            dealer.enrollPlayer(player2); // 1
            dealer.enrollPlayer(player3); // 2
            dealer.enrollPlayer(player4); // 3

            dealer.rotateDealerButton(); // 버튼 위치 = 1
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);

            // when: 3번 플레이어(player4)가 떠남
            dealer.adjustDealerButtonOnPlayerLeave(player4);
            dealer.removePlayer(player4);

            // then: 버튼 위치는 그대로 1
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
        }

        @Test
        @DisplayName("리스트에 없는 플레이어를 전달하면 아무 일도 하지 않는다")
        void adjustDealerButtonOnPlayerLeave_NonExistentPlayer_ShouldDoNothing() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);

            Player outsider = Player.newPlayer("Outsider");
            int initialPosition = dealer.getDealerButtonPosition();

            // when
            dealer.adjustDealerButtonOnPlayerLeave(outsider);

            // then
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(initialPosition);

            // cleanup
            outsider.releaseNickname();
        }

        @Test
        @DisplayName("마지막 플레이어가 떠나서 버튼 위치가 범위를 벗어나면 조정된다")
        void adjustDealerButtonOnPlayerLeave_LastPlayer_ShouldAdjustToLastValidPosition() {
            // given
            dealer.enrollPlayer(player1); // 0
            dealer.enrollPlayer(player2); // 1
            dealer.enrollPlayer(player3); // 2

            // 버튼을 2번 위치로 이동
            dealer.rotateDealerButton();
            dealer.rotateDealerButton();
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(2);

            // when: 2번 플레이어(player3, 마지막 플레이어)가 떠남
            dealer.adjustDealerButtonOnPlayerLeave(player3);
            dealer.removePlayer(player3);

            // then: 버튼 위치가 1로 조정됨 (players.size() - 1 = 2 - 1 = 1)
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("블라인드와 딜러 버튼 통합 테스트")
    class BlindAndDealerButtonIntegrationTest {

        @BeforeEach
        void setUpBlinds() {
            dealer.setSmallBlind(50);
            dealer.setBigBlind(100);
        }

        @Test
        @DisplayName("딜러 버튼 위치에 따라 블라인드 플레이어가 결정된다")
        void startTexasHoldem_ShouldSetBlindsBasedOnDealerButton() {
            // given
            dealer.enrollPlayer(player1); // 0
            dealer.enrollPlayer(player2); // 1
            dealer.enrollPlayer(player3); // 2
            dealer.enrollPlayer(player4); // 3

            // 딜러 버튼이 0번 위치
            assertThat(dealer.getDealerButtonPosition()).isEqualTo(0);

            // when
            dealer.startTexasHoldem();

            // then: 스몰 블라인드 = 1번, 빅 블라인드 = 2번
            assertThat(player1.getChips()).isEqualTo(10000); // 딜러, 블라인드 없음
            assertThat(player2.getChips()).isEqualTo(9950);  // 스몰 블라인드 50
            assertThat(player3.getChips()).isEqualTo(9900);  // 빅 블라인드 100
            assertThat(player4.getChips()).isEqualTo(10000); // 블라인드 없음
        }

        @Test
        @DisplayName("딜러 버튼 회전 후 블라인드 위치가 변경된다")
        void startNewHand_ShouldRotateBlindsWithDealerButton() {
            // given
            dealer.enrollPlayer(player1); // 0
            dealer.enrollPlayer(player2); // 1
            dealer.enrollPlayer(player3); // 2
            dealer.enrollPlayer(player4); // 3

            dealer.startTexasHoldem(); // 첫 핸드 (버튼 = 0)

            // when: 새 핸드 시작 (버튼 = 1)
            dealer.startNewHand();

            // then: 스몰 블라인드 = 2번, 빅 블라인드 = 3번
            // 첫 핸드 칩: player1=10000, player2=9950, player3=9900, player4=10000
            // 두 번째 핸드: player1=블라인드X, player2=빅100, player3=스몰50, player4=블라인드X
            assertThat(player1.getChips()).isEqualTo(10000); // 변화 없음
            assertThat(player2.getChips()).isEqualTo(10000 - 50); // 첫 핸드 스몰50만 차감됨
            assertThat(player3.getChips()).isEqualTo(10000 - 100 - 50); // 첫 핸드 빅100 + 이번 스몰50
            assertThat(player4.getChips()).isEqualTo(10000 - 100); // 이번 빅100만 차감
        }

        @Test
        @DisplayName("2명 게임에서 딜러 버튼 플레이어가 스몰 블라인드를 낸다")
        void startTexasHoldem_TwoPlayers_DealerIsSmallBlind() {
            // given
            dealer.enrollPlayer(player1); // 0 (딜러 버튼)
            dealer.enrollPlayer(player2); // 1

            // when
            dealer.startTexasHoldem();

            // then: 딜러(0번) = 스몰 블라인드, 1번 = 빅 블라인드
            assertThat(player1.getChips()).isEqualTo(9950);  // 스몰 50
            assertThat(player2.getChips()).isEqualTo(9900);  // 빅 100
        }
    }

    @Nested
    @DisplayName("핸드 번호 추적 테스트")
    class HandNumberTrackingTest {

        @Test
        @DisplayName("초기 핸드 번호는 0이다")
        void initialHandNumber_ShouldBeZero() {
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("여러 핸드를 진행하면 핸드 번호가 누적된다")
        void multipleHands_ShouldAccumulateHandNumber() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);

            // when & then
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(0);

            dealer.startNewHand();
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(1);

            dealer.startNewHand();
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(2);

            dealer.startNewHand();
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("rotateDealerButton()을 직접 호출해도 핸드 번호가 증가한다")
        void rotateDealerButton_ShouldIncrementHandNumber() {
            // given
            dealer.enrollPlayer(player1);
            dealer.enrollPlayer(player2);

            // when & then
            dealer.rotateDealerButton();
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(1);

            dealer.rotateDealerButton();
            assertThat(dealer.getCurrentHandNumber()).isEqualTo(2);
        }
    }
}
