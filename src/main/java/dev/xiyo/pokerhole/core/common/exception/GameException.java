package dev.xiyo.pokerhole.core.common.exception;

/**
 * 게임 도메인 로직에서 발생하는 예외
 */
public class GameException extends DomainException {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
