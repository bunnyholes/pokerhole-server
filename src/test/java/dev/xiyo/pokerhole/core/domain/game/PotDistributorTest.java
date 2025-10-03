package dev.xiyo.pokerhole.core.domain.game;

import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PotDistributor 테스트")
class PotDistributorTest {

    private PotDistributor distributor;
    private PlayerId player1;
    private PlayerId player2;
    private PlayerId player3;
    private PlayerId player4;

    @BeforeEach
    void setUp() {
        distributor = new PotDistributor();
        player1 = PlayerId.of(UUID.randomUUID());
        player2 = PlayerId.of(UUID.randomUUID());
        player3 = PlayerId.of(UUID.randomUUID());
        player4 = PlayerId.of(UUID.randomUUID());
    }

    @Nested
    @DisplayName("distribute - 기본 분배 테스트")
    class DistributeBasicTest {

        @Test
        @DisplayName("승자 1명 - 전체 팟을 받음")
        void singleWinnerGetsEntirePot() {
            List<PlayerId> winners = List.of(player1);
            int pot = 1000;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(1);
            assertThat(result.get(player1)).isEqualTo(1000);
        }

        @Test
        @DisplayName("승자 2명 - 균등 분배 (짝수)")
        void twoWinnersEvenSplit() {
            List<PlayerId> winners = List.of(player1, player2);
            int pot = 1000;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(2);
            assertThat(result.get(player1)).isEqualTo(500);
            assertThat(result.get(player2)).isEqualTo(500);
        }

        @Test
        @DisplayName("승자 2명 - 홀수 팟 분배")
        void twoWinnersOddSplit() {
            List<PlayerId> winners = List.of(player1, player2);
            int pot = 1001;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(2);
            // 첫 번째 승자가 나머지 1칩을 받음
            assertThat(result.get(player1)).isEqualTo(501);
            assertThat(result.get(player2)).isEqualTo(500);
            // 총합 검증
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(1001);
        }

        @Test
        @DisplayName("승자 3명 - 균등 분배 (나머지 0)")
        void threeWinnersEvenSplit() {
            List<PlayerId> winners = List.of(player1, player2, player3);
            int pot = 1200;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(3);
            assertThat(result.get(player1)).isEqualTo(400);
            assertThat(result.get(player2)).isEqualTo(400);
            assertThat(result.get(player3)).isEqualTo(400);
        }

        @Test
        @DisplayName("승자 3명 - 나머지 2칩")
        void threeWinnersWithTwoChipsRemainder() {
            List<PlayerId> winners = List.of(player1, player2, player3);
            int pot = 1001;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(3);
            // 앞의 두 승자가 각각 나머지 1칩씩 받음
            assertThat(result.get(player1)).isEqualTo(334);
            assertThat(result.get(player2)).isEqualTo(334);
            assertThat(result.get(player3)).isEqualTo(333);
            // 총합 검증
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(1001);
        }

        @Test
        @DisplayName("승자 3명 - 나머지 1칩")
        void threeWinnersWithOneChipRemainder() {
            List<PlayerId> winners = List.of(player1, player2, player3);
            int pot = 1000;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(3);
            // 첫 번째 승자가 나머지 1칩을 받음
            assertThat(result.get(player1)).isEqualTo(334);
            assertThat(result.get(player2)).isEqualTo(333);
            assertThat(result.get(player3)).isEqualTo(333);
            // 총합 검증
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(1000);
        }

        @Test
        @DisplayName("승자 4명 - 나머지 3칩")
        void fourWinnersWithThreeChipsRemainder() {
            List<PlayerId> winners = List.of(player1, player2, player3, player4);
            int pot = 1003;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(4);
            // 앞의 세 승자가 각각 나머지 1칩씩 받음
            assertThat(result.get(player1)).isEqualTo(251);
            assertThat(result.get(player2)).isEqualTo(251);
            assertThat(result.get(player3)).isEqualTo(251);
            assertThat(result.get(player4)).isEqualTo(250);
            // 총합 검증
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(1003);
        }
    }

    @Nested
    @DisplayName("distribute - 엣지 케이스 테스트")
    class DistributeEdgeCaseTest {

        @Test
        @DisplayName("팟이 0일 때 - 모든 승자가 0칩")
        void zeroPot() {
            List<PlayerId> winners = List.of(player1, player2);
            int pot = 0;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(2);
            assertThat(result.get(player1)).isEqualTo(0);
            assertThat(result.get(player2)).isEqualTo(0);
        }

        @Test
        @DisplayName("팟이 승자 수보다 작을 때")
        void potSmallerThanWinnerCount() {
            List<PlayerId> winners = List.of(player1, player2, player3);
            int pot = 2; // 3명인데 2칩

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(3);
            // 앞의 두 승자만 1칩씩 받음
            assertThat(result.get(player1)).isEqualTo(1);
            assertThat(result.get(player2)).isEqualTo(1);
            assertThat(result.get(player3)).isEqualTo(0);
            // 총합 검증
            assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(2);
        }

        @Test
        @DisplayName("팟이 음수일 때 - 예외 발생")
        void negativePotThrowsException() {
            List<PlayerId> winners = List.of(player1, player2);
            int pot = -100;

            assertThatThrownBy(() -> distributor.distribute(pot, winners, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pot cannot be negative");
        }

        @Test
        @DisplayName("승자 목록이 null일 때 - 예외 발생")
        void nullWinnersThrowsException() {
            assertThatThrownBy(() -> distributor.distribute(1000, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Winners list cannot be null or empty");
        }

        @Test
        @DisplayName("승자 목록이 비어있을 때 - 예외 발생")
        void emptyWinnersThrowsException() {
            assertThatThrownBy(() -> distributor.distribute(1000, List.of(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Winners list cannot be null or empty");
        }

        @Test
        @DisplayName("승자 목록에 중복이 있을 때 - 예외 발생")
        void duplicateWinnersThrowsException() {
            List<PlayerId> winners = List.of(player1, player2, player1); // player1 중복

            assertThatThrownBy(() -> distributor.distribute(1000, winners, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Winners list contains duplicates");
        }

        @Test
        @DisplayName("매우 큰 팟 (Integer.MAX_VALUE)")
        void veryLargePot() {
            List<PlayerId> winners = List.of(player1, player2);
            int pot = Integer.MAX_VALUE;

            Map<PlayerId, Integer> result = distributor.distribute(pot, winners, 0);

            assertThat(result).hasSize(2);
            int expectedBase = Integer.MAX_VALUE / 2;
            int expectedRemainder = Integer.MAX_VALUE % 2;
            assertThat(result.get(player1)).isEqualTo(expectedBase + expectedRemainder);
            assertThat(result.get(player2)).isEqualTo(expectedBase);
        }
    }

    @Nested
    @DisplayName("reorderWinnersByDealerButton - 순서 재정렬 테스트")
    class ReorderWinnersByDealerButtonTest {

        @Test
        @DisplayName("딜러 버튼 위치 0 - 순서 유지")
        void dealerButtonAtPositionZero() {
            List<PlayerId> allPlayers = List.of(player1, player2, player3, player4);
            List<PlayerId> winners = List.of(player2, player4);

            List<PlayerId> result = distributor.reorderWinnersByDealerButton(winners, allPlayers, 0);

            // 딜러 버튼이 0이면 player1 다음부터 시작
            // player2(위치1), player4(위치3) 순서 유지
            assertThat(result).containsExactly(player2, player4);
        }

        @Test
        @DisplayName("딜러 버튼 위치 1 - 순서 변경")
        void dealerButtonAtPositionOne() {
            List<PlayerId> allPlayers = List.of(player1, player2, player3, player4);
            List<PlayerId> winners = List.of(player1, player3);

            List<PlayerId> result = distributor.reorderWinnersByDealerButton(winners, allPlayers, 1);

            // 딜러 버튼이 1(player2)이면 player3부터 시작
            // player3(상대위치2), player1(상대위치3) 순서
            assertThat(result).containsExactly(player3, player1);
        }

        @Test
        @DisplayName("딜러 버튼 위치 2 - 순환 순서")
        void dealerButtonAtPositionTwo() {
            List<PlayerId> allPlayers = List.of(player1, player2, player3, player4);
            List<PlayerId> winners = List.of(player1, player2, player4);

            List<PlayerId> result = distributor.reorderWinnersByDealerButton(winners, allPlayers, 2);

            // 딜러 버튼이 2(player3)이면 player4부터 시작
            // player4(상대위치1), player1(상대위치2), player2(상대위치3) 순서
            assertThat(result).containsExactly(player4, player1, player2);
        }

        @Test
        @DisplayName("딜러 버튼 위치 3 - 마지막 위치")
        void dealerButtonAtLastPosition() {
            List<PlayerId> allPlayers = List.of(player1, player2, player3, player4);
            List<PlayerId> winners = List.of(player1, player3, player4);

            List<PlayerId> result = distributor.reorderWinnersByDealerButton(winners, allPlayers, 3);

            // 딜러 버튼이 3(player4)이면 상대 위치 계산:
            // player4(상대위치0), player1(상대위치1), player3(상대위치3)
            assertThat(result).containsExactly(player4, player1, player3);
        }

        @Test
        @DisplayName("승자 목록이 null일 때 - 예외 발생")
        void nullWinnersThrowsException() {
            List<PlayerId> allPlayers = List.of(player1, player2);

            assertThatThrownBy(() ->
                distributor.reorderWinnersByDealerButton(null, allPlayers, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Winners and allPlayers cannot be null");
        }

        @Test
        @DisplayName("전체 플레이어 목록이 null일 때 - 예외 발생")
        void nullAllPlayersThrowsException() {
            List<PlayerId> winners = List.of(player1);

            assertThatThrownBy(() ->
                distributor.reorderWinnersByDealerButton(winners, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Winners and allPlayers cannot be null");
        }

        @Test
        @DisplayName("딜러 버튼 위치가 음수일 때 - 예외 발생")
        void negativeDealerButtonThrowsException() {
            List<PlayerId> allPlayers = List.of(player1, player2);
            List<PlayerId> winners = List.of(player1);

            assertThatThrownBy(() ->
                distributor.reorderWinnersByDealerButton(winners, allPlayers, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid dealer button position");
        }

        @Test
        @DisplayName("딜러 버튼 위치가 범위 밖일 때 - 예외 발생")
        void dealerButtonOutOfRangeThrowsException() {
            List<PlayerId> allPlayers = List.of(player1, player2);
            List<PlayerId> winners = List.of(player1);

            assertThatThrownBy(() ->
                distributor.reorderWinnersByDealerButton(winners, allPlayers, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid dealer button position");
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("실제 게임 시나리오 - 딜러 버튼 고려한 분배")
        void realGameScenario() {
            // 4명의 플레이어, player2가 딜러 버튼
            List<PlayerId> allPlayers = List.of(player1, player2, player3, player4);
            List<PlayerId> winners = List.of(player1, player3, player4);
            int dealerButtonPosition = 1; // player2
            int pot = 1000;

            // 1. 승자 순서 재정렬 (딜러 버튼 다음부터)
            List<PlayerId> orderedWinners = distributor.reorderWinnersByDealerButton(
                winners, allPlayers, dealerButtonPosition);

            // 딜러 버튼(player2) 다음: player3 -> player4 -> player1
            assertThat(orderedWinners).containsExactly(player3, player4, player1);

            // 2. 팟 분배
            Map<PlayerId, Integer> distribution = distributor.distribute(
                pot, orderedWinners, dealerButtonPosition);

            // 1000 / 3 = 333, 나머지 1
            // player3: 334 (첫 번째, 나머지 1칩 받음)
            // player4: 333
            // player1: 333
            assertThat(distribution.get(player3)).isEqualTo(334);
            assertThat(distribution.get(player4)).isEqualTo(333);
            assertThat(distribution.get(player1)).isEqualTo(333);

            // 총합 검증
            assertThat(distribution.values().stream().mapToInt(Integer::intValue).sum())
                .isEqualTo(1000);
        }

        @Test
        @DisplayName("2명 동점 - 딜러 버튼 위치에 따른 나머지 분배")
        void twoWayTieWithDealerButton() {
            List<PlayerId> allPlayers = List.of(player1, player2, player3, player4);
            List<PlayerId> winners = List.of(player2, player4);
            int dealerButtonPosition = 2; // player3
            int pot = 1001;

            // 1. 승자 순서 재정렬
            List<PlayerId> orderedWinners = distributor.reorderWinnersByDealerButton(
                winners, allPlayers, dealerButtonPosition);

            // 딜러 버튼(player3) 다음: player4 -> player1 -> player2
            // winners 중에서: player4 -> player2
            assertThat(orderedWinners).containsExactly(player4, player2);

            // 2. 팟 분배
            Map<PlayerId, Integer> distribution = distributor.distribute(
                pot, orderedWinners, dealerButtonPosition);

            // 1001 / 2 = 500, 나머지 1
            // player4: 501 (첫 번째, 나머지 1칩 받음)
            // player2: 500
            assertThat(distribution.get(player4)).isEqualTo(501);
            assertThat(distribution.get(player2)).isEqualTo(500);
        }
    }
}
