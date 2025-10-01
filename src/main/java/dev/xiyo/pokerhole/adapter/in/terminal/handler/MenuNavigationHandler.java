package dev.xiyo.pokerhole.adapter.in.terminal.handler;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import dev.xiyo.pokerhole.ui.cli.model.MenuOption;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 메뉴 네비게이션 핸들러
 * 화살표 키로 메뉴 선택
 */
@Slf4j
@Component
public class MenuNavigationHandler {

    @Getter
    private int selectedIndex = 0;

    /**
     * 메뉴 표시
     */
    public void displayMenu(SessionState session) {
        StringBuilder menu = new StringBuilder();
        menu.append("\n📋 ═══════ 메인 메뉴 ═══════\n\n");

        MenuOption[] options = MenuOption.values();
        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == selectedIndex);
            String marker = selected ? "▶ " : "  ";
            String highlight = selected ? "【%s】" : " %s ";

            menu.append(String.format("%s%s %s\n",
                    marker,
                    String.format(highlight, options[i].getDisplay()),
                    options[i].getDescription()
            ));
        }

        menu.append("\n화살표 ↑↓ : 이동 | Enter : 선택 | q : 종료\n");

        session.send(menu.toString());
    }

    /**
     * 위로 이동
     */
    public MenuOption moveUp() {
        MenuOption[] options = MenuOption.values();
        selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        log.debug("메뉴 선택 변경: {}", options[selectedIndex]);
        return options[selectedIndex];
    }

    /**
     * 아래로 이동
     */
    public MenuOption moveDown() {
        MenuOption[] options = MenuOption.values();
        selectedIndex = (selectedIndex + 1) % options.length;
        log.debug("메뉴 선택 변경: {}", options[selectedIndex]);
        return options[selectedIndex];
    }

    /**
     * 현재 선택된 메뉴 확정
     */
    public MenuOption confirm() {
        MenuOption selected = MenuOption.values()[selectedIndex];
        log.info("메뉴 선택 확정: {}", selected);
        return selected;
    }

    /**
     * 선택 인덱스 초기화
     */
    public void reset() {
        selectedIndex = 0;
    }
}
