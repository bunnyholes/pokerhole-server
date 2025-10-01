package dev.xiyo.pokerhole.ui.cli.model;

import lombok.Getter;

/**
 * 메인 메뉴 옵션
 */
@Getter
public enum MenuOption {
    RANDOM_MATCHING("랜덤 매칭", "빠른 게임 시작"),
    CODE_MATCHING("코드 매칭", "친구와 함께 플레이"),
    MANUAL("게임 설명서", "포커 규칙 보기"),
    ABOUT("어바웃", "프로젝트 정보");

    private final String display;
    private final String description;

    MenuOption(String display, String description) {
        this.display = display;
        this.description = description;
    }
}
