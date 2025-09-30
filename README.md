# 🃏 PokerHole 네트워크 서버 v2.0

Spring Boot 4.x 기반의 멀티플레이어 포커 게임 서버입니다. Hexagonal Architecture와 DDD 원칙을 따르며, 표준 터미널(TCP 텍스트 클라이언트)과 브라우저(WebSocket 터미널)를 통해 접속해 방을 만들고 게임을 진행할 수 있습니다.

## 🚀 새로운 기능 (v2.0)

### 아키텍처 개선
- **Hexagonal Architecture (Ports & Adapters)** - 명확한 계층 분리
- **Domain-Driven Design (DDD)** - 도메인 중심 설계
- **Value Objects** - 불변 도메인 객체 (PlayerId, Nickname, Pot, BettingRound)
- **Domain Events** - 이벤트 기반 아키텍처 (RoundStarted, RoundEnded 등)
- **Use Case Pattern** - 명확한 비즈니스 로직 진입점

### 기술 스택 업그레이드
- **Spring Boot 4.0.0-M3** - 최신 밀스톤 릴리스
- **Java 21** - Virtual Threads 지원
- **Gradle 9.x** - Kotlin DSL 빌드 스크립트
- **MapStruct** - 자동 객체 매핑
- **Quartz Scheduler** - 백그라운드 작업 스케줄링
- **OpenAPI 3.1** - API 문서 자동 생성
- **Micrometer & Prometheus** - 메트릭 수집

### 테스트 인프라
- **ArchUnit** - 아키텍처 규칙 검증
- **TestContainers** - 실제 데이터베이스 통합 테스트
- **Awaitility** - 비동기 작업 테스트
- **PITest** - 뮤테이션 테스트

## 환경 요구 사항
- Java 21+ (Virtual Thread 지원)
- Gradle 9.x (랩퍼 포함)
- Docker (PostgreSQL 컨테이너용)

## 핵심 기능
- 기존 `Dealer`, `Player`, `Hand` 도메인 로직을 그대로 활용하는 게임 방 구조
- 방장 생성/참가/퇴장 및 자동 방장 승계
- 방장에 의한 라운드 시작과 라운드 결과 브로드캐스트
- 텍스트 기반 명령 프로토콜 공유 (TCP 터미널 & 웹소켓)
- **이벤트 기반 게임 로직** - 도메인 이벤트를 통한 느슨한 결합
- **설정 가능한 게임 규칙** - YAML 설정으로 게임 파라미터 조정

## 실행 방법
### 데이터베이스 준비 (PostgreSQL)
Spring Boot Docker Compose 연동이 기본으로 포함되어 있습니다. 루트의 `compose.yaml`만 유지하면 애플리케이션이 기동될 때 PostgreSQL 컨테이너가 자동으로 올라오고 접속 정보도 주입됩니다.

1. Docker Desktop 또는 호환 런타임이 실행 중인지 확인합니다.
2. `./gradlew bootRun` 또는 패키징된 JAR 실행과 함께 서버를 기동하면 됩니다.

별도 설정 없이 `pokerhole/pokerhole` 계정과 `pokerhole` 데이터베이스가 자동 생성됩니다. 호스트 포트는 Docker가 매번 랜덤으로 할당하므로 `docker compose ps` 명령으로 확인할 수 있습니다.

```bash
./gradlew bootRun
```

기본 HTTP 포트는 `8080`, 터미널 접속용 TCP 포트는 `7777`이며 `application.yml` 또는 실행 인자를 통해 비활성화/주소/포트를 모두 조정할 수 있습니다.

```bash
./gradlew bootRun --args='--server.port=9090 --pokerhole.terminal.enabled=true --pokerhole.terminal.host=127.0.0.1 --pokerhole.terminal.port=9000'
```

### 개발 모드로 실행
개발용 설정을 사용하려면 `dev` 프로파일을 활성화하세요:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## ⚙️ 설정 (Configuration)

### 게임 설정 (Game Properties)
`application.yml`에서 게임 규칙을 설정할 수 있습니다:

```yaml
pokerhole:
  game:
    initial-chips: 10000          # 플레이어 초기 칩
    minimum-bet: 100              # 최소 베팅 금액
    blind-amount: 50              # 블라인드 금액
    max-players: 10               # 최대 플레이어 수
    min-players: 2                # 최소 플레이어 수 (게임 시작 조건)
    round-timeout-seconds: 60     # 라운드 타임아웃
```

### 매칭 설정 (Matching Properties)
자동 매칭 및 AI 플레이어 관련 설정:

```yaml
pokerhole:
  matching:
    auto-matching-enabled: true         # 자동 매칭 활성화
    wait-timeout-seconds: 30            # 대기 타임아웃 (AI 투입 시점)
    queue-process-interval-ms: 5000     # 큐 처리 주기
    players-per-game: 4                 # 게임당 플레이어 수
    ai-player-enabled: true             # AI 플레이어 투입 활성화
    code-matching-enabled: true         # 코드 매칭 활성화
    code-expiration-minutes: 30         # 매칭 코드 유효 시간
```

### 터미널 설정 (Terminal Properties)
TCP 터미널 서버 설정:

```yaml
pokerhole:
  terminal:
    enabled: true     # 터미널 서버 활성화
    host: 0.0.0.0     # 바인딩 호스트
    port: 7777        # 터미널 포트
```

## 터미널 접속 예시
### 1. `nc`/`ncat` 활용 (macOS, Linux 기본 제공)
```bash
nc localhost 7777
```
또는 TLS가 필요한 경우 (예시)
```bash
ncat --ssl localhost 7777
```

### 2. PowerShell (Windows 기본 제공)
```powershell
powershell -Command "
  $client = New-Object System.Net.Sockets.TcpClient('localhost',7777);
  $stream = $client.GetStream();
  $writer = New-Object System.IO.StreamWriter($stream); $writer.AutoFlush = $true;
  $reader = New-Object System.IO.StreamReader($stream);
  while ($true) {
    if ($Host.UI.RawUI.KeyAvailable) { $line = Read-Host; $writer.WriteLine($line) }
    if ($stream.DataAvailable) { Write-Host ($reader.ReadLine()) }
    Start-Sleep -Milliseconds 50
  }
"
```

연결이 되면 다음과 같은 명령을 사용할 수 있습니다.

| 명령 | 설명 |
| --- | --- |
| `HELP` | 사용 가능한 명령어 안내 |
| `ROOM LIST` | 현재 생성된 방 목록 조회 |
| `ROOM CREATE <방이름> <닉네임>` | 방을 만들고 방장으로 입장 |
| `ROOM JOIN <방ID> <닉네임>` | 기존 방에 참가 |
| `START` | 방장이 라운드를 시작 |
| `LEAVE` | 현재 방에서 퇴장 |
| `QUIT` | 서버 연결 종료 |

라운드를 시작하면 덱 셔플 → 카드 배분 → 패 공개 → 승자 판정 → 전적 브로드캐스트가 순차적으로 출력됩니다.

## 브라우저(WebSocket) 터미널
`/ws/terminal` 엔드포인트에 WebSocket으로 연결하면 동일한 텍스트 프로토콜을 사용할 수 있습니다. 예를 들어 `wscat`을 이용하면 다음과 같습니다.

```bash
wscat -c ws://localhost:8080/ws/terminal
```

브라우저에서 직접 접속하려면 `xterm.js` 등 터미널 UI 컴포넌트로 해당 WebSocket 엔드포인트를 바인딩하면 됩니다.

## 📐 아키텍처

이 프로젝트는 **Hexagonal Architecture (Ports & Adapters)** 와 **Domain-Driven Design (DDD)** 원칙을 따릅니다.

### 패키지 구조
```
dev.xiyo.pokerhole/
├── core/                      # 🔴 Domain Core (Pure Java)
│   ├── domain/                # 도메인 모델
│   │   ├── card/              # 카드 엔티티
│   │   ├── player/            # 플레이어 aggregate & VOs
│   │   ├── game/              # 게임 도메인 & 이벤트
│   │   └── shared/            # 공유 추상화
│   └── application/           # 유스케이스 & 포트
│       ├── UseCase.java       # 커스텀 애노테이션
│       └── port/              # 입출력 포트
│
├── adapter/                   # 🔵 Adapters
│   ├── in/                    # 입력 어댑터
│   │   ├── terminal/          # TCP 터미널
│   │   └── web/               # WebSocket
│   └── out/                   # 출력 어댑터
│       ├── network/           # TCP, 세션
│       ├── persistence/       # JPA
│       └── event/             # 이벤트 발행
│
├── ui/                        # 🟢 Presentation
│   ├── cli/                   # CLI 컴포넌트
│   └── model/                 # 뷰 모델
│
└── configuration/             # 🟡 Spring Config
    └── properties/            # 설정 속성
```

자세한 내용은 [ARCHITECTURE.md](ARCHITECTURE.md)를 참조하세요.

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 아키텍처 테스트
ArchUnit을 사용한 아키텍처 규칙 검증:
```bash
./gradlew test --tests "*HexagonalArchitectureTest"
```

### 테스트 커버리지 확인
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 🎯 주요 기능 상세

### 도메인 이벤트
게임 진행 중 발생하는 주요 이벤트:
- `RoundStarted` - 라운드 시작
- `RoundEnded` - 라운드 종료 (승자 정보 포함)
- `BettingPhaseStarted` - 베팅 페이즈 시작

### Value Objects
도메인 모델의 불변 객체:
- `PlayerId` - 플레이어 식별자
- `Nickname` - 검증된 닉네임
- `Pot` - 팟 금액
- `BettingRound` - 베팅 라운드 단계

## 패키지 구조 (Legacy)
- `dev.xiyo.pokerhole.server.room`: 방 생성 및 게임 라운드 관리
- `dev.xiyo.pokerhole.server.session`: 접속 세션 추상화
- `dev.xiyo.pokerhole.server.terminal`: TCP 터미널 서버 및 명령 처리기 (Java 21 Virtual Thread 활용)
- `dev.xiyo.pokerhole.server.websocket`: WebSocket 터미널 핸들러

## 테스트 실행
```bash
./gradlew test
```

테스트는 내장 H2 데이터베이스를 자동으로 사용하므로 추가 설정이 필요 없습니다.

## 라이선스
MIT License (프로젝트 루트의 LICENSE 파일 참고)
