package dev.xiyo.pokerhole.adapter.in.terminal.handler;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import dev.xiyo.pokerhole.core.application.port.in.matching.CreateMatchingCodeUseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.JoinCodeMatchingUseCase;
import dev.xiyo.pokerhole.core.application.port.in.matching.JoinRandomMatchingUseCase;
import dev.xiyo.pokerhole.core.domain.matching.MatchingCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 매칭 핸들러
 * 랜덤/코드 매칭 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingHandler {

    private final JoinRandomMatchingUseCase joinRandomMatchingUseCase;
    private final JoinCodeMatchingUseCase joinCodeMatchingUseCase;
    private final CreateMatchingCodeUseCase createMatchingCodeUseCase;

    /**
     * 랜덤 매칭 시작
     */
    public void startRandomMatching(SessionState session, String nickname) {
        log.info("랜덤 매칭 시작: sessionId={}, nickname={}", session.id(), nickname);

        session.send("🎲 랜덤 매칭을 시작합니다...");
        session.send("닉네임: " + nickname);
        session.send("매칭 중... (최대 10초)");
        session.send("ESC 키를 누르면 취소할 수 있습니다.\n");

        joinRandomMatchingUseCase.joinRandomMatching(
                new JoinRandomMatchingUseCase.JoinRandomMatchingCommand(session.id(), nickname)
        );
    }

    /**
     * 코드 매칭 시작
     */
    public void startCodeMatching(SessionState session, String nickname, String code) {
        log.info("코드 매칭 시작: sessionId={}, nickname={}, code={}", session.id(), nickname, code);

        session.send("🎯 코드 매칭을 시작합니다...");
        session.send("닉네임: " + nickname);
        session.send("매칭 코드: " + code);
        session.send("매칭 중... (최대 10초)");
        session.send("ESC 키를 누르면 취소할 수 있습니다.\n");

        joinCodeMatchingUseCase.joinCodeMatching(
                new JoinCodeMatchingUseCase.JoinCodeMatchingCommand(session.id(), nickname, code)
        );
    }

    /**
     * 매칭 코드 생성 및 표시
     */
    public String createAndDisplayMatchingCode(SessionState session) {
        MatchingCode code = createMatchingCodeUseCase.createMatchingCode();
        log.info("매칭 코드 생성: sessionId={}, code={}", session.id(), code.getCode());

        session.send("\n🎫 ═══════ 매칭 코드 생성 ═══════");
        session.send("");
        session.send("    매칭 코드: " + code.getCode());
        session.send("");
        session.send("이 코드를 친구에게 공유하세요!");
        session.send("코드 유효 시간: 30분");
        session.send("═══════════════════════════════\n");

        return code.getCode();
    }

    /**
     * 매칭 진행 상황 업데이트
     */
    public void updateMatchingProgress(SessionState session, int current, int required) {
        session.send(String.format("⏳ 매칭 진행 중... (%d/%d 명)", current, required));
    }

    /**
     * 매칭 완료 알림
     */
    public void notifyMatchingCompleted(SessionState session) {
        session.send("\n✅ 매칭 완료!");
        session.send("게임 로비로 이동합니다...\n");
    }

    /**
     * 매칭 타임아웃 알림 (AI 투입)
     */
    public void notifyMatchingTimeout(SessionState session, int aiCount) {
        session.send(String.format("\n⏱️  매칭 시간 초과! AI 플레이어 %d명이 투입됩니다.", aiCount));
        session.send("잠시 후 게임이 시작됩니다...\n");
    }
}
