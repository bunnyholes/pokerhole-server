# PokerHole - 텍사스 홀덤 포커 게임 서버

> **Spring Boot 4.x + Java 21** 기반의 멀티플레이어 포커 게임 서버

---

## 프로젝트 개요

**PokerHole**은 Hexagonal Architecture와 Domain-Driven Design 원칙을 따르는 현대적인 포커 게임 서버입니다. 터미널(TCP)과 웹 브라우저(WebSocket)를 통해 접속하여 실시간 멀티플레이어 포커 게임을 즐길 수 있습니다.

### 주요 특징

- **현대적 아키텍처**: Hexagonal Architecture + DDD
- **최신 기술 스택**: Spring Boot 4.0.0-M3, Java 21 (Virtual Threads)
- **멀티 채널 지원**: TCP 터미널, WebSocket
- **AI 플레이어**: 3가지 전략 (보수적/공격적/랜덤)
- **자동 매칭**: 랜덤/코드 매칭, 타임아웃 시 AI 투입
- **테스트 커버리지**: 단위/통합/E2E 테스트

---

## 빠른 시작

### 필수 요구 사항

- **Java 21+** (Virtual Threads 지원)
- **Docker** (PostgreSQL 컨테이너)
- **Gradle 9.x** (포함됨)

### 1. 데이터베이스 시작

```bash
docker compose up -d
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 포트:
- **HTTP**: 8080
- **TCP Terminal**: 7777

### 3. 접속

#### 터미널 접속 (TCP)

```bash
nc localhost 7777
```

#### 웹 브라우저 접속 (WebSocket)

```bash
wscat -c ws://localhost:8080/ws/terminal
```

---

## 게임 플레이

### 게임 흐름

1. **배너 화면** (3초 자동 표시)
2. **메뉴 선택** (↑↓ 화살표 키)
   - 랜덤 매칭
   - 코드 매칭
   - 게임 설명서
   - About
3. **매칭 대기** (최대 10초)
   - 4명 달성 시 즉시 시작
   - 타임아웃 시 AI 플레이어 자동 투입
4. **게임 진행**
   - q키: 나가기 예약
   - c키: 예약 취소
5. **게임 결과** (5초 표시)
6. **자동 재시작**

### 주요 명령어

| 명령어 | 설명 |
|--------|------|
| `HELP` | 도움말 |
| `ROOM LIST` | 방 목록 조회 |
| `ROOM CREATE <방이름> <닉네임>` | 방 생성 |
| `ROOM JOIN <방ID> <닉네임>` | 방 참여 |
| `START` | 게임 시작 (방장) |
| `LEAVE` | 방 나가기 |
| `QUIT` | 서버 연결 종료 |

---

## 설정

### 게임 설정

`application.yml` 파일에서 게임 규칙을 조정할 수 있습니다:

```yaml
pokerhole:
  game:
    initial-chips: 10000
    minimum-bet: 100
    blind-amount: 50
    max-players: 10
    min-players: 2

  matching:
    wait-timeout-seconds: 10
    queue-process-interval-ms: 5000
    players-per-game: 4
    ai-player-enabled: true

  terminal:
    enabled: true
    host: 0.0.0.0
    port: 7777
```

---

## 아키텍처

이 프로젝트는 **Hexagonal Architecture (Ports & Adapters)** 패턴을 따릅니다.

```
core/domain        → 순수 Java 도메인 모델 (외부 의존성 없음)
core/application   → Use Cases & Ports (비즈니스 로직 진입점)
adapter/in         → 입력 어댑터 (Terminal, WebSocket)
adapter/out        → 출력 어댑터 (Persistence, Network, Event)
ui/cli             → 프레젠테이션 레이어
```

상세한 아키텍처 및 구현 계획은 **[PRD.md](./PRD.md)**를 참조하세요.

---

## 개발

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 테스트 커버리지
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html

# 아키텍처 테스트
./gradlew test --tests "*HexagonalArchitectureTest"
```

### 빌드

```bash
./gradlew build
```

---

## 문서

- **[PRD.md](./PRD.md)** - 전체 프로젝트 요구사항 및 구현 계획
  - Phase별 상세 작업 (체크박스로 진행 상황 추적)
  - 도메인 모델 상세 명세
  - Use Case 정의
  - 품질 기준 및 메트릭
  - 향후 로드맵

- **[Claude.MD](./Claude.MD)** - 개발 실행 계획 및 로드맵
  - 현재 상태 요약 및 우선순위
  - Task별 상세 구현 계획 (예상 기간, 복잡도 포함)
  - 주차별 실행 계획
  - 기술적 고려사항 및 품질 기준
  - 알려진 이슈 및 기술 부채

---

## 기술 스택

### 핵심
- Spring Boot 4.0.0-M3
- Java 21 (Virtual Threads)
- Gradle 9.x (Kotlin DSL)

### 주요 라이브러리
- MapStruct, Lombok
- JLine 3.27.1 (터미널 UI)
- Quartz Scheduler
- OpenAPI 3.1 (springdoc)
- Micrometer & Prometheus

### 테스트
- JUnit 5, TestContainers
- ArchUnit, Awaitility, PITest
- AssertJ, Mockito

### 데이터베이스
- PostgreSQL (운영)
- H2 (테스트)

---

## 현재 상태

### 완료 ✅
- Phase 1-5: 인프라, 도메인, 어댑터, UI, 기본 게임 플로우
- 터미널 UI 및 매칭 시스템
- AI 플레이어 (3가지 전략)

### 진행 중 🔄
- Phase 6: 포커 게임 핵심 로직 (핸드 평가, 베팅 라운드)

### 예정 📋
- Phase 7: 테스트 (단위/통합/E2E)
- Phase 8: 고급 기능 (관전, 채팅, 리더보드, 토너먼트)

자세한 진행 상황은 [PRD.md](./PRD.md)의 체크박스를 확인하세요.

---

## 라이선스

MIT License

---

## 기여

이슈 및 PR은 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 연락처

프로젝트 관리자: [@xiyo](https://github.com/xiyo)

프로젝트 링크: [https://github.com/bunnyholes/pokerhole](https://github.com/bunnyholes/pokerhole)
