# PokerHole - Product Requirements Document (PRD)

> **버전**: 2.0
> **최종 업데이트**: 2025-10-02
> **상태**: Phase 5 부분 완료, Phase 6 진행 예정

---

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택 및 아키텍처](#기술-스택-및-아키텍처)
3. [Phase별 상세 구현 계획](#phase별-상세-구현-계획)
4. [도메인 모델 상세 명세](#도메인-모델-상세-명세)
5. [Use Case 정의](#use-case-정의)
6. [품질 기준 및 메트릭](#품질-기준-및-메트릭)
7. [배포 및 운영](#배포-및-운영)
8. [향후 로드맵](#향후-로드맵)

---

## 프로젝트 개요

### 비전

**PokerHole**은 텍사스 홀덤 포커를 터미널과 웹 브라우저에서 즐길 수 있는 멀티플레이어 게임 서버입니다. 현대적인 아키텍처 패턴(Hexagonal Architecture + DDD)과 최신 기술 스택(Spring Boot 4.x, Java 21)을 활용하여 확장 가능하고 유지보수하기 쉬운 게임 플랫폼을 구축합니다.

### 핵심 가치

- **클린 아키텍처**: 비즈니스 로직과 인프라의 명확한 분리
- **도메인 중심**: 포커 게임의 본질에 집중한 도메인 모델
- **확장성**: 새로운 게임 모드, UI, 기능 추가가 용이한 구조
- **테스트 가능성**: 각 레이어가 독립적으로 테스트 가능
- **현대적 기술**: 최신 Java 21 기능 활용 (Virtual Threads, Pattern Matching)

### 주요 기능

#### 완료된 기능 ✅
- 터미널(TCP) 및 WebSocket 접속 지원
- 배너 화면 및 메뉴 시스템
- 랜덤/코드 매칭 시스템
- AI 플레이어 자동 투입 (3가지 전략)
- 게임 세션 라이프사이클 관리
- 기본 라운드 시작/종료 로직

#### 진행 중 기능 🔄
- 포커 핸드 평가 시스템
- 베팅 라운드 관리
- 실제 포커 게임 로직

#### 예정 기능 📋
- 완전한 텍사스 홀덤 룰 구현
- 포괄적인 테스트 커버리지
- 관전 모드
- 채팅 시스템
- 리더보드

---

## 기술 스택 및 아키텍처

### 기술 스택

#### 핵심 프레임워크
- **Spring Boot**: 4.0.0-M3 (최신 밀스톤)
- **Java**: 21 (LTS, Virtual Threads 지원)
- **Gradle**: 9.x (Kotlin DSL)

#### 주요 라이브러리
- **MapStruct**: 객체 매핑 자동화
- **JLine**: 3.27.1 (터미널 UI)
- **Quartz Scheduler**: 백그라운드 작업
- **Problem Details (RFC 7807)**: 표준화된 에러 응답
- **OpenAPI 3.1 (springdoc)**: API 문서 자동 생성
- **Micrometer & Prometheus**: 메트릭 수집
- **Lombok**: 보일러플레이트 코드 제거

#### 테스트 인프라
- **JUnit 5**: 단위 테스트 프레임워크
- **TestContainers**: 실제 DB 통합 테스트
- **ArchUnit**: 아키텍처 규칙 검증
- **Awaitility**: 비동기 작업 테스트
- **PITest**: 뮤테이션 테스트
- **AssertJ**: 유창한 assertion API
- **Mockito**: 모킹 프레임워크

#### 데이터베이스
- **PostgreSQL**: 운영 환경 (Docker Compose)
- **H2**: 테스트 환경 (인메모리)

### 아키텍처 원칙

#### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────┐
│           Adapter (In) - Driving               │
│        Terminal, WebSocket, REST API            │
└──────────────────┬──────────────────────────────┘
                   │ uses
                   ↓
┌─────────────────────────────────────────────────┐
│              Application Layer                  │
│         Use Cases, Ports (in/out)               │
└──────────────────┬──────────────────────────────┘
                   │ uses
                   ↓
┌─────────────────────────────────────────────────┐
│                Domain Layer                     │
│   Card, Player, Game, Room (Pure Java)         │
│        Aggregates, Entities, VOs, Events        │
└──────────────────┬──────────────────────────────┘
                   │ defines needs
                   ↓
┌─────────────────────────────────────────────────┐
│          Adapter (Out) - Driven                │
│   Network, Persistence, Event, Notification     │
└─────────────────────────────────────────────────┘
```

#### 패키지 구조

```
dev.xiyo.pokerhole/
├── core/                      # 🔴 Domain Core (Pure Java)
│   ├── domain/
│   │   ├── card/              # Card, Rank, Suit, Tier, Deck
│   │   ├── player/            # Player aggregate
│   │   │   └── vo/            # PlayerId, Nickname
│   │   ├── game/              # Game domain
│   │   │   ├── event/         # GameEvent, RoundStarted, RoundEnded
│   │   │   └── vo/            # BettingRound, Pot
│   │   ├── matching/          # Matching domain
│   │   │   ├── MatchingRequest, MatchingQueue
│   │   │   └── event/         # MatchingCompleted, MatchingTimeout
│   │   ├── ai/                # AI domain
│   │   │   ├── AIPlayer, AIStrategy
│   │   │   └── AIDecision
│   │   └── shared/            # DomainEvent, AggregateRoot
│   │
│   ├── application/           # Use cases and ports
│   │   ├── UseCase.java       # Custom annotation
│   │   └── port/
│   │       ├── in/            # Input ports (use cases)
│   │       │   ├── game/      # StartRoundUseCase, etc.
│   │       │   ├── matching/  # JoinRandomMatchingUseCase
│   │       │   └── ai/        # RequestAIPlayerUseCase
│   │       └── out/           # Output ports
│   │           ├── game/      # EventPublisher
│   │           ├── matching/  # MatchingQueuePort
│   │           └── ai/        # AIStrategyPort
│   │
│   └── common/
│       └── exception/         # DomainException, GameException
│
├── adapter/                   # 🔵 Adapters
│   ├── in/                    # Input adapters (Driving)
│   │   ├── terminal/          # TCP terminal adapter
│   │   │   ├── state/         # TerminalSessionState
│   │   │   └── handler/       # BannerDisplayHandler, MenuNavigationHandler
│   │   └── web/               # WebSocket adapter
│   │       └── WebSocketTerminalHandler
│   │
│   └── out/                   # Output adapters (Driven)
│       ├── network/           # TCP server, session management
│       ├── persistence/       # JPA entities, repositories
│       │   └── jpa/
│       ├── event/             # SpringEventPublisher
│       ├── matching/          # InMemoryMatchingQueueAdapter
│       └── ai/                # RuleBasedAIStrategy
│
├── ui/                        # 🟢 Presentation
│   ├── cli/                   # CLI components
│   │   ├── render/            # BannerRenderer, MenuRenderer
│   │   ├── input/             # JLineKeyHandler
│   │   └── model/             # MenuOption
│   └── model/                 # View models
│
├── configuration/             # 🟡 Spring Config
│   ├── PropertiesConfig.java
│   ├── MatchingSchedulingConfig.java
│   ├── AIConfig.java
│   └── properties/
│       ├── GameProperties.java
│       ├── MatchingProperties.java
│       └── TerminalGatewayProperties.java
│
├── server/                    # 🔶 Legacy (리팩토링 예정)
│   └── room/                  # Room management
│
└── dealer/                    # 🔶 Legacy (Game aggregate로 전환 예정)
```

#### 의존성 규칙

1. **Domain Layer** (core/domain)
   - 외부 의존성 없음 (Pure Java)
   - 다른 레이어에 의존하지 않음

2. **Application Layer** (core/application)
   - Domain Layer에만 의존
   - Adapter를 알지 못함

3. **Adapter Layer** (adapter)
   - Application과 Domain에 의존
   - Port 인터페이스를 구현

4. **UI Layer** (ui)
   - Application Port를 통해서만 비즈니스 로직 호출
   - Domain 내부 구현을 알지 못함

---

## Phase별 상세 구현 계획

### Phase 1: 인프라 & 아키텍처 기반 ✅ (완료)

**목표**: 프로젝트의 기술적 기반 구축 및 아키텍처 패턴 적용

#### Task 1.1: 빌드 시스템 현대화 ✅

- [x] **Step 1.1.1**: Gradle Kotlin DSL 마이그레이션
  - `build.gradle` → `build.gradle.kts`
  - 타입 안정성 확보
  - 파일: `build.gradle.kts`

- [x] **Step 1.1.2**: Spring Boot 4.0.0-M3 업그레이드
  - Milestone 릴리스 적용
  - Virtual Threads 활성화
  - 의존성 버전 정리

- [x] **Step 1.1.3**: Java 21 Toolchain 설정
  - Java 21 기능 활성화
  - `--enable-preview` 플래그 (필요시)
  - 파일: `build.gradle.kts` 의 `java.toolchain`

- [x] **Step 1.1.4**: Gradle 9.x 업그레이드
  - Gradle Wrapper 업데이트
  - 새로운 API 적용
  - 파일: `gradle/wrapper/gradle-wrapper.properties`

- [x] **Step 1.1.5**: 핵심 의존성 추가
  - MapStruct, JLine, Quartz
  - Problem Details, OpenAPI
  - Micrometer, Prometheus
  - TestContainers, ArchUnit, PITest

#### Task 1.2: Hexagonal Architecture 패키지 구조 ✅

- [x] **Step 1.2.1**: core/domain 패키지 생성
  - Pure Java 도메인 모델 위치
  - 외부 의존성 없음 확인

- [x] **Step 1.2.2**: core/application 패키지 생성
  - Use Case 인터페이스 위치
  - Port 정의 (in/out)

- [x] **Step 1.2.3**: adapter 패키지 구조 생성
  - adapter/in (Driving)
  - adapter/out (Driven)

- [x] **Step 1.2.4**: ui 패키지 분리
  - 프레젠테이션 로직 격리
  - ui/cli, ui/model

- [x] **Step 1.2.5**: configuration 패키지 중앙화
  - Spring 설정 통합
  - Properties 클래스 관리

#### Task 1.3: ArchUnit 아키텍처 테스트 ✅

- [x] **Step 1.3.1**: HexagonalArchitectureTest 생성
  - 파일: `src/test/java/.../architecture/HexagonalArchitectureTest.java`

- [x] **Step 1.3.2**: Domain 격리 규칙 테스트
  - Domain은 외부 의존성 없음
  - `@Test domainLayerShouldNotDependOnOtherLayers()`

- [x] **Step 1.3.3**: Application 레이어 독립성 테스트
  - Application은 Adapter에 의존하지 않음
  - `@Test applicationLayerShouldNotDependOnAdapters()`

- [x] **Step 1.3.4**: 순환 의존성 방지 테스트
  - 패키지 간 순환 참조 금지
  - `@Test noCircularDependencies()`

- [x] **Step 1.3.5**: Naming Convention 테스트
  - Port 인터페이스는 Port 접미사
  - Use Case는 UseCase 접미사

---

### Phase 2: 핵심 도메인 모델 구현 ✅ (완료)

**목표**: Pure Java로 포커 게임의 핵심 도메인 모델 구축

#### Task 2.1: Card 도메인 ✅

- [x] **Step 2.1.1**: Rank enum 정의
  - TWO ~ ACE (13개)
  - ordinal 값으로 비교
  - 파일: `core/domain/card/Rank.java`

- [x] **Step 2.1.2**: Suit enum 정의
  - HEARTS, DIAMONDS, CLUBS, SPADES
  - 파일: `core/domain/card/Suit.java`

- [x] **Step 2.1.3**: Card entity 구현
  - Rank + Suit 조합
  - equals/hashCode 구현
  - 파일: `core/domain/card/Card.java`

- [x] **Step 2.1.4**: Tier enum (카드 순위)
  - 게임별 카드 가치 정의
  - 파일: `core/domain/card/Tier.java`

- [x] **Step 2.1.5**: Deck aggregate 구현
  - 52장 카드 생성
  - shuffle() 메서드
  - deal() 메서드
  - 파일: `core/domain/card/Deck.java`

- [x] **Step 2.1.6**: Hand entity 구현
  - 플레이어가 보유한 카드 목록
  - 파일: `core/domain/card/Hand.java`

#### Task 2.2: Player 도메인 ✅

- [x] **Step 2.2.1**: PlayerId Value Object
  - UUID 기반 식별자
  - 불변 객체 (Lombok @Value)
  - 파일: `core/domain/player/vo/PlayerId.java`

- [x] **Step 2.2.2**: Nickname Value Object
  - 검증 로직 포함 (길이, 특수문자)
  - 불변 객체
  - 파일: `core/domain/player/vo/Nickname.java`

- [x] **Step 2.2.3**: Player aggregate root
  - PlayerId, Nickname 사용
  - chips 필드 (칩 보유량)
  - 파일: `core/domain/player/Player.java`

- [x] **Step 2.2.4**: PlayerRecord entity
  - 플레이어 전적 관리
  - wins, losses, draws
  - 파일: `core/domain/player/PlayerRecord.java`

#### Task 2.3: Game 도메인 (기본) ✅

- [x] **Step 2.3.1**: GameId Value Object
  - UUID 기반
  - 파일: `core/domain/game/vo/GameId.java`

- [x] **Step 2.3.2**: Pot Value Object
  - 불변 팟 금액
  - add() 메서드 (새 Pot 반환)
  - 파일: `core/domain/game/vo/Pot.java`

- [x] **Step 2.3.3**: BettingRound enum
  - PRE_FLOP, FLOP, TURN, RIVER
  - 파일: `core/domain/game/vo/BettingRound.java`

- [x] **Step 2.3.4**: GameState enum
  - WAITING, PLAYING, FINISHED
  - 파일: `core/domain/game/GameState.java`

- [x] **Step 2.3.5**: GameEvent sealed interface
  - RoundStarted, RoundEnded, BettingPhaseStarted
  - 파일: `core/domain/game/event/GameEvent.java`

#### Task 2.4: Matching 도메인 ✅

- [x] **Step 2.4.1**: MatchingType enum
  - RANDOM, CODE
  - 파일: `core/domain/matching/MatchingType.java`

- [x] **Step 2.4.2**: MatchingCode Value Object
  - 6자리 코드 생성
  - 파일: `core/domain/matching/MatchingCode.java`

- [x] **Step 2.4.3**: MatchingRequest entity
  - playerId, type, code, timestamp
  - 파일: `core/domain/matching/MatchingRequest.java`

- [x] **Step 2.4.4**: MatchingQueue aggregate
  - 매칭 요청 큐 관리
  - timeout 검증
  - 파일: `core/domain/matching/MatchingQueue.java`

- [x] **Step 2.4.5**: Matching domain events
  - MatchingCompleted, MatchingTimeout
  - 파일: `core/domain/matching/event/`

#### Task 2.5: AI 도메인 ✅

- [x] **Step 2.5.1**: AIStrategyType enum
  - CONSERVATIVE, AGGRESSIVE, RANDOM
  - 파일: `core/domain/ai/AIStrategyType.java`

- [x] **Step 2.5.2**: AIDecision Value Object
  - action, amount, reason
  - 파일: `core/domain/ai/AIDecision.java`

- [x] **Step 2.5.3**: AIPlayer entity
  - PlayerId 상속
  - strategyType 필드
  - 파일: `core/domain/ai/AIPlayer.java`

- [x] **Step 2.5.4**: AIStrategy interface (도메인)
  - decide() 메서드 정의
  - 파일: `core/domain/ai/AIStrategy.java`

#### Task 2.6: Shared 도메인 추상화 ✅

- [x] **Step 2.6.1**: DomainEvent interface
  - occurredOn() 메서드
  - 파일: `core/domain/shared/DomainEvent.java`

- [x] **Step 2.6.2**: AggregateRoot interface
  - 도메인 이벤트 발행 지원
  - 파일: `core/domain/shared/AggregateRoot.java`

- [x] **Step 2.6.3**: DomainException
  - 도메인 로직 예외 기반 클래스
  - 파일: `core/common/exception/DomainException.java`

- [x] **Step 2.6.4**: GameException
  - 게임 규칙 위반 예외
  - 파일: `core/common/exception/GameException.java`

- [x] **Step 2.6.5**: RoomException
  - 방 관련 예외
  - 파일: `core/common/exception/RoomException.java`

---

### Phase 3: 어댑터 레이어 구현 ✅ (완료)

**목표**: 입출력 어댑터 구현 및 인프라 통합

#### Task 3.1: Input Adapter - Terminal (TCP) ✅

- [x] **Step 3.1.1**: TerminalServer 구현
  - ServerSocket 기반 TCP 서버
  - Virtual Threads 활용
  - 파일: `adapter/in/terminal/TerminalServer.java`

- [x] **Step 3.1.2**: TerminalSessionHandler
  - 클라이언트 세션 관리
  - 명령어 파싱
  - 파일: `adapter/in/terminal/TerminalSessionHandler.java`

- [x] **Step 3.1.3**: CommandProcessor
  - ROOM CREATE, ROOM JOIN, START, LEAVE, QUIT
  - 파일: `adapter/in/terminal/CommandProcessor.java`

- [x] **Step 3.1.4**: TerminalSessionState enum
  - BANNER, MAIN_MENU, MATCHING, GAME_LOBBY, IN_GAME, GAME_RESULT
  - 파일: `adapter/in/terminal/state/TerminalSessionState.java`

- [x] **Step 3.1.5**: TerminalGatewayProperties
  - host, port, enabled 설정
  - 파일: `configuration/properties/TerminalGatewayProperties.java`

#### Task 3.2: Input Adapter - WebSocket ✅

- [x] **Step 3.2.1**: WebSocketConfig
  - WebSocket 엔드포인트 설정
  - /ws/terminal
  - 파일: `adapter/in/web/WebSocketConfig.java`

- [x] **Step 3.2.2**: WebSocketTerminalHandler
  - TextWebSocketHandler 구현
  - 메시지 송수신
  - 파일: `adapter/in/web/WebSocketTerminalHandler.java`

- [x] **Step 3.2.3**: WebSocket 세션 관리
  - 세션별 상태 관리
  - ConcurrentHashMap

#### Task 3.3: Output Adapter - Persistence (JPA) ✅

- [x] **Step 3.3.1**: PlayerJpaEntity
  - JPA 엔티티 정의
  - 파일: `adapter/out/persistence/jpa/PlayerJpaEntity.java`

- [x] **Step 3.3.2**: PlayerJpaRepository
  - Spring Data JPA Repository
  - 파일: `adapter/out/persistence/jpa/PlayerJpaRepository.java`

- [x] **Step 3.3.3**: PlayerPersistenceAdapter
  - Port 구현체
  - Entity ↔ Domain 변환
  - 파일: `adapter/out/persistence/jpa/PlayerPersistenceAdapter.java`

- [x] **Step 3.3.4**: Docker Compose (PostgreSQL)
  - compose.yaml
  - 데이터베이스 초기화

- [x] **Step 3.3.5**: Flyway Migration Scripts
  - V1__create_players_table.sql
  - 파일: `src/main/resources/db/migration/`

#### Task 3.4: Output Adapter - Event ✅

- [x] **Step 3.4.1**: EventPublisher port interface
  - publish(DomainEvent) 메서드
  - 파일: `core/application/port/out/game/EventPublisher.java`

- [x] **Step 3.4.2**: SpringEventPublisher adapter
  - ApplicationEventPublisher 래핑
  - 파일: `adapter/out/event/SpringEventPublisher.java`

- [x] **Step 3.4.3**: Domain Event Listeners
  - @EventListener 사용
  - RoundStartedListener, RoundEndedListener

#### Task 3.5: Output Adapter - Matching ✅

- [x] **Step 3.5.1**: MatchingQueuePort interface
  - add(), remove(), findAll() 메서드
  - 파일: `core/application/port/out/matching/MatchingQueuePort.java`

- [x] **Step 3.5.2**: InMemoryMatchingQueueAdapter
  - ConcurrentHashMap 기반
  - 파일: `adapter/out/matching/InMemoryMatchingQueueAdapter.java`

- [x] **Step 3.5.3**: ScheduledMatchingProcessor
  - @Scheduled(fixedDelay = 5000)
  - 매칭 큐 처리 및 타임아웃 감지
  - 파일: `adapter/out/matching/ScheduledMatchingProcessor.java`

- [x] **Step 3.5.4**: MatchingNotificationPort interface
  - notifyMatchingComplete() 메서드
  - 파일: `core/application/port/out/matching/MatchingNotificationPort.java`

#### Task 3.6: Output Adapter - AI ✅

- [x] **Step 3.6.1**: AIStrategyPort interface
  - decide() 메서드
  - 파일: `core/application/port/out/ai/AIStrategyPort.java`

- [x] **Step 3.6.2**: RuleBasedAIStrategy adapter
  - CONSERVATIVE, AGGRESSIVE, RANDOM 구현
  - 핸드 강도 기반 결정
  - 파일: `adapter/out/ai/RuleBasedAIStrategy.java`

- [x] **Step 3.6.3**: AIConfig
  - AIStrategy 빈 등록
  - 파일: `configuration/AIConfig.java`

#### Task 3.7: Output Adapter - Network ✅

- [x] **Step 3.7.1**: SessionManager
  - 활성 세션 관리
  - 파일: `adapter/out/network/SessionManager.java`

- [x] **Step 3.7.2**: BroadcastService
  - 모든 세션에 메시지 브로드캐스트
  - 파일: `adapter/out/network/BroadcastService.java`

---

### Phase 4: 터미널 UI 및 게임 플로우 ✅ (완료)

**목표**: 터미널 기반 사용자 인터페이스 및 게임 플로우 구현

#### Task 4.1: 배너 시스템 ✅

- [x] **Step 4.1.1**: BannerRenderer
  - ANSI 아트 배너 렌더링
  - 파일: `ui/cli/render/BannerRenderer.java`

- [x] **Step 4.1.2**: BannerDisplayHandler
  - 3초 타이머 (CompletableFuture.delayedExecutor)
  - 자동 메뉴 전환
  - 파일: `adapter/in/terminal/handler/BannerDisplayHandler.java`

- [x] **Step 4.1.3**: ANSI 아트 디자인
  - ASCII 아트 포커 테마
  - 컬러 코드 적용

#### Task 4.2: 메뉴 시스템 ✅

- [x] **Step 4.2.1**: MenuOption enum
  - RANDOM_MATCHING, CODE_MATCHING, GUIDE, ABOUT
  - 파일: `ui/cli/model/MenuOption.java`

- [x] **Step 4.2.2**: MenuRenderer
  - 선택된 항목 하이라이트
  - ANSI 컬러 적용
  - 파일: `ui/cli/render/MenuRenderer.java`

- [x] **Step 4.2.3**: JLineKeyHandler
  - 화살표 키 감지 (↑↓)
  - Enter, q 키 처리
  - 파일: `ui/cli/input/JLineKeyHandler.java`

- [x] **Step 4.2.4**: MenuNavigationHandler
  - 메뉴 상태 관리
  - 선택 처리
  - 파일: `adapter/in/terminal/handler/MenuNavigationHandler.java`

#### Task 4.3: 매칭 시스템 ✅

- [x] **Step 4.3.1**: MatchingHandler
  - 매칭 시작 처리
  - 대기 화면 표시
  - 파일: `adapter/in/terminal/handler/MatchingHandler.java`

- [x] **Step 4.3.2**: MatchingService
  - JoinRandomMatchingUseCase 구현
  - JoinCodeMatchingUseCase 구현
  - CreateMatchingCodeUseCase 구현
  - CancelMatchingUseCase 구현
  - 파일: `core/application/service/MatchingService.java`

- [x] **Step 4.3.3**: Matching Use Case Ports
  - 파일: `core/application/port/in/matching/`

- [x] **Step 4.3.4**: MatchingProperties
  - waitTimeoutSeconds, queueProcessIntervalMs, playersPerGame
  - 파일: `configuration/properties/MatchingProperties.java`

- [x] **Step 4.3.5**: 10초 타임아웃 처리
  - ScheduledMatchingProcessor에서 감지
  - AI 플레이어 자동 투입

#### Task 4.4: AI 플레이어 시스템 ✅

- [x] **Step 4.4.1**: AIPlayerService
  - RequestAIPlayerUseCase 구현
  - AI 플레이어 생성
  - 파일: `core/application/service/AIPlayerService.java`

- [x] **Step 4.4.2**: AI 전략 구현
  - Conservative: 보수적 플레이
  - Aggressive: 공격적 플레이 (30% 블러핑)
  - Random: 예측 불가능 플레이

- [x] **Step 4.4.3**: 핸드 강도 계산 로직
  - 간단한 핸드 평가 (High Card, Pair 등)
  - 팟 오즈 계산

- [x] **Step 4.4.4**: AI 결정 로깅
  - 디버깅용 결정 이유 출력

#### Task 4.5: 게임 세션 라이프사이클 ✅

- [x] **Step 4.5.1**: QuitReservationHandler
  - q키: 나가기 예약
  - c키: 예약 취소
  - ConcurrentHashMap 관리
  - 파일: `adapter/in/terminal/handler/QuitReservationHandler.java`

- [x] **Step 4.5.2**: 게임 결과 표시
  - 5초 타이머
  - 승자, 획득 칩 표시

- [x] **Step 4.5.3**: 자동 재시작 로직
  - GAME_RESULT → GAME_LOBBY 전환
  - 나가기 예약 확인

- [x] **Step 4.5.4**: State Machine 구현
  - BANNER → MAIN_MENU → MATCHING → GAME_LOBBY → IN_GAME → GAME_RESULT

---

### Phase 5: 게임 로직 통합 ✅ (부분 완료)

**목표**: 기본 게임 로직 통합 및 라운드 관리

#### Task 5.1: 라운드 시작 로직 ✅

- [x] **Step 5.1.1**: StartRoundUseCase interface
  - 파일: `core/application/port/in/game/StartRoundUseCase.java`

- [x] **Step 5.1.2**: StartRoundCommand DTO
  - gameId, playerId 검증
  - 파일: `core/application/port/in/game/StartRoundCommand.java`

- [x] **Step 5.1.3**: GameService (StartRoundUseCase 구현)
  - 라운드 시작 오케스트레이션
  - 파일: `core/application/service/GameService.java`

- [x] **Step 5.1.4**: RoundStarted 이벤트 발행
  - EventPublisher 사용

#### Task 5.2: 카드 배분 ✅

- [x] **Step 5.2.1**: Deck shuffle 및 deal
  - Dealer 클래스에서 호출

- [x] **Step 5.2.2**: 각 플레이어에게 2장 배분
  - Hand 업데이트

- [x] **Step 5.2.3**: 카드 배분 브로드캐스트
  - "You have been dealt: [카드]"

#### Task 5.3: 라운드 종료 로직 ✅

- [x] **Step 5.3.1**: RoundEnded 이벤트 발행
  - 승자 정보 포함

- [x] **Step 5.3.2**: 전적 업데이트
  - PlayerRecord 갱신

- [x] **Step 5.3.3**: 결과 브로드캐스트
  - 승자, 획득 칩 표시

#### Task 5.4: Room 관리 (Legacy 통합) 🔄

- [ ] **Step 5.4.1**: Room aggregate 정의
  - RoomId, 플레이어 목록, 상태
  - 파일: `core/domain/room/Room.java` (예정)

- [ ] **Step 5.4.2**: CreateRoomUseCase
  - 파일: `core/application/port/in/room/CreateRoomUseCase.java` (예정)

- [ ] **Step 5.4.3**: JoinRoomUseCase
  - 파일: `core/application/port/in/room/JoinRoomUseCase.java` (예정)

- [ ] **Step 5.4.4**: LeaveRoomUseCase
  - 파일: `core/application/port/in/room/LeaveRoomUseCase.java` (예정)

- [ ] **Step 5.4.5**: RoomRepository Port
  - 파일: `core/application/port/out/room/RoomRepositoryPort.java` (예정)

---

### Phase 6: 포커 게임 핵심 로직 구현 🔄 (진행 예정)

**목표**: 완전한 텍사스 홀덤 포커 규칙 구현

#### Task 6.1: 포커 핸드 평가 시스템

- [ ] **Step 6.1.1**: HandRanking enum 정의
  - HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND
  - STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND
  - STRAIGHT_FLUSH, ROYAL_FLUSH
  - 파일: `core/domain/game/vo/HandRanking.java` (신규)

- [ ] **Step 6.1.2**: HandEvaluator 서비스 인터페이스
  - evaluate(List<Card> playerCards, List<Card> communityCards): HandResult
  - 파일: `core/domain/game/HandEvaluator.java` (신규)

- [ ] **Step 6.1.3**: HandResult Value Object
  - ranking, cards, kickers
  - 파일: `core/domain/game/vo/HandResult.java` (신규)

- [ ] **Step 6.1.4**: 각 핸드 타입 검증 메서드 구현
  - isRoyalFlush(), isStraightFlush(), isFourOfAKind()
  - isFullHouse(), isFlush(), isStraight()
  - isThreeOfAKind(), isTwoPair(), isPair()
  - 파일: `core/domain/game/HandEvaluatorImpl.java` (신규)

- [ ] **Step 6.1.5**: 핸드 비교 로직 (Comparator)
  - HandResult.compareTo() 구현
  - 키커 비교 로직
  - 동점 처리

- [ ] **Step 6.1.6**: HandEvaluator 단위 테스트
  - 모든 핸드 타입별 테스트 케이스
  - 엣지 케이스 (Ace-high/low straight)
  - 파일: `src/test/java/.../game/HandEvaluatorTest.java` (신규)

#### Task 6.2: 베팅 액션 및 검증

- [ ] **Step 6.2.1**: BettingAction enum 정의
  - FOLD, CHECK, CALL, RAISE, ALL_IN
  - 파일: `core/domain/game/vo/BettingAction.java` (신규)

- [ ] **Step 6.2.2**: BettingActionCommand DTO
  - playerId, action, amount
  - 검증 어노테이션
  - 파일: `core/application/port/in/game/BettingActionCommand.java` (신규)

- [ ] **Step 6.2.3**: PlaceBetUseCase interface
  - execute(BettingActionCommand)
  - 파일: `core/application/port/in/game/PlaceBetUseCase.java` (신규)

- [ ] **Step 6.2.4**: BettingValidator 도메인 서비스
  - 최소/최대 베팅 금액 검증
  - CHECK 가능 여부 검증
  - RAISE 금액 검증 (이전 베팅의 2배 이상)
  - 파일: `core/domain/game/BettingValidator.java` (신규)

- [ ] **Step 6.2.5**: BettingException
  - 베팅 규칙 위반 예외
  - INSUFFICIENT_CHIPS, INVALID_AMOUNT, INVALID_ACTION
  - 파일: `core/common/exception/BettingException.java` (신규)

- [ ] **Step 6.2.6**: BettingValidator 단위 테스트
  - 각 액션별 검증 로직 테스트
  - 파일: `src/test/java/.../game/BettingValidatorTest.java` (신규)

#### Task 6.3: 팟 관리 (메인팟 & 사이드팟)

- [ ] **Step 6.3.1**: Pot Value Object 확장
  - 현재: 단순 금액
  - 확장: 참여 플레이어 목록, 사이드팟 여부
  - 파일: `core/domain/game/vo/Pot.java` (수정)

- [ ] **Step 6.3.2**: PotManager 도메인 서비스
  - addBet(PlayerId, Amount)
  - createSidePot(List<PlayerId>, Amount)
  - distributePots(Map<PlayerId, HandResult>)
  - 파일: `core/domain/game/PotManager.java` (신규)

- [ ] **Step 6.3.3**: SidePot Value Object
  - amount, eligiblePlayers
  - 파일: `core/domain/game/vo/SidePot.java` (신규)

- [ ] **Step 6.3.4**: 올인 시 사이드팟 생성 로직
  - ALL_IN 액션 처리
  - 남은 플레이어들의 베팅 분리

- [ ] **Step 6.3.5**: 팟 분배 로직
  - 승자 결정 후 팟 금액 분배
  - 동점 시 팟 분할
  - 사이드팟 각각 분배

- [ ] **Step 6.3.6**: PotManager 단위 테스트
  - 메인팟만 있는 경우
  - 사이드팟 생성 케이스
  - 복잡한 멀티 사이드팟
  - 파일: `src/test/java/.../game/PotManagerTest.java` (신규)

#### Task 6.4: 베팅 라운드 관리

- [ ] **Step 6.4.1**: BettingRoundManager 도메인 서비스
  - 현재 베팅 라운드 상태 관리
  - 차례 순서 관리 (Dealer Button 기준)
  - 파일: `core/domain/game/BettingRoundManager.java` (신규)

- [ ] **Step 6.4.2**: PlayerPosition enum
  - DEALER, SMALL_BLIND, BIG_BLIND, EARLY, MIDDLE, LATE
  - 파일: `core/domain/game/vo/PlayerPosition.java` (신규)

- [ ] **Step 6.4.3**: 베팅 라운드 진행 로직
  - startBettingRound(BettingRound)
  - nextPlayer(): PlayerId
  - isBettingRoundComplete(): boolean
  - 파일: `BettingRoundManager.java`

- [ ] **Step 6.4.4**: 블라인드 처리
  - Small Blind, Big Blind 자동 배치
  - GameProperties에서 금액 읽기

- [ ] **Step 6.4.5**: 베팅 라운드 완료 조건
  - 모든 플레이어가 동일 금액 베팅
  - 또는 한 명 제외 모두 폴드

- [ ] **Step 6.4.6**: BettingRoundManager 단위 테스트
  - 차례 순서 테스트
  - 블라인드 배치 테스트
  - 라운드 완료 조건 테스트
  - 파일: `src/test/java/.../game/BettingRoundManagerTest.java` (신규)

#### Task 6.5: 커뮤니티 카드 관리

- [ ] **Step 6.5.1**: CommunityCards Value Object
  - List<Card> 래핑
  - 불변 객체
  - 파일: `core/domain/game/vo/CommunityCards.java` (신규)

- [ ] **Step 6.5.2**: revealFlop() 메서드
  - Deck에서 3장 공개
  - BettingRound.FLOP 전환

- [ ] **Step 6.5.3**: revealTurn() 메서드
  - Deck에서 1장 공개
  - BettingRound.TURN 전환

- [ ] **Step 6.5.4**: revealRiver() 메서드
  - Deck에서 1장 공개
  - BettingRound.RIVER 전환

- [ ] **Step 6.5.5**: 커뮤니티 카드 브로드캐스트
  - "Flop: [카드1] [카드2] [카드3]"
  - "Turn: [카드]"
  - "River: [카드]"

#### Task 6.6: 게임 라운드 오케스트레이션

- [ ] **Step 6.6.1**: RoundOrchestrator 도메인 서비스
  - 라운드 전체 플로우 조율
  - 파일: `core/domain/game/RoundOrchestrator.java` (신규)

- [ ] **Step 6.6.2**: startRound() 메서드
  - 덱 셔플
  - 플레이어별 2장 배분
  - 블라인드 배치
  - Pre-flop 베팅 라운드 시작

- [ ] **Step 6.6.3**: proceedToNextPhase() 메서드
  - PRE_FLOP → FLOP (3장 공개)
  - FLOP → TURN (1장 공개)
  - TURN → RIVER (1장 공개)
  - RIVER → SHOWDOWN (승자 판정)

- [ ] **Step 6.6.4**: handleBettingAction() 메서드
  - 플레이어 액션 처리
  - Pot 업데이트
  - 다음 플레이어 전환
  - 베팅 라운드 완료 확인

- [ ] **Step 6.6.5**: determineWinner() 메서드
  - 모든 플레이어의 핸드 평가
  - HandResult 비교
  - 팟 분배
  - RoundEnded 이벤트 발행

- [ ] **Step 6.6.6**: handleFoldedPlayers() 메서드
  - 모두 폴드 시 즉시 종료
  - 남은 플레이어에게 팟 지급

- [ ] **Step 6.6.7**: RoundOrchestrator 통합 테스트
  - 전체 라운드 플로우 테스트
  - 파일: `src/test/java/.../game/RoundOrchestratorIntegrationTest.java` (신규)

#### Task 6.7: 플레이어 액션 처리 및 타임아웃

- [ ] **Step 6.7.1**: PlayerActionHandler
  - 플레이어 입력 수신
  - 파일: `adapter/in/terminal/handler/PlayerActionHandler.java` (신규)

- [ ] **Step 6.7.2**: 액션 입력 파싱
  - "FOLD", "CHECK", "CALL", "RAISE 200", "ALL_IN"
  - 대소문자 무시

- [ ] **Step 6.7.3**: 타임아웃 처리
  - GameProperties.roundTimeoutSeconds
  - 타임아웃 시 자동 FOLD
  - CompletableFuture.orTimeout() 사용

- [ ] **Step 6.7.4**: 액션 브로드캐스트
  - "Player [닉네임] folds"
  - "Player [닉네임] calls 100"
  - "Player [닉네임] raises to 200"

- [ ] **Step 6.7.5**: AI 플레이어 자동 액션
  - AIStrategy.decide() 호출
  - 결정 이유 로깅

#### Task 6.8: Game Aggregate 추출 (Dealer 리팩토링)

- [ ] **Step 6.8.1**: Game Aggregate Root 정의
  - GameId, 플레이어 목록, Deck, Pot, BettingRound
  - 파일: `core/domain/game/Game.java` (신규)

- [ ] **Step 6.8.2**: Dealer 클래스에서 Game으로 로직 이동
  - 현재 Dealer에 있는 게임 로직을 Game aggregate로
  - Dealer는 단순히 카드 배분 역할로 축소

- [ ] **Step 6.8.3**: GameRepository Port
  - save(), findById(), findAll()
  - 파일: `core/application/port/out/game/GameRepositoryPort.java` (신규)

- [ ] **Step 6.8.4**: GamePersistenceAdapter
  - Game aggregate 영속화
  - 파일: `adapter/out/persistence/jpa/GamePersistenceAdapter.java` (신규)

- [ ] **Step 6.8.5**: Game aggregate 단위 테스트
  - 모든 게임 규칙 테스트
  - 파일: `src/test/java/.../game/GameTest.java` (신규)

---

### Phase 7: 테스트 및 품질 보증 📋 (예정)

**목표**: 포괄적인 테스트 커버리지 및 품질 검증

#### Task 7.1: 단위 테스트 (Domain & Application)

- [ ] **Step 7.1.1**: Card 도메인 테스트
  - Rank, Suit, Card 테스트
  - 파일: `src/test/java/.../card/CardTest.java`

- [ ] **Step 7.1.2**: Deck 테스트
  - shuffle, deal 로직 검증
  - 파일: `src/test/java/.../card/DeckTest.java`

- [ ] **Step 7.1.3**: Player 도메인 테스트
  - PlayerId, Nickname validation
  - 파일: `src/test/java/.../player/PlayerTest.java`

- [ ] **Step 7.1.4**: Pot 테스트
  - 불변성, add 연산
  - 파일: `src/test/java/.../game/vo/PotTest.java`

- [ ] **Step 7.1.5**: HandEvaluator 테스트 (Task 6.1.6에서 작성)
  - 모든 핸드 타입 검증

- [ ] **Step 7.1.6**: BettingValidator 테스트 (Task 6.2.6에서 작성)
  - 베팅 규칙 검증

- [ ] **Step 7.1.7**: PotManager 테스트 (Task 6.3.6에서 작성)
  - 사이드팟 로직

- [ ] **Step 7.1.8**: BettingRoundManager 테스트 (Task 6.4.6에서 작성)
  - 베팅 라운드 진행

- [ ] **Step 7.1.9**: MatchingQueue 테스트
  - 매칭 큐 로직, 타임아웃
  - 파일: `src/test/java/.../matching/MatchingQueueTest.java`

- [ ] **Step 7.1.10**: AIStrategy 테스트
  - 각 전략별 결정 로직
  - 파일: `src/test/java/.../ai/AIStrategyTest.java`

- [ ] **Step 7.1.11**: Use Case 테스트
  - StartRoundUseCase, PlaceBetUseCase 등
  - Mockito로 Port 모킹
  - 파일: `src/test/java/.../application/service/`

#### Task 7.2: 통합 테스트 (Adapter & Infrastructure)

- [ ] **Step 7.2.1**: MatchingIntegrationTest
  - 10초 타임아웃 → AI 투입 검증
  - Awaitility 사용
  - 파일: `src/test/java/.../matching/MatchingIntegrationTest.java`

- [ ] **Step 7.2.2**: GameSessionLifecycleTest
  - 전체 게임 플로우 통합 테스트
  - BANNER → MENU → MATCHING → GAME → RESULT
  - 파일: `src/test/java/.../terminal/GameSessionLifecycleTest.java`

- [ ] **Step 7.2.3**: TerminalCommandIntegrationTest
  - 터미널 명령어 처리 통합 테스트
  - ROOM CREATE, JOIN, START, LEAVE
  - 파일: `src/test/java/.../terminal/TerminalCommandIntegrationTest.java`

- [ ] **Step 7.2.4**: PersistenceIntegrationTest
  - TestContainers + PostgreSQL
  - Player, Game 영속화 테스트
  - 파일: `src/test/java/.../persistence/PersistenceIntegrationTest.java`

- [ ] **Step 7.2.5**: WebSocketIntegrationTest
  - WebSocket 연결 및 메시지 송수신
  - 파일: `src/test/java/.../web/WebSocketIntegrationTest.java`

- [ ] **Step 7.2.6**: EventPublishingIntegrationTest
  - Domain Event 발행 및 수신 검증
  - 파일: `src/test/java/.../event/EventPublishingIntegrationTest.java`

#### Task 7.3: E2E 테스트

- [ ] **Step 7.3.1**: FullGameE2ETest
  - 실제 서버 기동
  - nc 명령으로 접속 시뮬레이션
  - 게임 완료까지 플로우 검증
  - 파일: `src/test/java/.../e2e/FullGameE2ETest.java`

- [ ] **Step 7.3.2**: MultiPlayerE2ETest
  - 4명 플레이어 동시 접속
  - 실제 게임 진행
  - 승자 판정 검증

- [ ] **Step 7.3.3**: AIPlayerE2ETest
  - AI 플레이어 포함 게임
  - AI 결정 로직 검증

#### Task 7.4: 아키텍처 테스트 확장

- [ ] **Step 7.4.1**: Matching/AI 도메인 의존성 테스트
  - Matching domain은 외부 의존 없음
  - AI domain은 외부 의존 없음
  - 파일: `src/test/java/.../architecture/DomainDependencyTest.java`

- [ ] **Step 7.4.2**: Use Case 네이밍 테스트
  - UseCase 인터페이스는 "UseCase" 접미사
  - 파일: `src/test/java/.../architecture/NamingConventionTest.java`

- [ ] **Step 7.4.3**: Port 네이밍 테스트
  - Port 인터페이스는 "Port" 접미사

- [ ] **Step 7.4.4**: Adapter 의존성 테스트
  - Adapter는 Application, Domain에만 의존

#### Task 7.5: 성능 테스트

- [ ] **Step 7.5.1**: 동시 접속 테스트
  - 100명 동시 접속 시뮬레이션
  - Virtual Threads 효과 검증

- [ ] **Step 7.5.2**: 매칭 처리 성능 테스트
  - 대규모 매칭 큐 처리 시간 측정

- [ ] **Step 7.5.3**: 핸드 평가 성능 테스트
  - 10,000번 핸드 평가 시간 측정
  - 병목 지점 식별

#### Task 7.6: 뮤테이션 테스트 (PITest)

- [ ] **Step 7.6.1**: PITest 설정
  - build.gradle.kts에 플러그인 추가
  - 타겟 패키지 설정

- [ ] **Step 7.6.2**: 뮤테이션 테스트 실행
  - `./gradlew pitest`
  - 커버리지 확인

- [ ] **Step 7.6.3**: Mutation Score 목표
  - Domain Layer: > 80%
  - Application Layer: > 70%

#### Task 7.7: 테스트 커버리지 측정

- [ ] **Step 7.7.1**: JaCoCo 설정
  - build.gradle.kts에 플러그인 추가

- [ ] **Step 7.7.2**: 커버리지 리포트 생성
  - `./gradlew test jacocoTestReport`

- [ ] **Step 7.7.3**: 커버리지 목표
  - Domain Layer: > 90%
  - Application Layer: > 85%
  - Adapter Layer: > 70%

---

### Phase 8: 고급 기능 및 최적화 📋 (예정)

**목표**: 사용자 경험 개선 및 고급 기능 추가

#### Task 8.1: 관전 모드

- [ ] **Step 8.1.1**: Spectator Value Object
  - spectatorId, nickname
  - 파일: `core/domain/game/vo/Spectator.java`

- [ ] **Step 8.1.2**: JoinAsSpectatorUseCase
  - 진행 중인 게임 관전
  - 파일: `core/application/port/in/game/JoinAsSpectatorUseCase.java`

- [ ] **Step 8.1.3**: 관전자 전용 브로드캐스트
  - 플레이어의 카드는 숨김
  - 베팅 액션만 표시

- [ ] **Step 8.1.4**: 관전자 목록 관리
  - Game aggregate에 spectators 필드

- [ ] **Step 8.1.5**: 관전자 UI
  - 터미널에서 관전 화면 렌더링

#### Task 8.2: 채팅 시스템

- [ ] **Step 8.2.1**: ChatMessage Value Object
  - senderId, content, timestamp
  - 파일: `core/domain/chat/ChatMessage.java`

- [ ] **Step 8.2.2**: SendChatMessageUseCase
  - 파일: `core/application/port/in/chat/SendChatMessageUseCase.java`

- [ ] **Step 8.2.3**: ChatRepository Port
  - 파일: `core/application/port/out/chat/ChatRepositoryPort.java`

- [ ] **Step 8.2.4**: 채팅 메시지 브로드캐스트
  - 게임 참여자 및 관전자에게 전송

- [ ] **Step 8.2.5**: 채팅 금지어 필터링
  - 비속어, 스팸 방지

- [ ] **Step 8.2.6**: 채팅 UI
  - 터미널에서 채팅 입력/표시

#### Task 8.3: 리더보드 및 통계

- [ ] **Step 8.3.1**: Leaderboard aggregate
  - 전체 플레이어 순위
  - 파일: `core/domain/leaderboard/Leaderboard.java`

- [ ] **Step 8.3.2**: PlayerStatistics Value Object
  - totalGames, wins, losses, winRate, totalChips
  - 파일: `core/domain/player/vo/PlayerStatistics.java`

- [ ] **Step 8.3.3**: GetLeaderboardUseCase
  - 파일: `core/application/port/in/leaderboard/GetLeaderboardUseCase.java`

- [ ] **Step 8.3.4**: LeaderboardRepository Port
  - 파일: `core/application/port/out/leaderboard/LeaderboardRepositoryPort.java`

- [ ] **Step 8.3.5**: 리더보드 UI
  - 터미널에서 순위 표시

- [ ] **Step 8.3.6**: 개인 통계 조회
  - "STATS" 명령어

#### Task 8.4: 토너먼트 모드

- [ ] **Step 8.4.1**: Tournament aggregate
  - 토너먼트 ID, 참가자, 라운드 구조
  - 파일: `core/domain/tournament/Tournament.java`

- [ ] **Step 8.4.2**: CreateTournamentUseCase
  - 파일: `core/application/port/in/tournament/CreateTournamentUseCase.java`

- [ ] **Step 8.4.3**: JoinTournamentUseCase
  - 파일: `core/application/port/in/tournament/JoinTournamentUseCase.java`

- [ ] **Step 8.4.4**: 토너먼트 브라켓 생성
  - 싱글 엘리미네이션, 더블 엘리미네이션

- [ ] **Step 8.4.5**: 토너먼트 진행 오케스트레이션
  - 라운드별 게임 생성
  - 승자 다음 라운드 진출

#### Task 8.5: 성능 최적화

- [ ] **Step 8.5.1**: 캐싱 전략
  - Caffeine Cache 도입
  - Leaderboard 캐싱

- [ ] **Step 8.5.2**: DB 쿼리 최적화
  - N+1 문제 해결
  - 인덱스 추가

- [ ] **Step 8.5.3**: 메모리 사용량 최적화
  - 불필요한 객체 생성 제거

- [ ] **Step 8.5.4**: Virtual Threads 활용 극대화
  - 블로킹 I/O를 Virtual Threads로

#### Task 8.6: 보안 강화

- [ ] **Step 8.6.1**: Rate Limiting
  - 플레이어별 명령어 제한
  - Bucket4j 사용

- [ ] **Step 8.6.2**: 입력 검증 강화
  - XSS, SQL Injection 방지

- [ ] **Step 8.6.3**: 세션 타임아웃
  - 일정 시간 비활성 시 자동 로그아웃

- [ ] **Step 8.6.4**: 암호화
  - 민감 정보 암호화 (비밀번호 등)

#### Task 8.7: 모니터링 및 로깅

- [ ] **Step 8.7.1**: Prometheus 메트릭 추가
  - 동시 접속자 수
  - 평균 게임 시간
  - 매칭 대기 시간

- [ ] **Step 8.7.2**: Grafana 대시보드
  - 실시간 메트릭 시각화

- [ ] **Step 8.7.3**: 구조화된 로깅
  - JSON 로그 포맷
  - ELK Stack 연동

- [ ] **Step 8.7.4**: Alert 설정
  - 에러율 임계값 초과 시 알림

---

## 도메인 모델 상세 명세

### Aggregates

#### 1. Player Aggregate

**Root Entity**: Player

**구성 요소**:
- PlayerId (Value Object)
- Nickname (Value Object)
- chips (long) - 보유 칩
- PlayerRecord (Entity) - 전적

**비즈니스 규칙**:
- Nickname은 3~20자, 영문/숫자/밑줄만 허용
- chips는 0 이상이어야 함
- 칩이 0이 되면 게임 참여 불가

**책임**:
- 칩 증감 관리
- 전적 업데이트

#### 2. Game Aggregate (Phase 6에서 구현 예정)

**Root Entity**: Game

**구성 요소**:
- GameId (Value Object)
- List<Player> players
- Deck deck
- CommunityCards (Value Object)
- Pot (Value Object)
- BettingRound (Value Object)
- GameState (Enum)

**비즈니스 규칙**:
- 최소 2명, 최대 10명 플레이어
- 게임 시작 시 모든 플레이어는 충분한 칩 보유
- 각 베팅 라운드는 순차적으로 진행

**책임**:
- 라운드 시작/종료
- 베팅 액션 처리
- 승자 판정 및 팟 분배

#### 3. Room Aggregate (Phase 5.4에서 구현 예정)

**Root Entity**: Room

**구성 요소**:
- RoomId (Value Object)
- name (String)
- List<Player> players
- hostPlayerId (PlayerId)
- RoomState (Enum: WAITING, PLAYING)

**비즈니스 규칙**:
- 방장만 게임 시작 가능
- 방장 퇴장 시 다음 플레이어에게 방장 승계
- 최대 인원 초과 시 입장 불가

**책임**:
- 플레이어 입장/퇴장 관리
- 방장 권한 관리

#### 4. MatchingQueue Aggregate

**Root Entity**: MatchingQueue

**구성 요소**:
- List<MatchingRequest> requests
- MatchingType (Enum)
- createdAt (Instant)

**비즈니스 규칙**:
- 10초 타임아웃 초과 시 AI 투입
- 4명 달성 시 게임 생성

**책임**:
- 매칭 요청 관리
- 타임아웃 감지

### Entities

#### 1. Card
- Rank + Suit 조합
- 불변 객체

#### 2. PlayerRecord
- Player에 종속
- wins, losses, draws

#### 3. AIPlayer
- Player 상속
- AIStrategyType 필드

### Value Objects

#### 1. PlayerId
- UUID 래핑
- 불변

#### 2. Nickname
- String 래핑
- 검증 로직 (3~20자, 영숫자/밑줄)

#### 3. Pot
- long amount
- add(amount) → 새 Pot 반환

#### 4. BettingRound
- PRE_FLOP, FLOP, TURN, RIVER

#### 5. GameId
- UUID 래핑

#### 6. MatchingCode
- 6자리 코드

#### 7. AIDecision
- BettingAction, amount, reason

#### 8. HandRanking (Phase 6)
- HIGH_CARD ~ ROYAL_FLUSH

#### 9. HandResult (Phase 6)
- HandRanking, List<Card>, kickers

#### 10. CommunityCards (Phase 6)
- List<Card> 래핑

#### 11. SidePot (Phase 6)
- amount, eligiblePlayers

### Domain Events

#### 1. GameEvent (Sealed Interface)
- RoundStarted
- RoundEnded
- BettingPhaseStarted

#### 2. MatchingEvent
- MatchingCompleted
- MatchingTimeout

---

## Use Case 정의

### Input Ports (Use Cases)

#### Game Use Cases

1. **StartRoundUseCase**
   - 입력: StartRoundCommand (gameId, playerId)
   - 출력: RoundId
   - 설명: 새로운 게임 라운드 시작

2. **PlaceBetUseCase** (Phase 6)
   - 입력: BettingActionCommand (gameId, playerId, action, amount)
   - 출력: void
   - 설명: 플레이어 베팅 액션 처리

3. **FoldUseCase** (Phase 6)
   - 입력: FoldCommand (gameId, playerId)
   - 출력: void
   - 설명: 플레이어 폴드

4. **JoinAsSpectatorUseCase** (Phase 8)
   - 입력: JoinSpectatorCommand (gameId, spectatorId)
   - 출력: void
   - 설명: 관전 모드로 게임 참여

#### Room Use Cases (Phase 5.4)

1. **CreateRoomUseCase**
   - 입력: CreateRoomCommand (name, hostPlayerId)
   - 출력: RoomId
   - 설명: 새로운 방 생성

2. **JoinRoomUseCase**
   - 입력: JoinRoomCommand (roomId, playerId)
   - 출력: void
   - 설명: 기존 방 참여

3. **LeaveRoomUseCase**
   - 입력: LeaveRoomCommand (roomId, playerId)
   - 출력: void
   - 설명: 방 퇴장

#### Matching Use Cases

1. **JoinRandomMatchingUseCase**
   - 입력: JoinRandomMatchingCommand (playerId)
   - 출력: MatchingRequestId
   - 설명: 랜덤 매칭 참여

2. **JoinCodeMatchingUseCase**
   - 입력: JoinCodeMatchingCommand (playerId, code)
   - 출력: MatchingRequestId
   - 설명: 코드 매칭 참여

3. **CreateMatchingCodeUseCase**
   - 입력: CreateMatchingCodeCommand (playerId)
   - 출력: MatchingCode
   - 설명: 매칭 코드 생성

4. **CancelMatchingUseCase**
   - 입력: CancelMatchingCommand (playerId)
   - 출력: void
   - 설명: 매칭 취소

#### AI Use Cases

1. **RequestAIPlayerUseCase**
   - 입력: RequestAIPlayerCommand (strategyType)
   - 출력: AIPlayer
   - 설명: AI 플레이어 생성

#### Leaderboard Use Cases (Phase 8)

1. **GetLeaderboardUseCase**
   - 입력: GetLeaderboardQuery (limit)
   - 출력: List<PlayerStatistics>
   - 설명: 리더보드 조회

#### Chat Use Cases (Phase 8)

1. **SendChatMessageUseCase**
   - 입력: SendChatMessageCommand (senderId, content)
   - 출력: void
   - 설명: 채팅 메시지 전송

### Output Ports (Repository & External Services)

#### Game Ports

1. **EventPublisher**
   - publish(DomainEvent)
   - 설명: 도메인 이벤트 발행

2. **GameRepositoryPort** (Phase 6)
   - save(Game)
   - findById(GameId): Optional<Game>
   - findAll(): List<Game>

#### Room Ports (Phase 5.4)

1. **RoomRepositoryPort**
   - save(Room)
   - findById(RoomId): Optional<Room>
   - findAll(): List<Room>

#### Matching Ports

1. **MatchingQueuePort**
   - add(MatchingRequest)
   - remove(MatchingRequest)
   - findAll(): List<MatchingRequest>
   - findByType(MatchingType): List<MatchingRequest>

2. **MatchingNotificationPort**
   - notifyMatchingComplete(List<PlayerId>, GameId)
   - notifyMatchingTimeout(PlayerId)

#### AI Ports

1. **AIStrategyPort**
   - decide(AIPlayer, GameContext): AIDecision

#### Player Ports

1. **PlayerRepositoryPort**
   - save(Player)
   - findById(PlayerId): Optional<Player>
   - findAll(): List<Player>

#### Leaderboard Ports (Phase 8)

1. **LeaderboardRepositoryPort**
   - getTopPlayers(limit): List<PlayerStatistics>

#### Chat Ports (Phase 8)

1. **ChatRepositoryPort**
   - save(ChatMessage)
   - findRecent(limit): List<ChatMessage>

---

## 품질 기준 및 메트릭

### 테스트 커버리지 목표

- **Domain Layer**: > 90%
- **Application Layer**: > 85%
- **Adapter Layer**: > 70%
- **전체 평균**: > 80%

### Mutation Score 목표

- **Domain Layer**: > 80%
- **Application Layer**: > 70%

### 아키텍처 규칙

- Domain은 외부 의존성 없음 (Pure Java)
- Application은 Adapter에 의존하지 않음
- 순환 의존성 없음

### 성능 기준

- **동시 접속자**: 100명 이상 지원
- **매칭 처리 시간**: 평균 < 1초
- **핸드 평가 시간**: < 10ms
- **응답 시간**: p95 < 500ms

### 보안 기준

- **입력 검증**: 모든 사용자 입력 검증
- **Rate Limiting**: 플레이어당 초당 10 요청
- **세션 타임아웃**: 30분 비활성 시 자동 로그아웃

### 코드 품질

- **Lombok 사용**: 보일러플레이트 코드 최소화
- **Naming Convention**: 명확하고 일관된 네이밍
- **SonarQube**: Critical 이슈 0건

---

## 배포 및 운영

### 환경 구성

#### 개발 환경

- **데이터베이스**: H2 인메모리
- **로깅 레벨**: DEBUG
- **포트**: 8080 (HTTP), 7777 (TCP Terminal)

#### 테스트 환경

- **데이터베이스**: TestContainers + PostgreSQL
- **로깅 레벨**: INFO
- **포트**: 동적 할당

#### 운영 환경

- **데이터베이스**: PostgreSQL (Docker Compose)
- **로깅 레벨**: WARN
- **포트**: 설정 가능
- **메트릭**: Prometheus + Grafana

### Docker Compose 설정

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: pokerhole
      POSTGRES_USER: pokerhole
      POSTGRES_PASSWORD: pokerhole
    ports:
      - "5432:5432"

  app:
    build: .
    ports:
      - "8080:8080"
      - "7777:7777"
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/pokerhole
```

### CI/CD 파이프라인 (예정)

1. **Build**
   - `./gradlew compileJava`
   - `./gradlew compileTestJava`

2. **Test**
   - `./gradlew test`
   - `./gradlew jacocoTestReport`
   - `./gradlew pitest`

3. **Quality Check**
   - SonarQube 분석
   - ArchUnit 테스트

4. **Package**
   - `./gradlew bootJar`

5. **Deploy**
   - Docker 이미지 빌드
   - Kubernetes 배포

### 모니터링 (Phase 8)

- **메트릭**: Micrometer + Prometheus
- **시각화**: Grafana
- **로깅**: Logback + ELK Stack
- **알림**: Alertmanager

---

## 향후 로드맵

### v1.0 (Phase 1-7 완료)

- 완전한 텍사스 홀덤 포커 규칙 구현
- 터미널 및 WebSocket 지원
- 랜덤/코드 매칭 시스템
- AI 플레이어 (3가지 전략)
- 포괄적인 테스트 커버리지

### v1.1 (Phase 8 일부)

- 관전 모드
- 채팅 시스템
- 리더보드 및 통계

### v1.2

- 토너먼트 모드
- 다양한 게임 모드 (오마하, 7-Card Stud)

### v2.0

- REST API 추가
- React 기반 웹 UI
- 모바일 앱 (React Native)
- 소셜 기능 (친구 추가, 초대)
- 인앱 구매 (칩 판매)

### v2.1

- 머신러닝 기반 AI 플레이어
- 실시간 통계 분석
- 플레이 스타일 분석

### v3.0

- Multi-Region 지원
- 대규모 토너먼트 (1000명 이상)
- VR/AR 지원

---

## 부록

### 용어 정의

- **Hexagonal Architecture**: 포트와 어댑터 패턴, 비즈니스 로직을 중심에 두고 외부 시스템과의 연결을 어댑터로 추상화
- **DDD (Domain-Driven Design)**: 도메인 중심 설계, 비즈니스 로직을 도메인 모델로 표현
- **Aggregate**: DDD에서 일관성 경계를 가진 엔티티와 값 객체의 클러스터
- **Value Object**: 식별자가 없고 불변인 객체
- **Domain Event**: 도메인에서 발생한 중요한 사건
- **Use Case**: 사용자의 목표를 달성하기 위한 시스템의 행동
- **Port**: 애플리케이션과 외부 세계 간의 인터페이스
- **Adapter**: Port를 구현하여 외부 시스템과 연결

### 참고 자료

- [Spring Boot 4.x 공식 문서](https://spring.io/projects/spring-boot)
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [TestContainers 공식 문서](https://www.testcontainers.org/)
- [텍사스 홀덤 규칙](https://www.pokernews.com/poker-rules/texas-holdem.htm)

---

**문서 끝**

이 PRD는 PokerHole 프로젝트의 전체 구현 계획을 담고 있습니다. 각 Phase의 체크박스를 통해 진행 상황을 추적하고, 상세한 Step별 가이드를 통해 체계적인 개발을 진행할 수 있습니다.
