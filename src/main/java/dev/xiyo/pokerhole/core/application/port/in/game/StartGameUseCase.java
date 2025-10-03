package dev.xiyo.pokerhole.core.application.port.in.game;

import dev.xiyo.pokerhole.core.application.port.in.game.dto.StartGameCommand;

/**
 * 게임 시작 Use Case
 * 매칭이 완료된 플레이어들로 새로운 게임을 시작하는 비즈니스 로직의 진입점입니다.
 */
public interface StartGameUseCase {

    /**
     * 게임을 시작합니다.
     *
     * @param command 게임 시작 커맨드
     * @return 생성된 게임 방 ID
     */
    String execute(StartGameCommand command);
}
