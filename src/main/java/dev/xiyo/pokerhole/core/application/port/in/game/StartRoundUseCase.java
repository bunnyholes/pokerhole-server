package dev.xiyo.pokerhole.core.application.port.in.game;

import dev.xiyo.pokerhole.core.application.port.in.game.dto.StartRoundCommand;

/**
 * 라운드 시작 Use Case
 * 게임의 새로운 라운드를 시작하는 비즈니스 로직의 진입점입니다.
 */
public interface StartRoundUseCase {
    
    /**
     * 라운드를 시작합니다.
     * 
     * @param command 라운드 시작 커맨드
     */
    void execute(StartRoundCommand command);
}
