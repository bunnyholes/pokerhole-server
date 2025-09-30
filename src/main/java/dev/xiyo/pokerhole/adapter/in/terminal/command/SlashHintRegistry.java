package dev.xiyo.pokerhole.adapter.in.terminal.command;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * SSH 터미널 클라이언트에서 사용할 수 있는 기본 힌트 문구를 관리한다.
 */
@Component
public class SlashHintRegistry {

    private final Map<String, List<String>> hints = new LinkedHashMap<>();

    public SlashHintRegistry() {
        register("HELP", List.of(
                "HELP : 사용 가능한 명령을 확인합니다.",
                "HELP <명령어> : 특정 명령어의 세부 도움말을 확인합니다."));
        register("ROOM LIST", List.of(
                "ROOM LIST : 현재 생성된 방 목록을 확인합니다."));
        register("ROOM CREATE", List.of(
                "ROOM CREATE <방이름> <닉네임> : 새 방을 만들고 방장으로 입장합니다.",
                "방 이름에는 공백이 없는 영문/숫자를 사용해주세요."));
        register("ROOM JOIN", List.of(
                "ROOM JOIN <방ID> <닉네임> : 기존 방에 참가합니다.",
                "ROOM LIST 로 확인한 방 ID 를 입력해야 합니다."));
        register("START", List.of(
                "START : 방장이 게임을 시작합니다.",
                "최소 2명 이상이 참가하고 있어야 합니다."));
        register("LEAVE", List.of(
                "LEAVE : 현재 참여 중인 방에서 나갑니다."));
        register("QUIT", List.of(
                "QUIT : 서버 연결을 종료합니다."));
    }

    public void register(String key, List<String> hintLines) {
        hints.put(normalize(key), List.copyOf(hintLines));
    }

    public Optional<List<String>> findHint(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(rawKey);
        List<String> lines = hints.get(normalized);
        if (lines != null) {
            return Optional.of(lines);
        }

        // 공백을 제거한 단일 키가 등록되어 있는 경우에도 검색한다.
        String compact = normalized.replace(" ", "");
        return Optional.ofNullable(hints.get(compact)).map(Collections::unmodifiableList);
    }

    private String normalize(String key) {
        return key.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
