# 🃏 PokerHole 네트워크 서버

Spring Boot 기반의 멀티플레이어 포커 게임 서버입니다. 표준 터미널(TCP 텍스트 클라이언트)과 브라우저(WebSocket 터미널)를 통해 접속해 방을 만들고 게임을 진행할 수 있습니다.

## 환경 요구 사항
- Java 21 (Virtual Thread 지원)
- Gradle 8.x (랩퍼 포함)

## 핵심 기능
- 기존 `Dealer`, `Player`, `Hand` 도메인 로직을 그대로 활용하는 게임 방 구조
- 방장 생성/참가/퇴장 및 자동 방장 승계
- 방장에 의한 라운드 시작과 라운드 결과 브로드캐스트
- 텍스트 기반 명령 프로토콜 공유 (TCP 터미널 & 웹소켓)

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

## 패키지 구조
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
