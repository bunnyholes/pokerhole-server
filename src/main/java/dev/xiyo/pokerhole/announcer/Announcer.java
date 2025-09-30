package dev.xiyo.pokerhole.announcer;

import dev.xiyo.pokerhole.common.Hand;
import dev.xiyo.pokerhole.dealer.Dealer;
import dev.xiyo.pokerhole.player.Player;

import java.util.*;

public class Announcer {

    private static final String[] medals = {"🥇", "🥈", "🥉", "💩"};

    private static void printBanner(String symbol, String message, int length) {
        System.out.println("\n" + symbol + " " + "-".repeat(length));
        System.out.println(message);
        System.out.println(symbol + " " + "-".repeat(length) + "\n");
    }

    public static void standbyStage() {
        printBanner("🎬", "🚀 새로운 스테이지가 시작되었습니다! 🚀", 30);
    }

    public static void endStage() {
        printBanner("🏁", "🛑 스테이지가 종료되었습니다.", 30);
    }

    public static void newGame() {
        printBanner("🎲", "✨ 새로운 매치를 시작합니다! ✨", 30);
    }

    public static void endGame() {
        printBanner("🏆", "🛑 매치가 종료되었습니다.", 30);
    }

    // 매치 승리자 발표
    public static void matchWinner(Player player) {
        String nickName = player.getNickName();
        player.openHand();
        Hand hand = player.getHand();
        System.out.println("🏆 " + nickName + "님이 " + hand.toString() + "로 승리하셨습니다!");
    }

    public static void draw() {
        System.out.println("🤝 무승부입니다.");
    }

    // 스테이지 승리자 발표
    public static void stageWinner(Optional<Player> totalStageWinner) {
        if (totalStageWinner.isPresent()) {
            Player player = totalStageWinner.get();
            String message = String.format(
                    "🎉 %s님이 최종 승리하셨습니다! %n🏆 최종 포인트 %d점, 전적 %s%d승 %s%d패 %s%d무",
                    player.getNickName(), player.getPoint(), "✅ ", player.getWins(), "❌ ", player.getLosses(), "🤝 ", player.getDraws()
            );
            System.out.println(message);
        } else {
            draw();
        }
    }

    public static void showStageResult(List<Player> players) {
        System.out.println("🔔 스테이지 결과:");
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            String message = String.format(
                    "%s %s님의 포인트 %d점, 전적 %s%d승 %s%d패 %s%d무",
                    medals[i], player.getNickName(), player.getPoint(), "✅ ", player.getWins(), "❌ ", player.getLosses(), "🤝 ", player.getDraws()
            );
            System.out.println(message);
        }
    }

    // 매치 결과 발표
    public static void openWinner(Optional<Player> optionalPlayer) {
        if (optionalPlayer.isPresent()) {
            matchWinner(optionalPlayer.get());
        } else {
            draw();
        }
    }

    public static void matchResult(Map<String, String> matchResult) {
        System.out.println("🔍 매치 결과:");
        for (Map.Entry<String, String> entry : matchResult.entrySet()) {
            String nickName = entry.getKey();
            String hand = entry.getValue();
            System.out.println("🃏 " + nickName + "님의 패: " + hand);
        }
    }

    public static void dealCard() {
        System.out.println("🃏 카드를 나눠주었습니다."); // 카드 배분 완료
    }

    public static void cardShuffle() {
        System.out.println("🔄 카드를 섞었습니다."); // 카드 섞기 완료
    }

    public static void handOpen() {
        System.out.println("🔍 카드를 오픈합니다."); // 카드 오픈
    }

    public static void playStage() {
        System.out.println("💀 포커 100판 진행 💀");
    }

    public static void enrollPlayer(Player player) {
        System.out.println("👥 " + player.getNickName() + "님이 입장하셨습니다.");
    }

    public static void enrollDealer(Dealer dealer) {
        System.out.println("🎩 딜러가 입장하셨습니다.");
    }
}
