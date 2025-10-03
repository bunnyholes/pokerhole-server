package dev.xiyo.pokerhole.server.room;

import dev.xiyo.pokerhole.dealer.Dealer;
import dev.xiyo.pokerhole.core.domain.player.Player;
import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameRoom {
    private final String id;
    private final String name;
    private final Instant createdAt = Instant.now();
    private final Dealer dealer = Dealer.newDealer();

    private final Map<String, Participant> participants = new LinkedHashMap<>();
    private SessionState host;

    public GameRoom(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public synchronized GameRoomSummary summary() {
        return new GameRoomSummary(id, name, participants.size(), Dealer.MAX_PLAYER, createdAt);
    }

    public void join(SessionState session, String nickname, boolean asHost) {
        List<String> announcements = new ArrayList<>();
        synchronized (this) {
            if (participants.containsKey(session.id())) {
                throw new IllegalStateException("이미 방에 참가 중입니다.");
            }
            if (asHost && host != null) {
                throw new IllegalStateException("이미 방장이 존재합니다.");
            }
            if (participants.size() >= Dealer.MAX_PLAYER) {
                throw new IllegalStateException("방 정원이 가득 찼습니다.");
            }

            Player player = Player.newPlayer(nickname);
            dealer.enrollPlayer(player);

            Participant participant = new Participant(session, player);
            participants.put(session.id(), participant);

            session.attachToRoom(this, player, false);
            session.demoteFromHost();
            if (asHost || host == null) {
                host = session;
                session.promoteToHost();
            }

            announcements.add("✅ " + nickname + " 님이 방에 참가했습니다.");
            announcements.add(buildParticipantLine());
        }

        session.send("방 \"" + name + "\" (ID: " + id + ")에 참가했습니다.");
        if (session.isHost()) {
            session.send("🎩 당신은 방장입니다. START 명령으로 게임을 시작할 수 있습니다.");
        }
        announcements.forEach(this::broadcast);
    }

    public boolean remove(SessionState session) {
        List<String> announcements = new ArrayList<>();
        SessionState newHost = null;
        boolean removed = false;

        synchronized (this) {
            Participant participant = participants.remove(session.id());
            if (participant == null) {
                return participants.isEmpty();
            }
            removed = true;

            Player player = participant.player();
            dealer.removePlayer(player);
            player.dropHand();
            player.releaseNickname();

            announcements.add("🚪 " + player.getNickName() + " 님이 방을 떠났습니다.");
            session.detachFromRoom();
            session.demoteFromHost();

            if (session.equals(host)) {
                host = null;
                Optional<Participant> nextHost = participants.values().stream().findFirst();
                if (nextHost.isPresent()) {
                    host = nextHost.get().session();
                    host.promoteToHost();
                    newHost = host;
                }
            }

            if (!participants.isEmpty()) {
                announcements.add(buildParticipantLine());
            }
        }

        if (removed) {
            announcements.forEach(this::broadcast);
            if (newHost != null) {
                newHost.send("🎩 당신이 새로운 방장이 되었습니다.");
            }
        }

        return isEmpty();
    }

    public void startRound(SessionState requester) {
        List<String> announcements = new ArrayList<>();
        synchronized (this) {
            if (!participants.containsKey(requester.id())) {
                throw new IllegalStateException("게임에 참여 중인 사용자만 명령을 실행할 수 있습니다.");
            }
            if (host == null || !host.equals(requester)) {
                throw new IllegalStateException("방장만 게임을 시작할 수 있습니다.");
            }
            if (participants.size() < Dealer.MIN_PLAYER) {
                throw new IllegalStateException("게임을 시작하려면 최소 " + Dealer.MIN_PLAYER + "명이 필요합니다.");
            }

            announcements.add("🎲 새로운 라운드를 시작합니다.");
            dealer.newGame();
            announcements.add("🔄 덱을 섞습니다.");
            dealer.shuffle();
            announcements.add("🃏 카드를 나눠줍니다.");
            dealer.dealCard();
            dealer.handOpen();

            announcements.add("📢 이번 라운드 패 공개:");
            Map<String, String> latestMatch = new LinkedHashMap<>(dealer.getLatestMatch());
            latestMatch.forEach((playerName, hand) ->
                    announcements.add(" - " + playerName + " : " + hand));

            Optional<Player> winner = dealer.getLastMatchWinner();
            if (winner.isPresent()) {
                announcements.add("🏆 승자: " + winner.get().getNickName());
            } else {
                announcements.add("🤝 이번 라운드는 무승부입니다.");
            }

            announcements.add("📊 현재 전적:");
            announcements.addAll(participants.values().stream()
                    .map(participant -> formatScoreLine(participant.player()))
                    .collect(Collectors.toList()));

            dealer.retrieveCard();
        }

        announcements.forEach(this::broadcast);
    }

    public synchronized boolean isEmpty() {
        return participants.isEmpty();
    }

    // ===== Texas Hold'em 메서드 =====

    /**
     * Texas Hold'em 라운드 시작
     * @param requester 게임 시작을 요청한 세션 (방장만 가능)
     */
    public void startTexasHoldemRound(SessionState requester) {
        List<String> announcements = new ArrayList<>();
        synchronized (this) {
            // 1. 권한 확인
            if (!participants.containsKey(requester.id())) {
                throw new IllegalStateException("게임에 참여 중인 사용자만 명령을 실행할 수 있습니다.");
            }
            if (host == null || !host.equals(requester)) {
                throw new IllegalStateException("방장만 게임을 시작할 수 있습니다.");
            }
            if (participants.size() < Dealer.MIN_PLAYER) {
                throw new IllegalStateException("게임을 시작하려면 최소 " + Dealer.MIN_PLAYER + "명이 필요합니다.");
            }

            // 2. Texas Hold'em 게임 시작
            announcements.add("🎲 Texas Hold'em을 시작합니다.");
            dealer.startTexasHoldem();

            // 3. 각 플레이어에게 홀카드 정보 전송 (비공개)
            announcements.add("🃏 각 플레이어에게 홀카드 2장을 배분했습니다.");

            // 4. 게임 상태 정보
            announcements.add("📊 현재 라운드: " + dealer.getCurrentRound());
            announcements.add("💰 팟: " + dealer.getPot() + " 칩");

            // 5. 현재 턴 플레이어 알림
            Player currentPlayer = dealer.getCurrentPlayer();
            if (currentPlayer != null) {
                announcements.add("👉 턴: " + currentPlayer.getNickName());
            }
        }

        // 브로드캐스트
        announcements.forEach(this::broadcast);
    }

    /**
     * WebSocket 매칭 시스템용 플레이어 추가
     * SessionState 없이 닉네임만으로 플레이어를 Dealer에 등록합니다.
     *
     * @param nickname 플레이어 닉네임
     * @return 생성된 Player 객체
     */
    public Player addPlayerForWebSocket(String nickname) {
        synchronized (this) {
            // Dealer에 직접 플레이어 추가
            Player player = Player.newPlayer(nickname);
            dealer.enrollPlayer(player);
            return player;
        }
    }

    /**
     * Texas Hold'em 게임 자동 시작 (매칭 후 자동 생성된 방용)
     * 방장 권한 체크 없이 게임을 시작합니다.
     *
     * @throws IllegalStateException 플레이어가 부족하거나 이미 게임이 진행 중인 경우
     */
    public void startTexasHoldemAuto() {
        synchronized (this) {
            // Dealer에 등록된 플레이어 수 확인
            List<Player> players = dealer.getPlayers();
            if (players.size() < Dealer.MIN_PLAYER) {
                throw new IllegalStateException("게임을 시작하려면 최소 " + Dealer.MIN_PLAYER + "명이 필요합니다. 현재: " + players.size() + "명");
            }

            // 이미 게임이 진행 중인지 확인
            if (dealer.getCurrentRound() != null) {
                throw new IllegalStateException("이미 게임이 진행 중입니다.");
            }

            // Texas Hold'em 게임 시작
            dealer.startTexasHoldem();
        }
    }

    /**
     * 플레이어 액션 처리 (Texas Hold'em)
     * @param session 액션을 수행하는 세션
     * @param action 액션 타입 (FOLD, CHECK, CALL, BET, RAISE, ALL_IN)
     * @param amount 베팅/레이즈 금액 (필요한 경우)
     */
    public void processPlayerAction(SessionState session, dev.xiyo.pokerhole.core.domain.game.vo.PlayerAction action, int amount) {
        List<String> announcements = new ArrayList<>();
        synchronized (this) {
            // 1. 참가자 확인
            Participant participant = participants.get(session.id());
            if (participant == null) {
                throw new IllegalStateException("방에 참가하지 않았습니다.");
            }

            Player player = participant.player();

            // 2. Dealer에 액션 처리 위임
            dealer.processPlayerAction(player, action, amount);

            // 3. 액션 결과 알림
            announcements.add("🎯 " + player.getNickName() + " 님이 " + action + " 했습니다." +
                (amount > 0 ? " (금액: " + amount + ")" : ""));

            // 4. 게임 상태 업데이트
            announcements.add("💰 팟: " + dealer.getPot() + " 칩");
            announcements.add("📊 현재 라운드: " + dealer.getCurrentRound());

            // 5. 커뮤니티 카드 표시 (FLOP 이후)
            if (!dealer.getCommunityCards().isEmpty()) {
                announcements.add("🃏 커뮤니티 카드: " + dealer.getCommunityCards());
            }

            // 6. 다음 턴 플레이어 알림
            Player nextPlayer = dealer.getCurrentPlayer();
            if (nextPlayer != null) {
                announcements.add("👉 턴: " + nextPlayer.getNickName());
            }
        }

        // 브로드캐스트
        announcements.forEach(this::broadcast);

        // TODO: Step 4에서 ServerMessage 기반 브로드캐스트 추가
        // broadcastGameState();
    }

    /**
     * 플레이어 액션 처리 (nickname 기반, WebSocket용)
     * GameCommandService에서 호출
     * @param nickname 플레이어 닉네임
     * @param action 액션 타입
     * @param amount 베팅/레이즈 금액
     */
    public void processPlayerActionByNickname(String nickname, dev.xiyo.pokerhole.core.domain.game.vo.PlayerAction action, int amount) {
        synchronized (this) {
            // 닉네임으로 참가자 찾기
            Participant participant = participants.values().stream()
                    .filter(p -> p.player().getNickName().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("방에 참가하지 않은 플레이어입니다: " + nickname));

            Player player = participant.player();

            // Dealer에 액션 처리 위임
            dealer.processPlayerAction(player, action, amount);
        }
    }

    /**
     * 현재 게임 상태를 Map으로 반환 (WebSocket 브로드캐스트용)
     * GameCommandService에서 ServerMessage 생성 시 사용
     */
    public Map<String, Object> getGameStateMap() {
        synchronized (this) {
            Map<String, Object> state = new LinkedHashMap<>();

            // 기본 정보
            state.put("gameId", id);  // FIXED: roomId → gameId
            state.put("roomName", name);
            state.put("playerCount", participants.size());

            // 게임 상태 (Texas Hold'em)
            state.put("round", dealer.getCurrentRound() != null ? dealer.getCurrentRound().name() : null);
            state.put("pot", dealer.getPot());
            state.put("currentBet", dealer.getCurrentBet());

            // 커뮤니티 카드 - FIXED: Send as compact strings like "AS", "KH"
            List<String> communityCardsList = dealer.getCommunityCards().stream()
                    .map(card -> {
                        // Convert to compact format: "AS" (Ace of Spades), "10H" (Ten of Hearts)
                        String rank = card.getRank().toString();  // "A", "K", "Q", "J", "10", "9", etc.
                        String suit = card.getSuit().name().substring(0, 1);  // "S", "H", "D", "C"
                        return rank + suit;
                    })
                    .collect(Collectors.toList());
            state.put("communityCards", communityCardsList);

            // 현재 턴 플레이어 - FIXED: currentTurnPlayer → currentPlayer
            Player currentPlayer = dealer.getCurrentPlayer();
            state.put("currentPlayer", currentPlayer != null ? currentPlayer.getNickName() : null);

            // 플레이어 정보 (칩, 상태 등)
            List<Map<String, Object>> playersList = participants.values().stream()
                    .map(p -> {
                        Map<String, Object> playerInfo = new LinkedHashMap<>();
                        playerInfo.put("nickname", p.player().getNickName());
                        playerInfo.put("chips", p.player().getChips());
                        playerInfo.put("status", p.player().getStatus().name());
                        playerInfo.put("bet", p.player().getCurrentBet());  // FIXED: currentBet → bet
                        return playerInfo;
                    })
                    .collect(Collectors.toList());
            state.put("players", playersList);

            return state;
        }
    }

    /**
     * 현재 턴 플레이어 조회
     * @return 현재 턴 플레이어 (Optional)
     */
    public Optional<Player> getCurrentTurnPlayer() {
        synchronized (this) {
            return Optional.ofNullable(dealer.getCurrentPlayer());
        }
    }

    /**
     * 현재 게임 라운드 조회
     * @return 현재 베팅 라운드 (Optional)
     */
    public Optional<dev.xiyo.pokerhole.core.domain.game.vo.BettingRound> getCurrentRound() {
        synchronized (this) {
            return Optional.ofNullable(dealer.getCurrentRound());
        }
    }

    /**
     * 현재 팟 금액 조회
     * @return 팟 금액
     */
    public int getPot() {
        synchronized (this) {
            return dealer.getPot();
        }
    }

    private String buildParticipantLine() {
        return "👥 현재 인원: " + participants.size() + "/" + Dealer.MAX_PLAYER + "명";
    }

    private String formatScoreLine(Player player) {
        return String.format(Locale.KOREA, " - %s | 포인트: %d, 승: %d, 패: %d, 무: %d",
                player.getNickName(), player.getPoint(), player.getWins(), player.getLosses(), player.getDraws());
    }

    /**
     * 모든 참가자에게 메시지 브로드캐스트 (public API)
     * GameCommandService에서 JSON 메시지 전송 시 사용
     */
    public void broadcastMessage(String message) {
        broadcast(message);
    }

    private void broadcast(String message) {
        List<SessionState> sessions = snapshotSessions();

        // 메시지 전송 실패한 세션 추적
        List<String> failedSessions = new ArrayList<>();

        for (SessionState session : sessions) {
            try {
                if (session != null) {
                    session.send(message);
                }
            } catch (Exception e) {
                // 전송 실패 시 로그 남기고 계속 진행
                failedSessions.add(session.id());
            }
        }

        // 실패한 세션이 있으면 로그 출력
        if (!failedSessions.isEmpty()) {
            // Note: 실제 정리는 WebSocket handler의 afterConnectionClosed에서 처리됨
        }
    }

    private List<SessionState> snapshotSessions() {
        synchronized (this) {
            return participants.values().stream()
                    .map(Participant::session)
                    .filter(session -> session != null)  // null 체크 추가
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private record Participant(SessionState session, Player player) {
    }
}
