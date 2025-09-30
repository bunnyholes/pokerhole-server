package dev.xiyo.pokerhole.core.domain.betting;

/**
 * 텍사스 홀덤의 베팅 라운드
 */
public enum BettingRound {
    PRE_FLOP,   // 카드 배분 후 첫 베팅
    FLOP,       // 커뮤니티 카드 3장 공개 후
    TURN,       // 커뮤니티 카드 4번째 공개 후
    RIVER,      // 커뮤니티 카드 5번째 공개 후
    SHOWDOWN    // 최종 패 공개
}
