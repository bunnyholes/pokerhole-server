# PokerHole 개발 계획 (Claude.MD)

> **작성일**: 2025-10-02
> **현재 상태**: Phase 5 부분 완료, Phase 6 Task 6.1 완료
> **목표**: v1.0 릴리스를 위한 체계적 개발 로드맵

---

## 📋 현재 상태 요약

### ✅ 완료된 작업
- **Phase 1-4**: 인프라, 아키텍처, 도메인 모델, 터미널 UI (100% 완료)
- **Phase 5**: 게임 로직 통합 (부분 완료)
  - ✅ 라운드 시작/종료 로직
  - ✅ 카드 배분
  - ⏳ Room 관리 (Legacy 통합 필요)
- **Phase 6**: 포커 핵심 로직 (진행 중)
  - ✅ Task 6.1: 포커 핸드 평가 시스템 (완료)
  - ⏳ Task 6.2-6.8: 베팅, 팟 관리, 라운드 관리 등 (예정)

### 🎯 현재 우선순위
1. Phase 6 나머지 작업 완료 (Task 6.2-6.8)
2. Phase 7 테스트 및 품질 보증
3. Phase 8 고급 기능 추가

---

## 🚀 단계별 개발 계획

### Phase 6: 포커 게임 핵심 로직 구현 (진행 중)

#### Task 6.2: 베팅 액션 및 검증 ⏳
**예상 기간**: 2-3일

**구현 목표**:
- BettingAction enum (FOLD, CHECK, CALL, RAISE, ALL_IN)
- BettingValidator 도메인 서비스
- PlaceBetUseCase 구현
- 베팅 규칙 검증 로직

**주요 파일**:
```
core/domain/game/vo/BettingAction.java
core/domain/game/BettingValidator.java
core/application/port/in/game/PlaceBetUseCase.java
core/application/port/in/game/BettingActionCommand.java
core/common/exception/BettingException.java
```

**테스트**:
- BettingValidator 단위 테스트 (각 액션별 검증)
- 최소/최대 베팅 금액 검증
- RAISE 금액 검증 (이전 베팅의 2배 이상)

---

#### Task 6.3: 팟 관리 (메인팟 & 사이드팟) ⏳
**예상 기간**: 3-4일

**구현 목표**:
- Pot Value Object 확장
- PotManager 도메인 서비스
- 사이드팟 생성 및 분배 로직
- 올인 시나리오 처리

**주요 파일**:
```
core/domain/game/vo/Pot.java (수정)
core/domain/game/PotManager.java
core/domain/game/vo/SidePot.java
```

**테스트**:
- 메인팟만 있는 경우
- 사이드팟 생성 케이스
- 복잡한 멀티 사이드팟
- 동점 시 팟 분할

**복잡도**: ⭐⭐⭐⭐ (높음)
- 올인 시나리오가 복잡함
- 여러 플레이어의 다양한 베팅 금액 처리 필요

---

#### Task 6.4: 베팅 라운드 관리 ⏳
**예상 기간**: 2-3일

**구현 목표**:
- BettingRoundManager 도메인 서비스
- 차례 순서 관리 (Dealer Button 기준)
- 블라인드 처리 (Small Blind, Big Blind)
- 베팅 라운드 완료 조건 체크

**주요 파일**:
```
core/domain/game/BettingRoundManager.java
core/domain/game/vo/PlayerPosition.java
core/domain/game/vo/BettingRound.java
```

**테스트**:
- 포지션별 차례 순서
- 베팅 라운드 완료 조건
- 블라인드 자동 배치

---

#### Task 6.5: 커뮤니티 카드 관리 ⏳
**예상 기간**: 1-2일

**구현 목표**:
- CommunityCards Value Object
- 플랍, 턴, 리버 단계별 카드 공개
- Dealer와 통합

**주요 파일**:
```
core/domain/game/vo/CommunityCards.java
dealer/Dealer.java (수정)
```

**테스트**:
- 플랍 3장 공개
- 턴 1장 추가
- 리버 1장 추가
- 카드 중복 방지

**복잡도**: ⭐⭐ (낮음)
- 상대적으로 단순한 로직

---

#### Task 6.6: 게임 라운드 오케스트레이션 ⏳
**예상 기간**: 3-5일

**구현 목표**:
- RoundOrchestrator 도메인 서비스
- 전체 라운드 플로우 관리
- 상태 전이 (PREFLOP → FLOP → TURN → RIVER → SHOWDOWN)
- 이벤트 발행 및 브로드캐스트

**주요 파일**:
```
core/domain/game/RoundOrchestrator.java
core/domain/game/vo/RoundState.java
core/domain/game/event/FlopDealt.java
core/domain/game/event/TurnDealt.java
core/domain/game/event/RiverDealt.java
core/domain/game/event/ShowdownStarted.java
```

**테스트**:
- 전체 라운드 플로우 통합 테스트
- 각 단계별 상태 전이
- 중간 폴드 시나리오

**복잡도**: ⭐⭐⭐⭐⭐ (매우 높음)
- 모든 컴포넌트를 통합하는 핵심 로직
- 다양한 엣지 케이스 처리 필요

---

#### Task 6.7: 플레이어 액션 처리 및 타임아웃 ⏳
**예상 기간**: 2-3일

**구현 목표**:
- PlayerActionHandler
- 타임아웃 처리 (자동 폴드)
- 액션 큐 관리

**주요 파일**:
```
core/application/service/PlayerActionHandler.java
configuration/properties/GameProperties.java (수정)
```

**테스트**:
- 정상 액션 처리
- 타임아웃 시나리오
- 연결 끊김 처리

---

#### Task 6.8: Game Aggregate 추출 ⏳
**예상 기간**: 2-3일

**구현 목표**:
- Game aggregate root 정의
- GameState 관리
- 도메인 이벤트 발행
- Repository Port 정의

**주요 파일**:
```
core/domain/game/Game.java
core/domain/game/vo/GameState.java
core/application/port/out/game/GameRepositoryPort.java
adapter/out/persistence/jpa/GameJpaAdapter.java
```

**테스트**:
- Game aggregate 생성 및 상태 전이
- 영속성 테스트 (JPA)

---

### Phase 7: 테스트 및 품질 보증 📋

#### 우선순위 테스트 목록

**1. 단위 테스트 (Domain & Application)**
- [ ] Card, Deck, Hand 테스트
- [ ] Player 도메인 테스트
- [ ] Pot 테스트
- [ ] BettingValidator 테스트
- [ ] HandEvaluator 테스트 (완료)
- [ ] PotManager 테스트
- [ ] BettingRoundManager 테스트
- [ ] RoundOrchestrator 테스트

**2. 통합 테스트 (Adapter & Infrastructure)**
- [ ] JPA 영속성 테스트
- [ ] WebSocket 통신 테스트
- [ ] Terminal TCP 연결 테스트
- [ ] Event 발행/수신 테스트
- [ ] AI 플레이어 통합 테스트
- [ ] 매칭 시스템 통합 테스트

**3. E2E 테스트**
- [ ] 전체 게임 플로우 (시작 → 종료)
- [ ] 멀티 플레이어 시나리오
- [ ] AI 플레이어 포함 게임
- [ ] 타임아웃 및 연결 끊김 시나리오
- [ ] 동시 다발 게임 시나리오

**4. 아키텍처 테스트 (ArchUnit)**
- [x] Hexagonal Architecture 규칙
- [ ] Domain 격리 규칙 확장
- [ ] Naming Convention 검증
- [ ] 순환 의존성 방지

**5. 성능 테스트**
- [ ] 동시 접속자 100명 부하 테스트
- [ ] 매칭 시스템 처리량 테스트
- [ ] 메모리 누수 검사
- [ ] 데이터베이스 쿼리 최적화

**6. 뮤테이션 테스트 (PITest)**
- [ ] Domain 로직 뮤테이션 테스트
- [ ] Application 서비스 뮤테이션 테스트

---

### Phase 8: 고급 기능 및 최적화 📋

#### Task 8.1: 관전 모드
**예상 기간**: 3-4일

**기능**:
- 게임 관전 입장/퇴장
- 실시간 게임 상태 브로드캐스트 (관전자용)
- 관전자 목록 관리
- 관전자 UI (터미널)

---

#### Task 8.2: 채팅 시스템
**예상 기간**: 2-3일

**기능**:
- 게임 내 채팅 (플레이어/관전자)
- 이모지 지원
- 욕설 필터링
- 채팅 히스토리

---

#### Task 8.3: 리더보드 및 통계
**예상 기간**: 3-4일

**기능**:
- 플레이어 랭킹
- 승률, 평균 칩 등 통계
- 최근 전적
- 시즌별 리더보드

---

#### Task 8.4: 토너먼트 모드
**예상 기간**: 5-7일

**기능**:
- 토너먼트 생성 및 참가
- 블라인드 자동 증가
- 탈락 시스템
- 상금 분배

---

#### Task 8.5: 성능 최적화
**예상 기간**: 2-3일

**개선 사항**:
- Virtual Threads 활용
- 데이터베이스 인덱스 최적화
- 캐싱 전략 (Redis)
- 네트워크 프로토콜 최적화

---

#### Task 8.6: 보안 강화
**예상 기간**: 2-3일

**보안 조치**:
- JWT 기반 인증
- Rate Limiting
- SQL Injection 방지
- XSS/CSRF 방지

---

#### Task 8.7: 모니터링 및 로깅
**예상 기간**: 2-3일

**구현**:
- Prometheus 메트릭 추가
- Grafana 대시보드
- 구조화된 로깅 (JSON)
- Alert 설정

---

## 📊 예상 일정

### v1.0 릴리스 타임라인 (Phase 6-7 완료)

```
Phase 6 완료:        4-5주
Phase 7 테스트:      2-3주
버그 수정 및 안정화:  1-2주
--------------------------------
총 예상 기간:        7-10주
```

### v1.1 릴리스 타임라인 (Phase 8 일부)

```
Task 8.1-8.3:        2-3주
--------------------------------
총 예상 기간:        2-3주
```

---

## 🎯 핵심 우선순위

### High Priority (필수)
1. ✅ Task 6.1: 포커 핸드 평가 (완료)
2. ⏳ Task 6.2: 베팅 액션 및 검증
3. ⏳ Task 6.3: 팟 관리
4. ⏳ Task 6.6: 게임 라운드 오케스트레이션
5. 📋 Phase 7: 통합 테스트 및 E2E 테스트

### Medium Priority (중요)
1. ⏳ Task 6.4: 베팅 라운드 관리
2. ⏳ Task 6.5: 커뮤니티 카드 관리
3. ⏳ Task 6.7: 플레이어 액션 처리
4. ⏳ Task 6.8: Game Aggregate 추출
5. 📋 Phase 7: 성능 테스트

### Low Priority (선택)
1. 📋 Task 8.1: 관전 모드
2. 📋 Task 8.2: 채팅 시스템
3. 📋 Task 8.3: 리더보드
4. 📋 Task 8.7: 모니터링

---

## 🔧 기술적 고려사항

### 아키텍처 원칙
- **Hexagonal Architecture 준수**: Domain은 외부 의존성 없음
- **DDD 패턴**: Aggregate, Value Object, Domain Event 활용
- **SOLID 원칙**: 단일 책임, 개방-폐쇄 원칙
- **테스트 가능성**: 모든 레이어 독립적 테스트

### 코드 품질 기준
- **테스트 커버리지**: 최소 80% 이상
- **뮤테이션 커버리지**: 최소 70% 이상
- **ArchUnit 테스트**: 모든 아키텍처 규칙 통과
- **SonarQube**: Critical/Major 이슈 0개

### 성능 목표
- **응답 시간**: 평균 100ms 이하
- **동시 접속**: 최소 100명 지원
- **게임 처리**: 초당 10게임 이상
- **메모리**: 최대 2GB 이하

---

## 📝 다음 단계 (Immediate Next Steps)

### 1주차: Task 6.2 베팅 액션
- [ ] BettingAction enum 정의
- [ ] BettingValidator 구현
- [ ] PlaceBetUseCase 구현
- [ ] 단위 테스트 작성

### 2주차: Task 6.3 팟 관리
- [ ] Pot Value Object 확장
- [ ] PotManager 구현
- [ ] 사이드팟 로직
- [ ] 통합 테스트 작성

### 3주차: Task 6.4-6.5
- [ ] BettingRoundManager 구현
- [ ] CommunityCards 구현
- [ ] Dealer 통합
- [ ] 단위 테스트 작성

### 4주차: Task 6.6 오케스트레이션
- [ ] RoundOrchestrator 구현
- [ ] 전체 플로우 통합
- [ ] E2E 테스트 작성

---

## 🚨 알려진 이슈 및 기술 부채

### 현재 이슈
1. ⚠️ Room 관리가 Legacy 코드로 남아있음 (Phase 5 Task 5.4)
   - 해결: Phase 6 완료 후 리팩토링 예정

2. ⚠️ AI 플레이어 전략이 단순함
   - 해결: Phase 8 또는 v2.1에서 ML 기반으로 개선

3. ⚠️ 터미널 UI가 고정 크기로 제한됨
   - 해결: 반응형 레이아웃 개선 필요 (낮은 우선순위)

### 기술 부채
1. 일부 Legacy 서버 코드 존재 (server 패키지)
   - 점진적으로 Hexagonal Architecture로 마이그레이션 중

2. 데이터베이스 마이그레이션 스크립트 부족
   - Flyway 스크립트 추가 필요

3. 모니터링 및 로깅 시스템 미흡
   - Phase 8에서 개선 예정

---

## 🎓 학습 자료

### 포커 규칙
- [텍사스 홀덤 규칙](https://www.pokerstars.com/poker/games/texas-holdem/)
- [핸드 랭킹](https://www.cardplayer.com/rules-of-poker/hand-rankings)
- [베팅 구조](https://www.pokernews.com/poker-rules/)

### 아키텍처 패턴
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)

### 기술 스택
- [Spring Boot 4.x](https://spring.io/projects/spring-boot)
- [Java 21 Virtual Threads](https://openjdk.org/jeps/444)
- [JLine 3](https://github.com/jline/jline3)

---

## 📞 팀 커뮤니케이션

### 일일 체크인
- 진행 상황 공유
- 블로커 식별
- 다음 단계 계획

### 주간 리뷰
- 완료된 Task 리뷰
- 코드 품질 점검
- 다음 주 계획

### 마일스톤 리뷰
- Phase 완료 시 전체 리뷰
- 아키텍처 검증
- 성능 벤치마크

---

## ✅ 수용 기준 (Acceptance Criteria)

### v1.0 릴리스 조건
- [x] Phase 1-4 완료
- [ ] Phase 5 완료 (Room 관리 포함)
- [ ] Phase 6 완료 (Task 6.1-6.8)
- [ ] Phase 7 완료 (테스트 커버리지 80% 이상)
- [ ] 모든 ArchUnit 테스트 통과
- [ ] 성능 목표 달성 (100명 동시 접속)
- [ ] Critical 버그 0개

### v1.1 릴리스 조건
- [ ] v1.0 안정화
- [ ] Phase 8 Task 8.1-8.3 완료
- [ ] 관전 모드 기능 테스트
- [ ] 채팅 시스템 욕설 필터 적용
- [ ] 리더보드 성능 최적화

---

**마지막 업데이트**: 2025-10-02  
**다음 리뷰**: 2025-10-09 (1주 후)
