package dev.xiyo.pokerhole.ui.cli.render;

import dev.xiyo.pokerhole.ui.cli.model.MenuOption;
import org.springframework.stereotype.Component;

/**
 * 메뉴 렌더러
 * 메인 메뉴 UI 생성
 */
@Component
public class MenuRenderer {

    public String renderMenu(int selectedIndex) {
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

        return menu.toString();
    }

    public String renderManual() {
        return """

                📖 ═══════ 게임 설명서 ═══════

                【텍사스 홀덤 포커 규칙】

                1. 게임 시작
                   - 4명의 플레이어로 시작
                   - 각 플레이어는 10,000 칩 보유

                2. 카드 배분
                   - 각 플레이어에게 2장의 카드 배분
                   - 5장의 커뮤니티 카드 공개

                3. 베팅 라운드
                   - 폴드: 게임 포기
                   - 체크: 베팅 없이 패스
                   - 콜: 현재 베팅 금액 맞추기
                   - 레이즈: 베팅 금액 올리기

                4. 승자 결정
                   - 가장 강한 패를 가진 플레이어 승리
                   - 팟(Pot) 금액 획득

                【패 순위 (강함 → 약함)】
                1. 스트레이트 플러시
                2. 포카드
                3. 풀하우스
                4. 플러시
                5. 스트레이트
                6. 트리플
                7. 투 페어
                8. 원 페어
                9. 하이 카드

                ESC 키로 메뉴로 돌아가기
                """;
    }

    public String renderAbout() {
        return """

                ℹ️  ═══════ 어바웃 ═══════

                【PokerHole v2.0】

                🎯 프로젝트 정보
                   - 이름: PokerHole
                   - 버전: 2.0.0
                   - 장르: 멀티플레이어 텍사스 홀덤 포커

                ⚙️  기술 스택
                   - Spring Boot 4.0.0-M3
                   - Java 21 (Virtual Threads)
                   - JLine 3.27.1 (Terminal UI)
                   - Lombok (Code Simplification)

                🏗️  아키텍처
                   - Hexagonal Architecture
                   - Domain-Driven Design
                   - Event-Driven Architecture

                🎮 주요 기능
                   - 랜덤 매칭 (10초 이내)
                   - 코드 매칭 (친구 초대)
                   - AI 플레이어 자동 투입
                   - 화살표 키 네비게이션

                👥 개발팀
                   - BunnyHoles Team
                   - GitHub: github.com/bunnyholes/pokerhole

                📜 라이선스
                   - MIT License

                ESC 키로 메뉴로 돌아가기
                """;
    }
}
