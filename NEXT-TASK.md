# Phase 1 완료 작업 - 다음 단계 가이드

**브랜치**: `feature/phase1-completion`
**마지막 커밋**: `2791aae` - "feat: Implement Phase 1 WebSocket matching integration (Step 1-3)"
**진행률**: Phase 1 약 **85% 완료**

---

## 완료된 작업 (Step 1-3)

### Step 1: PlayerSession 강화 ✅

**파일**: `src/main/java/dev/xiyo/pokerhole/adapter/in/websocket/session/PlayerSession.java`

**추가된 기능**:
```java
private String currentRoomId;        // 현재 참가 중인 방 ID
private String currentMatchingId;    // 현재 매칭 요청 ID

public void joinRoom(String roomId)
public void joinMatching(String matchingId)
public void leaveRoom()
public void leaveMatching()
public boolean isInRoom()
public boolean isMatching()
```

**목적**: WebSocket 세션이 현재 어떤 방/매칭에 참가 중인지 추적

---

### Step 2: WebSocketMatchingNotificationAdapter ✅

**파일**: `src/main/java/dev/xiyo/pokerhole/adapter/out/matching/WebSocketMatchingNotificationAdapter.java` (새로 생성)

**구현 내용**:
- `MatchingNotificationPort` 구현
- `@Primary` 어노테이션으로 `TerminalMatchingNotificationAdapter` 교체
- WebSocket을 통한 매칭 알림 전송:
  - `MATCHING_COMPLETED`: 매칭 완료 시 모든 플레이어에게 전송
  - `MATCHING_PROGRESS`: 대기 중인 플레이어 수 업데이트
  - `MATCHING_CANCELLED`: 매칭 취소 알림

**의존성**:
- `WebSocketSessionRegistry`: 세션 조회
- `MessageCodec`: JSON 직렬화

**동작 흐름**:
```
MatchingScheduler → MatchingService → WebSocketMatchingNotificationAdapter
                                       → WebSocketSession.sendMessage()
```

---

### Step 3: GameWebSocketHandler TODO 구현 ✅

**파일**: `src/main/java/dev/xiyo/pokerhole/adapter/in/websocket/GameWebSocketHandler.java`

**구현된 TODO (6개 중 5개)**:

1. **handleJoinRandomMatch** ✅
   - `JoinRandomMatchingUseCase` 주입 및 호출
   - 플레이어 세션 검증
   - 매칭 요청 생성 후 `playerSession.joinMatching()` 상태 업데이트
   - `MATCHING_STARTED` 메시지 응답

2. **handleJoinCodeMatch** ✅
   - 코드 검증 (null/blank 체크)
   - `JoinCodeMatchingUseCase` 호출
   - 상태 업데이트 및 응답

3. **handleCancelMatching** ✅
   - `CancelMatchingUseCase` 호출 (메서드는 String sessionId를 직접 받음)
   - `playerSession.leaveMatching()` 호출
   - 예외 처리 (이미 취소된 경우에도 정상 응답)

4. **handleLeaveGame** ✅
   - `playerSession.isInRoom()` 검증
   - `playerSession.leaveRoom()` 호출
   - `GAME_ENDED` 메시지 전송
   - **TODO 남음**: GameRoom에서 플레이어 제거 및 다른 참가자 알림 (Step 4)

5. **handleChatMessage** ✅
   - 메시지 검증
   - `CHAT_MESSAGE` 타입 전송 (자신에게 에코)
   - **TODO 남음**: 같은 방에 있는 플레이어들에게 브로드캐스트 (Step 4)

6. **handleGameAction** ⚠️
   - 현재 로그만 출력
   - **미완성**: Step 4에서 GameCommandService와 연결 필요

**추가 작업**:
- `ServerMessageType.CHAT_MESSAGE` enum 추가

---

## 남은 작업 (Step 4-5)

### Step 4: GameCommandService 생성 및 게임 액션 처리 ⚠️

**예상 소요 시간**: 1-2시간

#### 4.1 GameCommandService 생성

**생성할 파일**: `src/main/java/dev/xiyo/pokerhole/core/application/service/GameCommandService.java`

**목적**: 게임 액션(CALL, RAISE, FOLD, CHECK, ALL_IN)을 처리하고 GameRoom/Dealer와 연동

**필요한 기능**:
```java
@Service
@RequiredArgsConstructor
public class GameCommandService {
    private final RoomRegistry roomRegistry;  // GameRoom 조회

    /**
     * 게임 액션 실행
     * @param sessionId 플레이어 세션 ID
     * @param action 액션 타입 (CALL, RAISE, FOLD, CHECK, ALL_IN)
     * @param amount 금액 (RAISE의 경우)
     * @throws IllegalStateException 방에 참가하지 않았거나 잘못된 턴인 경우
     */
    public void executeAction(String sessionId, PlayerAction action, Integer amount) {
        // 1. 세션 → 방 조회
        GameRoom room = findRoomBySessionId(sessionId);

        // 2. Dealer에 액션 위임
        room.getDealer().processAction(sessionId, action, amount);

        // 3. 게임 상태 변경 브로드캐스트
        broadcastGameState(room);
    }

    private GameRoom findRoomBySessionId(String sessionId) {
        // PlayerSession에서 currentRoomId 조회
        // RoomRegistry에서 GameRoom 조회
        // 없으면 IllegalStateException
    }

    private void broadcastGameState(GameRoom room) {
        // 방에 있는 모든 플레이어에게 GAME_STATE_UPDATE 전송
        // TODO: WebSocketSessionRegistry 사용하여 각 플레이어에게 전송
    }
}
```

**의존성 추가 필요**:
- `RoomRegistry` (이미 존재: `src/main/java/dev/xiyo/pokerhole/server/room/RoomRegistry.java`)
- `WebSocketSessionRegistry` (브로드캐스트용)
- `MessageCodec` (메시지 직렬화)

---

#### 4.2 GameWebSocketHandler에 GameCommandService 연결

**파일**: `src/main/java/dev/xiyo/pokerhole/adapter/in/websocket/GameWebSocketHandler.java`

**수정 내용**:

1. **의존성 주입**:
```java
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {
    // 기존 의존성들...

    // 추가
    private final GameCommandService gameCommandService;
```

2. **handleGameAction 구현**:
```java
private void handleGameAction(WebSocketSession session, String action, Map<String, Object> payload) {
    PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
            .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

    // 방에 참가 중인지 확인
    if (!playerSession.isInRoom()) {
        sendError(session, "방에 참가하지 않았습니다.");
        return;
    }

    try {
        Integer amount = null;
        if ("RAISE".equals(action) && payload != null) {
            amount = (Integer) payload.get("amount");
            if (amount == null || amount <= 0) {
                sendError(session, "RAISE 액션에는 유효한 금액이 필요합니다.");
                return;
            }
        }

        // GameCommandService에 위임
        gameCommandService.executeAction(session.getId(), parseAction(action), amount);

        // 응답은 broadcastGameState에서 처리됨

    } catch (IllegalStateException e) {
        log.warn("잘못된 게임 액션: sessionId={}, action={}", session.getId(), action, e);
        sendMessage(session, ServerMessage.of(ServerMessageType.INVALID_ACTION,
                Map.of("message", e.getMessage())));
    } catch (Exception e) {
        log.error("게임 액션 처리 실패: sessionId={}, action={}", session.getId(), action, e);
        sendError(session, "게임 액션 처리 중 오류가 발생했습니다.");
    }
}

private PlayerAction parseAction(String action) {
    return switch (action) {
        case "CALL" -> PlayerAction.CALL;
        case "RAISE" -> PlayerAction.RAISE;
        case "FOLD" -> PlayerAction.FOLD;
        case "CHECK" -> PlayerAction.CHECK;
        case "ALL_IN" -> PlayerAction.ALL_IN;
        default -> throw new IllegalArgumentException("알 수 없는 액션: " + action);
    };
}
```

---

#### 4.3 채팅 브로드캐스트 구현

**handleChatMessage 수정**:
```java
private void handleChatMessage(WebSocketSession session, Map<String, Object> payload) {
    PlayerSession playerSession = sessionRegistry.findBySessionId(session.getId())
            .orElseThrow(() -> new IllegalStateException("등록되지 않은 세션입니다."));

    String message = payload != null ? (String) payload.get("message") : null;

    if (message == null || message.isBlank()) {
        sendError(session, "채팅 메시지가 필요합니다.");
        return;
    }

    // 방에 있는 경우에만 브로드캐스트
    if (!playerSession.isInRoom()) {
        sendError(session, "방에 참가하지 않았습니다.");
        return;
    }

    log.info("채팅 메시지: sessionId={}, nickname={}, message={}",
            session.getId(), playerSession.getNickname(), message);

    // GameCommandService를 통해 같은 방 플레이어들에게 브로드캐스트
    gameCommandService.broadcastChatMessage(
            playerSession.getCurrentRoomId(),
            playerSession.getNickname(),
            message
    );
}
```

**GameCommandService에 추가**:
```java
public void broadcastChatMessage(String roomId, String senderNickname, String message) {
    GameRoom room = roomRegistry.findById(roomId)
            .orElseThrow(() -> new IllegalStateException("방을 찾을 수 없습니다."));

    // 방에 있는 모든 플레이어에게 전송
    room.getParticipants().forEach(participant -> {
        sessionRegistry.findBySessionId(participant.getSessionId()).ifPresent(playerSession -> {
            sendMessage(playerSession.getWebSocketSession(),
                    ServerMessage.of(ServerMessageType.CHAT_MESSAGE,
                            Map.of(
                                    "nickname", senderNickname,
                                    "message", message,
                                    "timestamp", System.currentTimeMillis()
                            )));
        });
    });
}
```

---

### Step 5: 통합 테스트 작성 ⚠️

**예상 소요 시간**: 30분

**생성할 파일**: `src/test/java/dev/xiyo/pokerhole/adapter/in/websocket/GameWebSocketHandlerIntegrationTest.java`

**테스트 시나리오**:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class GameWebSocketHandlerIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void fullGameFlow() throws Exception {
        // 1. WebSocket 연결
        StompSession session1 = connectWebSocket("player-uuid-1", "Player1");
        StompSession session2 = connectWebSocket("player-uuid-2", "Player2");
        StompSession session3 = connectWebSocket("player-uuid-3", "Player3");
        StompSession session4 = connectWebSocket("player-uuid-4", "Player4");

        // 2. 랜덤 매칭 참가
        session1.send("/app/JOIN_RANDOM_MATCH", Map.of());
        session2.send("/app/JOIN_RANDOM_MATCH", Map.of());
        session3.send("/app/JOIN_RANDOM_MATCH", Map.of());
        session4.send("/app/JOIN_RANDOM_MATCH", Map.of());

        // 3. 매칭 완료 대기
        awaitMessage(session1, ServerMessageType.MATCHING_COMPLETED, 5000);

        // 4. 게임 시작 확인
        awaitMessage(session1, ServerMessageType.GAME_STARTED, 2000);

        // 5. 게임 액션 (CALL, RAISE, FOLD)
        session1.send("/app/CALL", Map.of());
        awaitMessage(session2, ServerMessageType.PLAYER_ACTION, 1000);

        // 6. 채팅
        session1.send("/app/CHAT_MESSAGE", Map.of("message", "Hello!"));
        awaitMessage(session2, ServerMessageType.CHAT_MESSAGE, 1000);

        // 7. 게임 나가기
        session1.send("/app/LEAVE_GAME", Map.of());
        awaitMessage(session1, ServerMessageType.GAME_ENDED, 1000);
    }

    @Test
    void matchingCancellation() {
        // 매칭 참가 → 취소 플로우 테스트
    }

    @Test
    void invalidActionHandling() {
        // 잘못된 액션(턴 아닌 플레이어, 부족한 칩 등) 테스트
    }
}
```

**테스트 유틸리티**:
- Spring WebSocketClient 사용
- `StompSessionHandler` 구현
- 메시지 대기 헬퍼 메서드

---

## 기술적 참고사항

### RoomRegistry 및 GameRoom 구조

**RoomRegistry 위치**: `src/main/java/dev/xiyo/pokerhole/server/room/RoomRegistry.java`

**GameRoom 위치**: `src/main/java/dev/xiyo/pokerhole/server/room/GameRoom.java`

**GameRoom 주요 메서드**:
```java
public class GameRoom {
    private final String id;
    private final String name;
    private final Dealer dealer;
    private final Map<String, Participant> participants;

    public void join(SessionState session, String nickname, boolean asHost)
    public GameRoomSummary summary()
    public String id()
    public Dealer getDealer()  // 이 메서드 존재 여부 확인 필요
    // ...
}
```

**주의사항**:
- `GameRoom`이 `Dealer`를 public으로 노출하는지 확인 필요
- 노출하지 않으면 `GameRoom` 자체에 `processAction()` 메서드 추가 필요

---

### Dealer 구조

**Dealer 위치**: `src/main/java/dev/xiyo/pokerhole/dealer/Dealer.java`

**주요 메서드** (추정):
```java
public class Dealer {
    public static final int MAX_PLAYER = 10;

    // 게임 진행 메서드들 (확인 필요)
    public void processAction(String playerId, PlayerAction action, Integer amount);
    public GameState getCurrentState();
    // ...
}
```

**TODO**: Dealer API 확인 및 GameCommandService 구현 시 적절히 호출

---

### PlayerAction enum

**위치 확인 필요**: `src/main/java/dev/xiyo/pokerhole/core/domain/game/vo/PlayerAction.java` 또는 유사 경로

**예상 구조**:
```java
public enum PlayerAction {
    CALL,
    RAISE,
    FOLD,
    CHECK,
    ALL_IN
}
```

---

## 빌드 및 테스트

### 컴파일 확인
```bash
./gradlew compileJava
```

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 실행
```bash
./gradlew test --tests GameWebSocketHandlerIntegrationTest
```

### 현재 빌드 상태
✅ Step 1-3 구현 후 컴파일 성공 (커밋 `2791aae`)

---

## 커밋 가이드

### Step 4 완료 시 커밋 메시지 예시
```
feat: Implement GameCommandService for game actions (Step 4)

- Create GameCommandService to handle CALL, RAISE, FOLD, CHECK, ALL_IN
- Connect GameWebSocketHandler.handleGameAction to GameCommandService
- Implement broadcastGameState to notify all players in room
- Implement broadcastChatMessage for chat functionality
- Add action validation (turn order, chip amount, etc.)

Remaining: Step 5 (integration tests)
```

### Step 5 완료 시 커밋 메시지 예시
```
test: Add WebSocket integration tests (Step 5)

- Test full game flow: connect → match → start → action → leave
- Test matching cancellation
- Test invalid action handling
- Add test utilities for WebSocket message handling

Phase 1 completion: 100%
```

### Phase 1 완료 후 최종 커밋
```
feat: Complete Phase 1 - WebSocket integration and matching system

Summary:
- Enhanced PlayerSession with room/matching state tracking
- Implemented WebSocketMatchingNotificationAdapter
- Completed all 6 GameWebSocketHandler TODOs
- Created GameCommandService for game action processing
- Added comprehensive integration tests

Phase 1 is now 100% complete and ready for Phase 3 (client integration).

Next milestone: Test with actual Go client and start Phase 3.
```

---

## 체크리스트

**Step 4 완료 전**:
- [ ] `GameCommandService.java` 생성
- [ ] `executeAction()` 메서드 구현
- [ ] `broadcastGameState()` 메서드 구현
- [ ] `broadcastChatMessage()` 메서드 구현
- [ ] `GameWebSocketHandler`에 의존성 주입
- [ ] `handleGameAction()` 구현 완료
- [ ] `handleChatMessage()` 브로드캐스트 구현
- [ ] 컴파일 테스트 (`./gradlew compileJava`)

**Step 5 완료 전**:
- [ ] `GameWebSocketHandlerIntegrationTest.java` 생성
- [ ] `fullGameFlow()` 테스트 작성
- [ ] `matchingCancellation()` 테스트 작성
- [ ] `invalidActionHandling()` 테스트 작성
- [ ] 모든 테스트 통과 (`./gradlew test`)

**Phase 1 완료 전**:
- [ ] Step 4, 5 모두 완료
- [ ] 모든 TODO 주석 제거 또는 Phase 3로 이동
- [ ] 코드 리뷰 및 리팩토링
- [ ] 브랜치 푸시: `git push origin feature/phase1-completion`
- [ ] Pull Request 생성
- [ ] README.md 업데이트 (Phase 1 → 100%)

---

## 예상 완료 시간

- **Step 4**: 1-2시간
- **Step 5**: 30분
- **리팩토링 및 정리**: 30분

**총 예상 시간**: 2-3시간

---

## 문의사항

구현 중 불분명한 사항:
1. **GameRoom/Dealer API**: `getDealer()` 메서드가 public인지 확인 필요
2. **PlayerAction 위치**: enum 정확한 패키지 확인
3. **브로드캐스트 전략**: GameRoom에서 WebSocketSessionRegistry 접근 방법
4. **테스트 환경**: WebSocket 테스트 시 MockMvc vs 실제 WebSocketClient 선택

**해결 방법**: 코드 검색 및 기존 구현 참고

---

## 참고 문서

- **CLAUDE.md**: 프로젝트 전체 가이드
- **README.md**: 프로젝트 개요 및 Phase 진행 상황
- **ADR-002**: Server Authority 아키텍처 결정
- **Phase 1 백로그**: 이 문서의 상단 섹션 참고

**마지막 업데이트**: 2025-10-03
**작성자**: Claude (Phase 1 Step 1-3 구현)
