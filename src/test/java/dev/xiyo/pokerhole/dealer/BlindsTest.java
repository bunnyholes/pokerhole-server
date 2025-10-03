package dev.xiyo.pokerhole.dealer;

import dev.xiyo.pokerhole.core.domain.player.Player;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 블라인드 베팅 시스템 테스트
 * - 스몰 블라인드 / 빅 블라인드가 자동으로 베팅되는지 검증
 * - 팟 금액, 플레이어 칩, 베팅 금액 등을 검증
 */
class BlindsTest {

    private Dealer dealer;
    private final List<Player> testPlayers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        dealer = Dealer.newDealer();
    }

    @AfterEach
    void tearDown() {
        // 모든 테스트 플레이어의 닉네임 해제
        for (Player player : testPlayers) {
            player.releaseNickname();
        }
        testPlayers.clear();
    }

    private Player createPlayer(String nickname) {
        Player player = Player.newPlayer(nickname);
        testPlayers.add(player);
        return player;
    }

    @Test
    @DisplayName("2명 게임: 스몰/빅 블라인드가 정확히 베팅된다")
    void twoPlayerBlindsPosting() {
        // Given: 2명의 플레이어
        Player player1 = createPlayer("Alice");
        Player player2 = createPlayer("Bob");
        dealer.enrollPlayer(player1);
        dealer.enrollPlayer(player2);

        // When: Texas Hold'em 시작
        dealer.startTexasHoldem();

        // Then: 블라인드 베팅 검증
        // 딜러 버튼이 0번 위치라면:
        // - 스몰 블라인드: 1번 플레이어 (player2)
        // - 빅 블라인드: 0번 플레이어 (player1) - 2명일 때 (0+2)%2 = 0

        // 팟 = 스몰 블라인드(50) + 빅 블라인드(100) = 150
        assertThat(dealer.getPot()).isEqualTo(150);

        // 현재 베팅은 빅 블라인드 금액
        assertThat(dealer.getCurrentBet()).isEqualTo(100);

        // 플레이어 칩 확인
        // player1 (빅 블라인드): 10000 - 100 = 9900
        // player2 (스몰 블라인드): 10000 - 50 = 9950
        assertThat(player1.getChips()).isEqualTo(9900);
        assertThat(player2.getChips()).isEqualTo(9950);

        // 플레이어 상태는 모두 ACTIVE
        assertThat(player1.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
        assertThat(player2.getStatus()).isEqualTo(PlayerStatus.ACTIVE);
    }

    @Test
    @DisplayName("3명 게임: 스몰/빅 블라인드가 정확한 위치에 베팅된다")
    void threePlayerBlindsPosting() {
        // Given: 3명의 플레이어
        Player player1 = createPlayer("Alice");
        Player player2 = createPlayer("Bob");
        Player player3 = createPlayer("Charlie");
        dealer.enrollPlayer(player1);
        dealer.enrollPlayer(player2);
        dealer.enrollPlayer(player3);

        // When: Texas Hold'em 시작
        dealer.startTexasHoldem();

        // Then: 블라인드 베팅 검증
        // 딜러 버튼 = 0
        // 스몰 블라인드 = (0+1) % 3 = 1 (player2)
        // 빅 블라인드 = (0+2) % 3 = 2 (player3)

        // 팟 = 50 + 100 = 150
        assertThat(dealer.getPot()).isEqualTo(150);

        // 현재 베팅 = 100
        assertThat(dealer.getCurrentBet()).isEqualTo(100);

        // 칩 확인
        assertThat(player1.getChips()).isEqualTo(10000); // 블라인드 없음
        assertThat(player2.getChips()).isEqualTo(9950);  // 스몰 블라인드
        assertThat(player3.getChips()).isEqualTo(9900);  // 빅 블라인드
    }

    @Test
    @DisplayName("블라인드 금액이 기본값으로 설정되어 있다")
    void blindAmountsAreCorrect() {
        // Then: 기본값 확인
        assertThat(dealer.getSmallBlind()).isEqualTo(50);
        assertThat(dealer.getBigBlind()).isEqualTo(100);
    }

    @Test
    @DisplayName("팟 총액은 스몰 블라인드 + 빳 블라인드와 같다")
    void potTotalAfterBlinds() {
        // Given
        Player p1 = createPlayer("P1");
        Player p2 = createPlayer("P2");
        dealer.enrollPlayer(p1);
        dealer.enrollPlayer(p2);

        // When
        dealer.startTexasHoldem();

        // Then
        int expectedPot = dealer.getSmallBlind() + dealer.getBigBlind();
        assertThat(dealer.getPot()).isEqualTo(expectedPot);
    }

    @Test
    @DisplayName("첫 액션은 빅 블라인드 다음 플레이어부터 시작한다")
    void firstActionIsAfterBigBlind() {
        // Given: 4명의 플레이어
        Player p1 = createPlayer("P1");
        Player p2 = createPlayer("P2");
        Player p3 = createPlayer("P3");
        Player p4 = createPlayer("P4");
        dealer.enrollPlayer(p1);
        dealer.enrollPlayer(p2);
        dealer.enrollPlayer(p3);
        dealer.enrollPlayer(p4);

        // When: 게임 시작
        dealer.startTexasHoldem();

        // Then: 현재 턴 플레이어 확인
        // 딜러 버튼 = 0
        // 스몰 블라인드 = 1 (p2)
        // 빅 블라인드 = 2 (p3)
        // 첫 액션 = 3 (p4)
        Player currentPlayer = dealer.getCurrentPlayer();
        assertThat(currentPlayer).isEqualTo(p4);
    }

    @Test
    @DisplayName("블라인드 설정을 변경할 수 있다")
    void canChangeBlindAmounts() {
        // When: 블라인드 금액 변경
        dealer.setSmallBlind(100);
        dealer.setBigBlind(200);

        // Then
        assertThat(dealer.getSmallBlind()).isEqualTo(100);
        assertThat(dealer.getBigBlind()).isEqualTo(200);

        // Given: 플레이어 등록
        Player p1 = createPlayer("P1_custom");
        Player p2 = createPlayer("P2_custom");
        dealer.enrollPlayer(p1);
        dealer.enrollPlayer(p2);

        // When: 게임 시작
        dealer.startTexasHoldem();

        // Then: 변경된 블라인드로 베팅됨
        assertThat(dealer.getPot()).isEqualTo(300); // 100 + 200
        assertThat(dealer.getCurrentBet()).isEqualTo(200);
    }

    @Test
    @DisplayName("홀카드는 블라인드 베팅 후에 배분된다")
    void holeCardsDealtAfterBlinds() {
        // Given
        Player p1 = createPlayer("P1_cards");
        Player p2 = createPlayer("P2_cards");
        dealer.enrollPlayer(p1);
        dealer.enrollPlayer(p2);

        // When
        dealer.startTexasHoldem();

        // Then: 각 플레이어가 2장의 홀카드를 받았는지 확인
        // Hand는 Iterable이므로 iterator를 사용하여 카운트
        int p1CardCount = 0;
        for (dev.xiyo.pokerhole.core.domain.card.Card card : p1.getHand()) {
            p1CardCount++;
        }

        int p2CardCount = 0;
        for (dev.xiyo.pokerhole.core.domain.card.Card card : p2.getHand()) {
            p2CardCount++;
        }

        assertThat(p1CardCount).isEqualTo(2);
        assertThat(p2CardCount).isEqualTo(2);
    }
}
