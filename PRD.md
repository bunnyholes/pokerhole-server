# PokerHole - Product Requirements Document (PRD)

> **버전**: 3.0
> **최종 업데이트**: 2025-10-02
> **상태**: Phase 5 부분 완료, Phase 6 진행 예정
> **예상 완료 일정**: 2026년 Q4 (v2.0 기준)

---

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택 및 아키텍처](#기술-스택-및-아키텍처)
3. [개발 규칙](#개발-규칙)
4. [Phase별 상세 구현 계획](#phase별-상세-구현-계획)
5. [도메인 모델 상세 명세](#도메인-모델-상세-명세)
6. [Use Case 정의](#use-case-정의)
7. [품질 기준 및 메트릭](#품질-기준-및-메트릭)
8. [배포 및 운영](#배포-및-운영)
9. [향후 로드맵](#향후-로드맵)
10. [리스크 관리](#리스크-관리)

---

## 프로젝트 개요

### 비전

PokerHole은 텍사스 홀덤 포커를 터미널과 웹 브라우저에서 즐길 수 있는 멀티플레이어 게임 플랫폼입니다. Hexagonal Architecture와 Domain-Driven Design 원칙을 따르며, 최신 기술 스택(Spring Boot 4.x, Java 21)을 활용하여 확장 가능하고 유지보수하기 쉬운 게임 플랫폼을 구축합니다.

### 핵심 가치

- **클린 아키텍처**: 비즈니스 로직과 인프라의 명확한 분리
- **도메인 중심**: 포커 게임의 본질에 집중한 도메인 모델
- **확장성**: 새로운 게임 모드, UI, 기능 추가가 용이한 구조
- **테스트 가능성**: 각 레이어가 독립적으로 테스트 가능
- **현대적 기술**: Java 21 기능 활용 (Virtual Threads, Pattern Matching, Sealed Classes)

### 주요 기능

#### 완료된 기능
- 터미널(TCP) 및 WebSocket 접속 지원
- 배너 화면 및 메뉴 시스템
- 랜덤/코드 매칭 시스템
- AI 플레이어 자동 투입 (3가지 전략: Conservative, Aggressive, Random)
- 게임 세션 라이프사이클 관리
- 기본 라운드 시작/종료 로직
- Hexagonal Architecture 기반 패키지 구조
- ArchUnit 아키텍처 테스트

#### 진행 중 기능
- 포커 핸드 평가 시스템
- 베팅 라운드 관리
- 실제 포커 게임 로직

#### 예정 기능
- 완전한 텍사스 홀덤 룰 구현
- 포괄적인 테스트 커버리지 (목표 85% 이상)
- 관전 모드
- 채팅 시스템
- 리더보드 및 토너먼트

---

## 기술 스택 및 아키텍처

### 기술 스택

#### 핵심 프레임워크
- **Spring Boot**: 4.0.0-M3
- **Java**: 21 (LTS, Virtual Threads 지원)
- **Gradle**: 9.x (Kotlin DSL)

#### 주요 라이브러리
- **MapStruct**: 1.6.3 (객체 매핑)
- **JLine**: 3.27.1 (터미널 UI)
- **Quartz Scheduler**: 백그라운드 작업
- **Problem Details (RFC 7807)**: 표준화된 에러 응답
- **OpenAPI 3.1 (springdoc)**: API 문서 자동 생성
- **Micrometer & Prometheus**: 메트릭 수집
- **Lombok**: 1.18.34 (보일러플레이트 코드 제거)

#### 테스트 인프라
- **JUnit 5**: 단위 테스트 프레임워크
- **TestContainers**: 실제 DB 통합 테스트
- **ArchUnit**: 아키텍처 규칙 검증
- **Awaitility**: 비동기 작업 테스트
- **PITest**: 뮤테이션 테스트
- **AssertJ**: assertion API
- **Mockito**: 모킹 프레임워크

#### 데이터베이스
- **PostgreSQL**: 16.x (운영 환경, Docker Compose)
- **H2**: 2.3.232 (테스트 환경, 인메모리)

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
├── core/
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
│   │   ├── mapper/            # MapStruct 매퍼 (Application <-> Domain)
│   │   │   ├── PlayerMapper.java
│   │   │   ├── GameMapper.java
│   │   │   └── MatchingMapper.java
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
├── adapter/
│   ├── in/                    # Input adapters
│   │   ├── terminal/          # TCP terminal adapter
│   │   │   ├── state/         # TerminalSessionState
│   │   │   └── handler/       # BannerDisplayHandler, MenuNavigationHandler
│   │   └── web/               # WebSocket adapter
│   │       ├── WebSocketTerminalHandler
│   │       └── mapper/        # Request/Response DTO 매퍼
│   │
│   └── out/                   # Output adapters
│       ├── network/           # TCP server, session management
│       ├── persistence/       # JPA entities, repositories
│       │   ├── jpa/
│       │   │   ├── entity/    # PlayerJpaEntity, GameJpaEntity
│       │   │   ├── repository/ # Spring Data JPA Repositories
│       │   │   └── adapter/   # PersistenceAdapter 구현체
│       │   └── mapper/        # JPA Entity <-> Domain 매퍼 (MapStruct)
│       │       ├── PlayerEntityMapper.java
│       │       ├── GameEntityMapper.java
│       │       └── RoomEntityMapper.java
│       ├── event/             # SpringEventPublisher
│       ├── matching/          # InMemoryMatchingQueueAdapter
│       └── ai/                # RuleBasedAIStrategy
│
├── ui/
│   ├── cli/                   # CLI components
│   │   ├── render/            # BannerRenderer, MenuRenderer
│   │   ├── input/             # JLineKeyHandler
│   │   └── model/             # MenuOption
│   └── model/                 # View models (DTO)
│
├── configuration/
│   ├── PropertiesConfig.java
│   ├── MatchingSchedulingConfig.java
│   ├── AIConfig.java
│   ├── MapperConfig.java      # MapStruct 설정
│   └── properties/
│       ├── GameProperties.java
│       ├── MatchingProperties.java
│       └── TerminalGatewayProperties.java
│
├── server/                    # Legacy (리팩토링 예정)
│   └── room/
│
└── dealer/                    # Legacy (Game aggregate로 전환 예정)
```

#### 의존성 규칙

1. **Domain Layer** (core/domain)
   - 외부 의존성 없음 (Pure Java)
   - 다른 레이어에 의존하지 않음
   - MapStruct, Lombok 제외한 모든 라이브러리 금지

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

## 개발 규칙

### 객체 매핑 규칙

모든 객체 간 변환은 MapStruct를 사용합니다.

#### 매퍼 위치

1. **Persistence 매퍼**: `adapter/out/persistence/mapper/`
   - JPA Entity <-> Domain 변환
   - 예: PlayerEntityMapper, GameEntityMapper

2. **Application 매퍼**: `core/application/mapper/`
   - Domain <-> DTO 변환
   - 예: PlayerMapper, GameMapper

3. **Input Adapter 매퍼**: `adapter/in/web/mapper/`
   - Request/Response <-> Command 변환
   - 예: GameRequestMapper, PlayerRequestMapper

#### 매퍼 작성 규칙

```java
// 올바른 예
@Mapper(componentModel = "spring")
public interface PlayerMapper {
    Player toDomain(PlayerJpaEntity entity);
    PlayerJpaEntity toEntity(Player domain);
    List<Player> toDomainList(List<PlayerJpaEntity> entities);
}

// 잘못된 예 - componentModel 누락
@Mapper
public interface PlayerMapper {
    Player map(PlayerJpaEntity entity);
}
```

#### build.gradle.kts 설정

```kotlin
dependencies {
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
}
```

#### MapperConfig 설정

```java
@Configuration
@ComponentScan(basePackages = {
    "dev.xiyo.pokerhole.core.application.mapper",
    "dev.xiyo.pokerhole.adapter.out.persistence.mapper",
    "dev.xiyo.pokerhole.adapter.in.web.mapper"
})
public class MapperConfig {
    // MapStruct 매퍼 Bean 자동 등록
}
```

### 네이밍 규칙

- **Use Case 인터페이스**: `UseCase` 접미사 필수
- **Port 인터페이스**: `Port` 접미사 필수
- **Domain Event**: `Event` 접미사 또는 과거형 동사
- **Value Object**: 명사 또는 명사구
- **Aggregate Root**: 도메인 엔티티명

### 테스트 규칙

- **단위 테스트**: 모든 도메인 로직 및 Use Case 필수
- **통합 테스트**: Adapter 구현체 필수
- **아키텍처 테스트**: ArchUnit으로 의존성 규칙 검증
- **매퍼 테스트**: 모든 MapStruct 매퍼에 대해 필드 매핑 검증

---

## Phase별 상세 구현 계획

### Phase 1: 인프라 & 아키텍처 기반 (완료)

**기간**: 2025-01 ~ 2025-02
**목표**: 프로젝트의 기술적 기반 구축 및 아키텍처 패턴 적용

- [x] Gradle Kotlin DSL 마이그레이션
- [x] Spring Boot 4.0.0-M3 업그레이드
- [x] Java 21 Toolchain 설정
- [x] Hexagonal Architecture 패키지 구조
- [x] ArchUnit 아키텍처 테스트

---

### Phase 2: 핵심 도메인 모델 구현 (완료)

**기간**: 2025-03 ~ 2025-04
**목표**: Pure Java로 포커 게임의 핵심 도메인 모델 구축

- [x] Card 도메인 (Rank, Suit, Card, Tier, Deck, Hand)
- [x] Player 도메인 (PlayerId, Nickname, Player, PlayerRecord)
- [x] Game 도메인 기본 (GameId, Pot, BettingRound, GameState, GameEvent)
- [x] Matching 도메인 (MatchingType, MatchingCode, MatchingRequest, MatchingQueue)
- [x] AI 도메인 (AIStrategyType, AIDecision, AIPlayer, AIStrategy)
- [x] Shared 도메인 추상화 (DomainEvent, AggregateRoot, 예외 클래스)

---

### Phase 3: 어댑터 레이어 구현 (완료)

**기간**: 2025-05 ~ 2025-06
**목표**: 입출력 어댑터 구현 및 인프라 통합

- [x] Terminal (TCP), WebSocket 어댑터
- [x] JPA Persistence 어댑터
- [x] Event, Matching, AI, Network 어댑터

---

### Phase 4: 터미널 UI 및 게임 플로우 (완료)

**기간**: 2025-07 ~ 2025-08
**목표**: 터미널 기반 사용자 인터페이스 및 게임 플로우 구현

- [x] 배너 시스템
- [x] 메뉴 시스템
- [x] 매칭 시스템
- [x] AI 플레이어 시스템
- [x] 게임 세션 라이프사이클

---

### Phase 5: 게임 로직 통합 (부분 완료)

**기간**: 2025-09 ~ 2025-10
**목표**: 기본 게임 로직 통합 및 라운드 관리

- [x] 라운드 시작 로직
- [x] 카드 배분
- [x] 라운드 종료 로직
- [ ] Room 관리 (Legacy 통합)

---

### Phase 6: 포커 게임 핵심 로직 구현 (진행 예정)

**기간**: 2025-11 ~ 2026-03 (6개월)
**목표**: 완전한 텍사스 홀덤 포커 규칙 구현
**예상 투입 인력**: 백엔드 개발자 2명

#### Task 6.1: 포커 핸드 평가 시스템 (2주)

- [ ] HandRanking enum (HIGH_CARD ~ ROYAL_FLUSH)
- [ ] HandEvaluator 서비스 인터페이스
- [ ] HandResult Value Object
- [ ] 각 핸드 타입 검증 메서드 (isRoyalFlush, isStraightFlush 등)
- [ ] 핸드 비교 로직 및 키커 처리
- [ ] 단위 테스트 100개 이상

#### Task 6.2: 베팅 액션 및 검증 (2주)

- [ ] BettingAction enum (FOLD, CHECK, CALL, RAISE, ALL_IN)
- [ ] BettingActionCommand DTO
- [ ] PlaceBetUseCase
- [ ] BettingValidator 도메인 서비스
- [ ] BettingException
- [ ] 단위 테스트 50개 이상

#### Task 6.3: 팟 관리 (메인팟 & 사이드팟) (3주)

- [ ] Pot Value Object 확장
- [ ] PotManager 도메인 서비스
- [ ] SidePot Value Object
- [ ] 올인 시 사이드팟 생성 로직
- [ ] 팟 분배 로직
- [ ] 단위 테스트 70개 이상

#### Task 6.4: 베팅 라운드 관리 (2주)

- [ ] BettingRoundManager 도메인 서비스
- [ ] PlayerPosition enum
- [ ] 베팅 라운드 진행 로직
- [ ] 블라인드 처리
- [ ] 베팅 라운드 완료 조건
- [ ] 단위 테스트 40개 이상

#### Task 6.5: 커뮤니티 카드 관리 (1주)

- [ ] CommunityCards Value Object
- [ ] revealFlop, revealTurn, revealRiver 메서드
- [ ] 커뮤니티 카드 브로드캐스트

#### Task 6.6: 게임 라운드 오케스트레이션 (4주)

- [ ] RoundOrchestrator 도메인 서비스
- [ ] startRound 메서드
- [ ] proceedToNextPhase 메서드
- [ ] handleBettingAction 메서드
- [ ] determineWinner 메서드
- [ ] handleFoldedPlayers 메서드
- [ ] 통합 테스트 30개 이상 시나리오

#### Task 6.7: 플레이어 액션 처리 및 타임아웃 (2주)

- [ ] PlayerActionHandler
- [ ] 액션 입력 파싱
- [ ] 타임아웃 처리
- [ ] 액션 브로드캐스트
- [ ] AI 플레이어 자동 액션

#### Task 6.8: Game Aggregate 추출 (3주)

- [ ] Game Aggregate Root 정의
- [ ] Dealer 클래스 리팩토링
- [ ] GameRepository Port
- [ ] GamePersistenceAdapter (MapStruct 사용)
- [ ] Game aggregate 단위 테스트 80개 이상

#### Task 6.9: MapStruct 매퍼 통합 (2주)

- [ ] Persistence 매퍼 구현 (PlayerEntityMapper, GameEntityMapper, RoomEntityMapper)
- [ ] Application 매퍼 구현 (PlayerMapper, GameMapper, MatchingMapper)
- [ ] Input Adapter 매퍼 구현 (GameRequestMapper, PlayerRequestMapper)
- [ ] MapperConfig 설정
- [ ] 매퍼 단위 테스트 (각 매퍼당 10개)

---

### Phase 7: 테스트 및 품질 보증 (예정)

**기간**: 2026-04 ~ 2026-06 (3개월)
**목표**: 포괄적인 테스트 커버리지 및 품질 검증
**예상 투입 인력**: QA 1명, 백엔드 개발자 2명

#### Task 7.1: 단위 테스트 (4주)

**목표 커버리지**:
- Domain Layer: 95% 이상
- Application Layer: 90% 이상

**예상 테스트 수**: 500개 이상

#### Task 7.2: 통합 테스트 (4주)

- [ ] MatchingIntegrationTest
- [ ] GameSessionLifecycleTest
- [ ] PersistenceIntegrationTest (TestContainers)
- [ ] WebSocketIntegrationTest
- [ ] EventPublishingIntegrationTest

**예상 테스트 수**: 100개 이상

#### Task 7.3: E2E 테스트 (2주)

- [ ] FullGameE2ETest
- [ ] MultiPlayerE2ETest
- [ ] AIPlayerE2ETest

**예상 테스트 수**: 30개 이상

#### Task 7.4: 아키텍처 테스트 확장 (1주)

- [ ] Matching/AI 도메인 의존성 테스트
- [ ] Use Case/Port 네이밍 테스트
- [ ] Adapter 의존성 테스트

#### Task 7.5: 성능 테스트 (2주)

**성능 목표**:
- 동시 접속자: 100명 이상
- 매칭 처리 시간: 평균 1초 미만
- 핸드 평가 시간: 10ms 미만
- 응답 시간: p95 500ms 미만

**도구**: JMeter, Gatling

#### Task 7.6: 뮤테이션 테스트 (1주)

**목표 Mutation Score**:
- Domain Layer: 80% 이상
- Application Layer: 70% 이상

**도구**: PITest

#### Task 7.7: 테스트 커버리지 측정 (1주)

- JaCoCo 리포트 생성
- SonarQube 통합

---

### Phase 8: 고급 기능 및 최적화 (예정)

**기간**: 2026-07 ~ 2026-12 (6개월)
**목표**: 사용자 경험 개선 및 고급 기능 추가
**예상 투입 인력**: 백엔드 2명, 프론트엔드 1명, 디자이너 1명

#### Task 8.1: 관전 모드 (2주)
#### Task 8.2: 채팅 시스템 (3주)
#### Task 8.3: 리더보드 및 통계 (3주)
#### Task 8.4: 토너먼트 모드 (6주)
#### Task 8.5: 성능 최적화 (3주)
#### Task 8.6: 보안 강화 (2주)
#### Task 8.7: 모니터링 및 로깅 (2주)

---

## 도메인 모델 상세 명세

### Aggregates

#### 1. Player Aggregate

**Root Entity**: Player

**구성 요소**:
- PlayerId (Value Object): UUID 기반 식별자
- Nickname (Value Object): 3~20자, 영문/숫자/밑줄
- chips (long): 보유 칩 (0 이상)
- PlayerRecord (Entity): 전적 (wins, losses, draws)

**비즈니스 규칙**:
- Nickname은 3~20자, 영문/숫자/밑줄만 허용
- chips는 0 이상
- 칩이 0이면 게임 참여 불가

**책임**:
- 칩 증감 관리
- 전적 업데이트

#### 2. Game Aggregate (Phase 6)

**Root Entity**: Game

**구성 요소**:
- GameId (Value Object)
- List<Player> players (2~10명)
- Deck deck
- CommunityCards (Value Object)
- Pot (Value Object)
- BettingRound (Value Object)
- GameState (Enum)

**비즈니스 규칙**:
- 최소 2명, 최대 10명
- 게임 시작 시 모든 플레이어는 충분한 칩 보유
- 베팅 라운드는 순차 진행 (PRE_FLOP -> FLOP -> TURN -> RIVER)

**책임**:
- 라운드 시작/종료
- 베팅 액션 처리
- 승자 판정 및 팟 분배

#### 3. Room Aggregate (Phase 5.4)

**Root Entity**: Room

**구성 요소**:
- RoomId (Value Object)
- name (String)
- List<Player> players
- hostPlayerId (PlayerId)
- RoomState (Enum)

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

### Value Objects

1. PlayerId: UUID 래핑
2. Nickname: String 래핑, 검증 로직
3. Pot: long amount, 불변
4. BettingRound: Enum (PRE_FLOP, FLOP, TURN, RIVER)
5. GameId: UUID 래핑
6. MatchingCode: 6자리 코드
7. AIDecision: BettingAction, amount, reason
8. HandRanking (Phase 6): HIGH_CARD ~ ROYAL_FLUSH
9. HandResult (Phase 6): HandRanking, cards, kickers
10. CommunityCards (Phase 6): List<Card>
11. SidePot (Phase 6): amount, eligiblePlayers

### Domain Events

1. **GameEvent** (Sealed Interface)
   - RoundStarted
   - RoundEnded
   - BettingPhaseStarted

2. **MatchingEvent**
   - MatchingCompleted
   - MatchingTimeout

---

## Use Case 정의

### Input Ports (Use Cases)

#### Game Use Cases

1. **StartRoundUseCase**
   - 입력: StartRoundCommand (gameId, playerId)
   - 출력: RoundId

2. **PlaceBetUseCase** (Phase 6)
   - 입력: BettingActionCommand (gameId, playerId, action, amount)
   - 출력: void

3. **FoldUseCase** (Phase 6)
   - 입력: FoldCommand (gameId, playerId)
   - 출력: void

4. **JoinAsSpectatorUseCase** (Phase 8)
   - 입력: JoinSpectatorCommand (gameId, spectatorId)
   - 출력: void

#### Room Use Cases (Phase 5.4)

1. **CreateRoomUseCase**
   - 입력: CreateRoomCommand (name, hostPlayerId)
   - 출력: RoomId

2. **JoinRoomUseCase**
   - 입력: JoinRoomCommand (roomId, playerId)
   - 출력: void

3. **LeaveRoomUseCase**
   - 입력: LeaveRoomCommand (roomId, playerId)
   - 출력: void

#### Matching Use Cases

1. **JoinRandomMatchingUseCase**
   - 입력: JoinRandomMatchingCommand (playerId)
   - 출력: MatchingRequestId

2. **JoinCodeMatchingUseCase**
   - 입력: JoinCodeMatchingCommand (playerId, code)
   - 출력: MatchingRequestId

3. **CreateMatchingCodeUseCase**
   - 입력: CreateMatchingCodeCommand (playerId)
   - 출력: MatchingCode

4. **CancelMatchingUseCase**
   - 입력: CancelMatchingCommand (playerId)
   - 출력: void

#### AI Use Cases

1. **RequestAIPlayerUseCase**
   - 입력: RequestAIPlayerCommand (strategyType)
   - 출력: AIPlayer

### Output Ports

#### Game Ports

1. **EventPublisher**: publish(DomainEvent)
2. **GameRepositoryPort** (Phase 6): save, findById, findAll

#### Room Ports (Phase 5.4)

1. **RoomRepositoryPort**: save, findById, findAll

#### Matching Ports

1. **MatchingQueuePort**: add, remove, findAll
2. **MatchingNotificationPort**: notifyMatchingComplete

#### AI Ports

1. **AIStrategyPort**: decide(AIPlayer, GameContext)

#### Player Ports

1. **PlayerRepositoryPort**: save, findById, findAll

---

## 품질 기준 및 메트릭

### 테스트 커버리지 목표

- Domain Layer: 95% 이상
- Application Layer: 90% 이상
- Adapter Layer: 75% 이상
- 전체 평균: 85% 이상

### Mutation Score 목표

- Domain Layer: 80% 이상
- Application Layer: 70% 이상

### 아키텍처 규칙

- Domain은 외부 의존성 없음 (Pure Java)
- Application은 Adapter에 의존하지 않음
- 순환 의존성 없음
- MapStruct 매퍼는 명확한 계층에 위치

### 성능 기준

- 동시 접속자: 100명 이상
- 매칭 처리 시간: 평균 1초 미만
- 핸드 평가 시간: 10ms 미만
- 응답 시간: p95 500ms 미만

### 보안 기준

- 모든 사용자 입력 검증
- Rate Limiting: 플레이어당 초당 10 요청
- 세션 타임아웃: 30분 비활성 시 자동 로그아웃

### 코드 품질

- Lombok 사용으로 보일러플레이트 최소화
- MapStruct 사용으로 객체 매핑 자동화
- 명확하고 일관된 네이밍
- SonarQube Critical 이슈 0건

---

## 배포 및 운영

### 환경 구성

#### 개발 환경
- 데이터베이스: H2 인메모리
- 로깅 레벨: DEBUG
- 포트: 8080 (HTTP), 7777 (TCP Terminal)

#### 테스트 환경
- 데이터베이스: TestContainers + PostgreSQL
- 로깅 레벨: INFO

#### 운영 환경
- 데이터베이스: PostgreSQL 16 (Docker Compose)
- 로깅 레벨: WARN
- 메트릭: Prometheus + Grafana (Phase 8.7)

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

### CI/CD 파이프라인 (Phase 8)

1. Build: `./gradlew compileJava`
2. Test: `./gradlew test jacocoTestReport`
3. Quality Check: SonarQube, ArchUnit
4. Package: `./gradlew bootJar`
5. Deploy: Docker 이미지 빌드 후 Kubernetes 배포

---

## 향후 로드맵

### v1.0 (2026-06) - MVP

**목표**: 완전한 텍사스 홀덤 포커 서버

- 완전한 텍사스 홀덤 포커 규칙 구현
- 터미널 및 WebSocket 지원
- 랜덤/코드 매칭 시스템
- AI 플레이어 (3가지 전략)
- 테스트 커버리지 85% 이상

**릴리스 날짜**: 2026년 6월

---

### v1.1 (2026-09)

**목표**: 사용자 경험 개선

- 관전 모드
- 채팅 시스템
- 리더보드 및 통계

**릴리스 날짜**: 2026년 9월

---

### v1.2 (2026-12)

**목표**: 고급 기능 추가

- 토너먼트 모드
- 성능 최적화 (Caffeine Cache, Virtual Threads)
- 보안 강화 (Rate Limiting, 입력 검증)
- 모니터링 (Prometheus, Grafana, ELK)

**릴리스 날짜**: 2026년 12월

---

### v2.0 (2027-06) - 웹 플랫폼

**기간**: 2027-01 ~ 2027-06 (6개월)
**예상 투입 인력**: 프론트엔드 2명, 백엔드 2명, 디자이너 1명

#### Phase 9: REST API 개발 (2개월)

- RESTful API 설계 (OpenAPI 3.1)
- Spring Web MVC Controller 구현
- API 인증/인가 (Spring Security, JWT)
- API 문서 자동 생성 (springdoc-openapi)

#### Phase 10: React 웹 UI 개발 (4개월)

- React 프로젝트 설정 (Vite, TypeScript, TailwindCSS)
- 게임 UI 컴포넌트 (포커 테이블, 카드, 플레이어 아바타)
- WebSocket 통합 (SockJS, STOMP)
- 반응형 디자인 (모바일/태블릿/데스크톱)
- 리더보드 및 통계 UI (Chart.js)

**릴리스 날짜**: 2027년 6월

---

### v2.1 (2027-12) - 모바일 앱

**기간**: 2027-07 ~ 2027-12 (6개월)
**예상 투입 인력**: 모바일 2명, 백엔드 2명, 프론트엔드 1명, 디자이너 1명

#### Phase 11: 모바일 앱 개발 (4개월)

- React Native 프로젝트 설정 (Expo, TypeScript)
- 모바일 게임 UI (터치 최적화, 세로/가로 모드)
- 푸시 알림 (Firebase Cloud Messaging)

#### Phase 12: 소셜 기능 (2개월)

- 친구 시스템 (친구 추가/삭제, 친구 초대)
- 소셜 로그인 (Google, Facebook, Apple)

**릴리스 날짜**: 2027년 12월

---

### v3.0 (2028-06) - 글로벌 서비스

**기간**: 2028-01 ~ 2028-06 (6개월)
**예상 투입 인력**: ML 엔지니어 2명, 백엔드 3명, DevOps 2명, 프론트엔드 1명

#### Phase 13: 머신러닝 AI 플레이어 (3개월)

- 강화학습 모델 개발 (TensorFlow, PyTorch, PPO)
- AI 훈련 데이터 수집 (실제 게임 로그 10만 건 이상)
- AI 모델 서빙 (TensorFlow Serving, gRPC)

#### Phase 14: Multi-Region 지원 (3개월)

- CDN 통합 (Cloudflare, AWS CloudFront)
- 다국어 지원 (영어, 한국어, 일본어, 중국어)
- 리전별 서버 배포 (AWS, GCP, Azure)

**릴리스 날짜**: 2028년 6월

---

## 리스크 관리

### 기술 리스크

| 리스크 | 영향도 | 확률 | 완화 전략 |
|--------|--------|------|----------|
| Spring Boot 4.x Milestone 버전 불안정성 | 높음 | 중간 | GA 릴리스 대기, 대체 버전 준비 |
| Virtual Threads 성능 이슈 | 중간 | 낮음 | 성능 테스트 조기 수행, Thread Pool 대체 |
| MapStruct 학습 곡선 | 낮음 | 높음 | 팀 교육, 예제 코드 공유 |
| PostgreSQL 스케일링 한계 | 중간 | 낮음 | Redis 캐싱, Read Replica |

### 일정 리스크

| 리스크 | 영향도 | 확률 | 완화 전략 |
|--------|--------|------|----------|
| Phase 6 핸드 평가 구현 지연 | 높음 | 중간 | 경험 있는 개발자 투입, 외부 라이브러리 검토 |
| Phase 7 테스트 커버리지 목표 미달성 | 중간 | 중간 | 우선순위 조정, QA 투입 |
| Phase 10 React UI 디자인 지연 | 중간 | 높음 | 디자이너 조기 투입, UI 라이브러리 활용 |

### 인력 리스크

| 리스크 | 영향도 | 확률 | 완화 전략 |
|--------|--------|------|----------|
| 핵심 개발자 이탈 | 높음 | 낮음 | 문서화 강화, 페어 프로그래밍 |
| 신규 투입 인력 적응 지연 | 중간 | 중간 | 온보딩 프로세스 정립, 멘토링 |

### 품질 리스크

| 리스크 | 영향도 | 확률 | 완화 전략 |
|--------|--------|------|----------|
| 버그 다발 발생 | 높음 | 중간 | 테스트 커버리지 강화, 코드 리뷰 |
| 성능 저하 | 중간 | 낮음 | 성능 테스트 조기 수행, 프로파일링 |

---

## 부록

### 용어 정의

- **Hexagonal Architecture**: 포트와 어댑터 패턴, 비즈니스 로직을 중심에 두고 외부 시스템과의 연결을 어댑터로 추상화
- **DDD (Domain-Driven Design)**: 도메인 중심 설계, 비즈니스 로직을 도메인 모델로 표현
- **Aggregate**: 일관성 경계를 가진 엔티티와 값 객체의 클러스터
- **Value Object**: 식별자가 없고 불변인 객체
- **Domain Event**: 도메인에서 발생한 중요한 사건
- **Use Case**: 사용자의 목표를 달성하기 위한 시스템의 행동
- **Port**: 애플리케이션과 외부 세계 간의 인터페이스
- **Adapter**: Port를 구현하여 외부 시스템과 연결
- **MapStruct**: 컴파일 타임 객체 매핑 도구

### 참고 자료

- [Spring Boot 4.x 공식 문서](https://spring.io/projects/spring-boot)
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [MapStruct 공식 문서](https://mapstruct.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [TestContainers 공식 문서](https://www.testcontainers.org/)
- [텍사스 홀덤 규칙](https://www.pokernews.com/poker-rules/texas-holdem.htm)

---

이 PRD는 PokerHole 프로젝트의 전체 구현 계획을 담고 있습니다. Phase별 체크박스를 통해 진행 상황을 추적하고, 상세한 가이드를 통해 체계적인 개발을 진행할 수 있습니다.
