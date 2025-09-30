package dev.xiyo.pokerhole.core.domain.player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * 플레이어 프로필
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerProfile {
    @NonNull
    private final String nickname;
    private final boolean isBot;
    
    public static PlayerProfile of(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다");
        }
        if (nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 20자 이하여야 합니다");
        }
        return new PlayerProfile(nickname, false);
    }
    
    public static PlayerProfile bot(String nickname) {
        return new PlayerProfile(nickname, true);
    }
}
