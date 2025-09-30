package dev.xiyo.pokerhole.core.common.exception;

/**
 * 방 도메인 로직에서 발생하는 예외
 */
public class RoomException extends DomainException {
    public RoomException(String message) {
        super(message);
    }

    public RoomException(String message, Throwable cause) {
        super(message, cause);
    }
}
