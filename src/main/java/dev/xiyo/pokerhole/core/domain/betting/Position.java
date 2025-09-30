package dev.xiyo.pokerhole.core.domain.betting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 포지션 (블라인드 결정)
 */
@Getter
@RequiredArgsConstructor
public enum Position {
    SMALL_BLIND(10),     // 스몰 블라인드 10원
    BIG_BLIND(20),       // 빅 블라인드 20원
    EARLY(0),
    MIDDLE(0),
    LATE(0),
    BUTTON(0);           // 딜러 버튼
    
    private final long forcedBet;
}
