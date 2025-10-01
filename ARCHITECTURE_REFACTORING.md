# 🏗️ PokerHole 아키텍처 리팩토링 계획

## 📋 현재 상태 분석

### 문제점

#### 1. **혼재된 프로토콜 레이어**
```
현재:
- SSH Gateway (port 2222) - 사용 안 함, 삭제 필요
- Terminal TCP (port 7777) - 단순 등록만 처리, 역할 불명확
- WebSocket - 설정만 있고 실제 구현 없음

문제:
- SSH는 성능 문제로 폐기했으나 코드 남아있음
- TCP는 단순 등록만 하고 게임 통신은 안 함
- WebSocket 전환 계획이지만 구현 없음
```

#### 2. **서버 UI 렌더링 코드 잔존**
```
/ui/cli/
├── render/
│   ├── BannerRenderer.java      # 서버에서 렌더링 (불필요)
│   ├── MenuRenderer.java         # 서버에서 렌더링 (불필요)
│   └── PokerCliRenderer.java    # 서버에서 렌더링 (불필요)
└── model/
    └── PokerCliViewModel.java    # 서버 UI 모델 (불필요)

문제:
- 클라이언트(Go)가 UI를 담당하는데 서버에 UI 렌더링 코드 존재
- 역할 분리가 명확하지 않음
```

#### 3. **클라이언트 구조 미흡**
```
client/
├── main.go              # 모든 로직이 하나의 파일
└── main_test.go

문제:
- 단일 파일에 1000줄+ 코드
- 네트워크, UI, 상태관리 모두 섞여있음
- 테스트 어려움
- 확장성 부족
```

#### 4. **통신 프로토콜 불명확**
```
현재:
- 클라이언트 → 서버: JSON 한 번 전송 후 종료
- 게임 진행 중 통신: 없음
- 실시간 업데이트: 불가능

문제:
- 게임 진행을 위한 양방향 통신 없음
- 상태 동기화 메커니즘 없음
```

---

## 🎯 목표 아키텍처

### 역할 분리 원칙

```
┌─────────────────────────────────────────────────────────────┐
│                    SERVER (Java/Spring)                      │
│                                                              │
│  - 게임 로직 (도메인 모델)                                    │
│  - 매칭 시스템                                                │
│  - AI 플레이어                                                │
│  - 상태 관리 (Room, Player)                                  │
│  - 게임 진행 제어                                             │
│  - WebSocket 메시지 브로드캐스트                              │
│                                                              │
└──────────────────────┬───────────────────────────────────────┘
                       │
                  WebSocket
                  (JSON Protocol)
                       │
┌──────────────────────┴───────────────────────────────────────┐
│                   CLIENT (Go/Bubble Tea)                     │
│                                                              │
│  - TUI 렌더링 (Lipgloss)                                      │
│  - 사용자 입력 처리                                           │
│  - WebSocket 통신                                             │
│  - 로컬 상태 캐싱                                             │
│  - 애니메이션/효과                                            │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 📐 서버 아키텍처 개선

### 1. 제거할 항목

#### SSH 관련 (완전 삭제)
```bash
삭제 대상:
/adapter/out/network/ssh/
/configuration/properties/SshGatewayProperties.java
application.yml의 ssh 설정
```

#### 서버 UI 렌더링 (완전 삭제)
```bash
삭제 대상:
/ui/cli/render/                    # BannerRenderer, MenuRenderer 등
/ui/cli/model/                     # 서버 UI 모델
/ui/model/PokerCliViewModel.java   # 서버 ViewModel
/adapter/in/terminal/handler/      # 서버 UI 핸들러들
/adapter/in/terminal/state/        # 서버 상태 관리
```

### 2. WebSocket 프로토콜 정의

#### 메시지 타입
```java
// 클라이언트 → 서버
enum ClientMessageType {
    // 연결 관리
    REGISTER,           // 최초 접속 (UUID, Nickname)
    HEARTBEAT,          // 연결 유지

    // 매칭
    JOIN_RANDOM_MATCH,  // 랜덤 매칭 참가
    JOIN_CODE_MATCH,    // 코드 매칭 참가
    CANCEL_MATCHING,    // 매칭 취소

    // 게임 액션
    CALL,               // 콜
    RAISE,              // 레이즈
    FOLD,               // 폴드
    CHECK,              // 체크
    ALL_IN,             // 올인

    // 기타
    LEAVE_GAME,         // 게임 나가기
    CHAT_MESSAGE        // 채팅
}

// 서버 → 클라이언트
enum ServerMessageType {
    // 연결 응답
    REGISTER_SUCCESS,   // 등록 성공
    REGISTER_FAILURE,   // 등록 실패

    // 매칭 이벤트
    MATCHING_STARTED,   // 매칭 시작
    MATCHING_PROGRESS,  // 매칭 진행 (대기 인원)
    MATCHING_COMPLETED, // 매칭 완료
    MATCHING_CANCELLED, // 매칭 취소

    // 게임 상태
    GAME_STARTED,       // 게임 시작
    GAME_STATE_UPDATE,  // 게임 상태 업데이트
    PLAYER_ACTION,      // 플레이어 액션 알림
    ROUND_COMPLETED,    // 라운드 종료
    GAME_ENDED,         // 게임 종료

    // 에러
    ERROR,              // 일반 에러
    INVALID_ACTION      // 잘못된 액션
}
```

#### 메시지 구조
```json
// 기본 메시지 프레임
{
  "type": "GAME_STATE_UPDATE",
  "timestamp": 1234567890,
  "payload": { ... }
}

// 예: 게임 상태 업데이트
{
  "type": "GAME_STATE_UPDATE",
  "timestamp": 1234567890,
  "payload": {
    "gameId": "game-123",
    "round": "FLOP",
    "pot": 1000,
    "currentBet": 200,
    "communityCards": ["AS", "KH", "QD"],
    "players": [
      {
        "id": "player-1",
        "nickname": "LuckyShark123",
        "chips": 8500,
        "bet": 200,
        "status": "ACTIVE",
        "position": 0
      }
    ],
    "currentPlayer": "player-2",
    "validActions": ["CALL", "RAISE", "FOLD"]
  }
}
```

### 3. 서버 패키지 재구조화

```
dev.xiyo.pokerhole/
├── core/domain/                    # 도메인 (변경 없음)
│   ├── game/
│   ├── player/
│   ├── matching/
│   └── ai/
│
├── core/application/               # 유스케이스 (변경 없음)
│   ├── port/in/
│   └── port/out/
│
├── adapter/
│   ├── in/
│   │   └── websocket/              # WebSocket 어댑터 (신규)
│   │       ├── GameWebSocketHandler.java
│   │       ├── message/
│   │       │   ├── ClientMessage.java
│   │       │   ├── ServerMessage.java
│   │       │   └── MessageCodec.java
│   │       └── session/
│   │           ├── WebSocketSessionRegistry.java
│   │           └── PlayerSession.java
│   │
│   └── out/
│       ├── matching/               # 매칭 어댑터 (유지)
│       ├── ai/                     # AI 어댑터 (유지)
│       └── notification/           # 알림 어댑터 (신규)
│           └── WebSocketNotifier.java
│
└── configuration/
    ├── WebSocketConfig.java        # WebSocket 설정 (확장)
    └── properties/
```

---

## 🎨 클라이언트 아키텍처 개선

### Go 클라이언트 패키지 구조

```
client/
├── cmd/
│   └── poker-client/
│       └── main.go                 # 진입점
│
├── internal/
│   ├── app/
│   │   └── app.go                  # 애플리케이션 초기화
│   │
│   ├── ui/                         # Bubble Tea UI
│   │   ├── model.go                # 전체 UI 모델
│   │   ├── splash.go               # 스플래시 화면
│   │   ├── menu.go                 # 메뉴 화면
│   │   ├── matching.go             # 매칭 화면
│   │   ├── game.go                 # 게임 화면
│   │   └── styles.go               # Lipgloss 스타일
│   │
│   ├── network/                    # 네트워크 레이어
│   │   ├── client.go               # WebSocket 클라이언트
│   │   ├── messages.go             # 메시지 타입
│   │   ├── codec.go                # JSON 인코딩/디코딩
│   │   └── heartbeat.go            # Heartbeat 처리
│   │
│   ├── state/                      # 상태 관리
│   │   ├── game_state.go           # 게임 상태
│   │   ├── player_state.go         # 플레이어 상태
│   │   └── sync.go                 # 서버 동기화
│   │
│   └── identity/                   # 사용자 식별
│       ├── uuid.go                 # UUID 관리
│       └── nickname.go             # 닉네임 생성
│
├── pkg/                            # 공용 유틸리티
│   └── logger/
│       └── logger.go
│
├── go.mod
└── go.sum
```

### 주요 컴포넌트 설계

#### 1. WebSocket 클라이언트
```go
// internal/network/client.go
type Client struct {
    conn       *websocket.Conn
    serverURL  string
    uuid       string
    nickname   string

    // 메시지 채널
    inbound    chan ServerMessage
    outbound   chan ClientMessage

    // 상태
    connected  bool
    mu         sync.RWMutex
}

func (c *Client) Connect() error
func (c *Client) Send(msg ClientMessage) error
func (c *Client) Receive() <-chan ServerMessage
func (c *Client) Close() error
```

#### 2. Bubble Tea 통합
```go
// internal/ui/model.go
type Model struct {
    // 현재 화면
    currentView ViewType

    // 네트워크 클라이언트
    client *network.Client

    // 하위 뷰 모델
    splashModel   SplashModel
    menuModel     MenuModel
    matchingModel MatchingModel
    gameModel     GameModel

    // 상태
    gameState *state.GameState
}

// Bubble Tea 메시지
type ServerMessageReceived struct {
    Message network.ServerMessage
}

func (m Model) Update(msg tea.Msg) (tea.Model, tea.Cmd)
func (m Model) View() string
```

#### 3. 상태 동기화
```go
// internal/state/game_state.go
type GameState struct {
    GameID         string
    Round          RoundType
    Pot            int
    CurrentBet     int
    CommunityCards []string
    Players        []PlayerState
    CurrentPlayer  string
    ValidActions   []ActionType

    mu sync.RWMutex
}

func (s *GameState) Update(update ServerMessage)
func (s *GameState) GetSnapshot() GameStateSnapshot
```

---

## 🔄 마이그레이션 단계

### Phase 1: 서버 정리 (1일) ✅ COMPLETED
1. ✅ SSH 관련 코드 완전 삭제
2. ✅ 서버 UI 렌더링 코드 삭제
3. ✅ TCP 연결을 WebSocket 등록으로 통합
4. ✅ 메시지 프로토콜 정의

**결과:** 2,400+ 라인 제거, 불필요한 의존성 제거

### Phase 2: WebSocket 구현 (2일) ✅ COMPLETED
1. ✅ 서버 WebSocket 엔드포인트 구현 (`/ws/game`)
2. ✅ 메시지 코덱 구현 (JSON 직렬화/역직렬화)
3. ✅ 세션 관리 구현 (WebSocketSessionRegistry, PlayerSession)
4. ✅ 기본 핸드쉐이크 테스트 완료

**결과:** 완전한 프로토콜 스택, 통합 테스트 통과

### Phase 3: 클라이언트 리팩토링 (2일) ✅ COMPLETED
1. ✅ 패키지 구조 재편성 (모듈화된 Go 패키지)
2. ✅ WebSocket 클라이언트 구현 (자동 재연결, Heartbeat)
3. ✅ Bubble Tea 통합 (TUI 프레임워크)
4. ✅ 상태 동기화 구현 (Thread-safe 게임 상태)

**결과:** 532줄 모놀리식 → 5개 모듈, 650+ 라인

### Phase 4: 게임 통신 (2일) 🚧 READY FOR IMPLEMENTATION
1. ⏳ 게임 시작 플로우
2. ⏳ 액션 처리 (Call, Raise, Fold 등)
3. ⏳ 상태 업데이트 브로드캐스트
4. ⏳ 게임 종료 처리

**상태:** 인프라 준비 완료, 기존 게임 로직 통합 필요

### Phase 5: 테스트 & 최적화 (1일) ⏸️ PENDING
1. ⏳ 통합 테스트
2. ⏳ 성능 테스트
3. ⏳ 에러 처리 강화
4. ⏳ 재연결 로직

---

## 🚀 즉시 실행 가능한 액션

### 1. 서버 정리 스크립트
```bash
# 삭제할 디렉토리/파일
rm -rf src/main/java/dev/xiyo/pokerhole/adapter/out/network/ssh
rm -rf src/main/java/dev/xiyo/pokerhole/ui/cli/render
rm -rf src/main/java/dev/xiyo/pokerhole/ui/cli/model
rm -rf src/main/java/dev/xiyo/pokerhole/adapter/in/terminal/handler
rm -rf src/main/java/dev/xiyo/pokerhole/adapter/in/terminal/state
rm src/main/java/dev/xiyo/pokerhole/configuration/properties/SshGatewayProperties.java
rm src/main/java/dev/xiyo/pokerhole/ui/model/PokerCliViewModel.java
```

### 2. Go 클라이언트 리팩토링 시작
```bash
cd client
mkdir -p cmd/poker-client internal/{ui,network,state,identity} pkg/logger
```

### 3. WebSocket 라이브러리 추가
```gradle
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-websocket")
```

```bash
# Go
cd client
go get github.com/gorilla/websocket
```

---

## 📊 예상 효과

### 성능 개선
- SSH 오버헤드 제거 → **90% 지연시간 감소**
- WebSocket 양방향 통신 → **실시간 업데이트**
- 서버 렌더링 제거 → **서버 CPU 사용량 80% 감소**

### 코드 품질
- 역할 분리 명확화 → **유지보수성 향상**
- 클라이언트 모듈화 → **테스트 용이성 향상**
- 명확한 프로토콜 → **확장성 향상**

### 개발 속도
- 명확한 계층 구조 → **기능 추가 속도 향상**
- 병렬 개발 가능 → **팀 협업 효율 향상**

---

## ✅ 다음 단계

1. ✅ **Phase 1 완료**: 서버 불필요한 코드 정리 (2,400+ 라인 제거)
2. ✅ **Phase 2 완료**: WebSocket 프로토콜 구현 (통합 테스트 통과)
3. ✅ **Phase 3 완료**: 클라이언트 리팩토링 (모듈화 완료)
4. 🚧 **Phase 4 진행**: 게임 통신 (인프라 준비 완료, 게임 로직 통합 필요)
5. ⏸️ **Phase 5 대기**: 테스트 & 최적化

### 현재 상태 (2024년 10월 1일)

**완료된 작업:**
- 🎯 완전한 WebSocket 프로토콜 스택
- 🎯 서버 및 클라이언트 모두 빌드 성공
- 🎯 통합 테스트 통과 (REGISTER 플로우)
- 🎯 세션 관리 검증 완료
- 🎯 문서화 완료

**다음 작업 (Phase 4):**
- GameWebSocketHandler를 RoomRegistry에 연결
- 게임 액션을 실제 게임 로직과 통합
- 룸 내 모든 플레이어에게 상태 브로드캐스트
- 매칭 시스템 구현 (랜덤/코드 매칭)

**참고 문서:**
- `docs/PHASE1_REFACTORING_SUMMARY.md` - Phase 1 완료 요약
- `docs/PHASE2-5_IMPLEMENTATION_SUMMARY.md` - Phase 2, 3, 5 구현 상세
- `client/README.md` - 클라이언트 아키텍처 가이드
