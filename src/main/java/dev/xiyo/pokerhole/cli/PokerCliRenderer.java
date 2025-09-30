package dev.xiyo.pokerhole.cli;

import org.jline.terminal.Terminal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

/**
 * ANSI 기반 터미널 렌더러.
 */
public class PokerCliRenderer {
    private static final int WIDTH = 86;
    private static final int HEIGHT = 24;

    private final Terminal terminal;

    public PokerCliRenderer(Terminal terminal) {
        this.terminal = terminal;
    }

    public void render(PokerCliViewModel viewModel) {
        char[][] canvas = new char[HEIGHT][WIDTH];
        for (char[] row : canvas) {
            Arrays.fill(row, ' ');
        }

        placeText(canvas, 0, 1, String.format("판돈: %d", viewModel.pot()));
        placeText(canvas, 0, WIDTH - 12, viewModel.clockText());

        drawCommunityCards(canvas, viewModel.communityCards());
        drawHeroArea(canvas, viewModel.hero());
        drawOpponents(canvas, viewModel.opponents());
        drawActions(canvas, viewModel.actions(), viewModel.selectedActionIndex(), viewModel.raiseAmount());
        drawLog(canvas, viewModel.logLines());

        try {
            terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);
            terminal.flush();
            for (char[] row : canvas) {
                terminal.writer().println(new String(row));
            }
            terminal.writer().flush();
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException("터미널 렌더링 실패", e));
        }
    }

    private void drawCommunityCards(char[][] canvas, List<String> communityCards) {
        if (communityCards.isEmpty()) {
            placeText(canvas, 4, WIDTH / 2 - 6, "커뮤니티 카드: (없음)");
        } else {
            String cards = String.join(" ", communityCards);
            placeText(canvas, 4, WIDTH / 2 - cards.length() / 2, "커뮤니티 카드: " + cards);
        }
    }

    private void drawHeroArea(char[][] canvas, PokerCliSeatState hero) {
        placeText(canvas, HEIGHT - 5, 3, String.format("내 칩: %d", hero.stack()));
        String cards = hero.cards().isEmpty() ? "[??] [??]" : String.join(" ", hero.cards());
        placeText(canvas, HEIGHT - 3, WIDTH / 2 - cards.length() / 2, "내 카드: " + cards);
        placePlayerBadge(canvas, HEIGHT - 8, WIDTH / 2 - 12, hero.name(), "(히어로)");
    }

    private void drawOpponents(char[][] canvas, List<PokerCliSeatState> opponents) {
        for (PokerCliSeatState seat : opponents) {
            switch (seat.position()) {
                case LEFT -> drawOpponent(canvas, 6, 2, seat);
                case TOP -> drawOpponent(canvas, 2, WIDTH / 2 - 12, seat);
                case RIGHT -> drawOpponent(canvas, 6, WIDTH - 24, seat);
                default -> {
                }
            }
        }
    }

    private void drawOpponent(char[][] canvas, int row, int col, PokerCliSeatState seat) {
        placePlayerBadge(canvas, row, col, seat.name(), seat.status());
        placeText(canvas, row + 1, col, String.format("스택: %d", seat.stack()));
    }

    private void placePlayerBadge(char[][] canvas, int row, int col, String name, String status) {
        placeText(canvas, row, col, String.format("┌──────────────┐"));
        placeText(canvas, row + 1, col, String.format("│ %-12s │", trimTo(name, 12)));
        placeText(canvas, row + 2, col, String.format("│ %-12s │", trimTo(status, 12)));
        placeText(canvas, row + 3, col, String.format("└──────────────┘"));
    }

    private void drawActions(char[][] canvas, List<String> actions, int selectedIndex, int raiseAmount) {
        int baseRow = HEIGHT - 8;
        placeText(canvas, baseRow, WIDTH / 2 - 10, "액션 선택 (Enter로 확정)");

        int startCol = WIDTH / 2 - (actions.size() * 12) / 2;
        for (int i = 0; i < actions.size(); i++) {
            String label = actions.get(i);
            boolean selected = i == selectedIndex;
            String text = selected ? String.format("▶ %s ◀", label) : String.format("  %s  ", label);
            placeText(canvas, baseRow + 1, startCol + i * 12, padTo(text, 10));
        }

        placeText(canvas, baseRow + 3, WIDTH / 2 - 12, String.format("베팅/레이즈 금액: %d", raiseAmount));
        placeText(canvas, baseRow + 4, WIDTH / 2 - 16, "↑↓ 로 금액을 조정할 수 있습니다");
    }

    private void drawLog(char[][] canvas, List<String> logLines) {
        int row = HEIGHT - 6;
        placeText(canvas, row, 2, "최근 이벤트");
        int offset = 1;
        for (String line : logLines) {
            if (row + offset >= HEIGHT) {
                break;
            }
            placeText(canvas, row + offset, 2, trimTo(line, WIDTH - 4));
            offset++;
        }
    }

    private void placeText(char[][] canvas, int row, int col, String text) {
        if (row < 0 || row >= HEIGHT) {
            return;
        }
        if (col < 0) {
            text = text.substring(-col);
            col = 0;
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length && col + i < WIDTH; i++) {
            canvas[row][col + i] = chars[i];
        }
    }

    private String padTo(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }

    private String trimTo(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 1) + "…";
    }
}
