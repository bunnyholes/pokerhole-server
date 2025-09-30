package dev.xiyo.pokerhole.ui.cli;

/**
 * 화살표 입력과 엔터, 종료 키를 추상화한 커맨드.
 */
public enum ArrowCommand {
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    CONFIRM,
    CANCEL,
    QUIT
}
