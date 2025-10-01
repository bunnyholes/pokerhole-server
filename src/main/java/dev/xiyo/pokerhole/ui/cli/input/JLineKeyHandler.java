package dev.xiyo.pokerhole.ui.cli.input;

import dev.xiyo.pokerhole.ui.cli.ArrowCommand;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JLine 키 입력 핸들러
 * 화살표 키, Enter, ESC 감지
 */
@Slf4j
@Component
public class JLineKeyHandler {

    /**
     * 키 입력 읽기 (비동기)
     *
     * @param reader NonBlockingReader
     * @return ArrowCommand 또는 null
     */
    public ArrowCommand readKey(NonBlockingReader reader) {
        try {
            int c = reader.read(10); // 10ms 타임아웃

            if (c == -2) { // 타임아웃
                return null;
            }

            if (c == -1) { // EOF
                return ArrowCommand.QUIT;
            }

            // ESC 시퀀스 감지
            if (c == 27) { // ESC
                int next = reader.read(10);
                if (next == '[') {
                    int key = reader.read(10);
                    return switch (key) {
                        case 'A' -> ArrowCommand.MOVE_UP;
                        case 'B' -> ArrowCommand.MOVE_DOWN;
                        case 'C' -> ArrowCommand.MOVE_RIGHT;
                        case 'D' -> ArrowCommand.MOVE_LEFT;
                        default -> ArrowCommand.CANCEL;
                    };
                }
                return ArrowCommand.CANCEL; // ESC 단독
            }

            // Enter
            if (c == '\r' || c == '\n') {
                return ArrowCommand.CONFIRM;
            }

            // q 키 (나가기)
            if (c == 'q' || c == 'Q') {
                return ArrowCommand.QUIT;
            }

            // c 키 (취소)
            if (c == 'c' || c == 'C') {
                return ArrowCommand.CANCEL;
            }

        } catch (IOException e) {
            log.error("키 입력 읽기 실패", e);
        }

        return null;
    }

    /**
     * 터미널 초기화
     */
    public void initializeTerminal(Terminal terminal) {
        try {
            terminal.enterRawMode();
            log.info("터미널 Raw 모드 활성화");
        } catch (Exception e) {
            log.warn("터미널 Raw 모드 활성화 실패", e);
        }
    }

    /**
     * 화면 클리어
     */
    public void clearScreen(Terminal terminal) {
        terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);
        terminal.flush();
    }

    /**
     * 커서 이동
     */
    public void moveCursor(Terminal terminal, int row, int col) {
        terminal.puts(org.jline.utils.InfoCmp.Capability.cursor_address, row, col);
        terminal.flush();
    }
}
