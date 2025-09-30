package dev.xiyo.pokerhole.core.common.exception;

/**
 * 도메인 로직에서 발생하는 기본 예외
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
