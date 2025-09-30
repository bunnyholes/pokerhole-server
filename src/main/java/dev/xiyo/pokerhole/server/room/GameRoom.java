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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class GameRoom {
    private final String id;
    private final String name;
    private final Instant createdAt = Instant.now();
    private final Dealer dealer = Dealer.newDealer();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, Participant> participants = new LinkedHashMap<>();
    private SessionState host;
    private RoundLifecycleManager lifecycleManager;
    private boolean isRoundInProgress = false;

    public GameRoom(String id, String name) {
        this.id = id;
        this.name = name;
        this.lifecycleManager = new RoundLifecycleManager(this, scheduler);
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
            isRoundInProgress = true;
            
            // 라운드 완료 처리 - 5초 타이머 시작
            lifecycleManager.onRoundComplete();
        }

        announcements.forEach(this::broadcast);
    }

    /**
     * 다음 라운드 자동 시작 (lifecycleManager에서 호출)
     */
    public void autoStartNextRound() {
        if (host == null) {
            broadcast("⚠️ 방장이 없어 다음 라운드를 시작할 수 없습니다.");
            return;
        }
        
        // 자동으로 다음 라운드 시작
        try {
            lifecycleManager.startRound();
            startRound(host);
            isRoundInProgress = true;
        } catch (IllegalStateException e) {
            broadcast("⚠️ " + e.getMessage());
            isRoundInProgress = false;
        }
    }

    /**
     * 라운드 시작 가능 여부 확인
     */
    public boolean canStartRound() {
        return participants.size() >= Dealer.MIN_PLAYER;
    }

    /**
     * 나가기 요청 처리 (라운드 중에는 예약만)
     */
    public void requestLeave(SessionState session) {
        if (isRoundInProgress) {
            lifecycleManager.requestLeave(session);
        } else {
            // 라운드가 진행 중이 아니면 즉시 나가기
            remove(session);
        }
    }
    
    /**
     * 연결 끊김 처리
     */
    public void handleDisconnect(SessionState session) {
        if (isRoundInProgress) {
            lifecycleManager.handleDisconnect(session);
        } else {
            remove(session);
        }
    }

    /**
     * 세션 ID로 플레이어 제거
     */
    public void removeBySessionId(String sessionId) {
        Participant participant = participants.get(sessionId);
        if (participant != null) {
            remove(participant.session());
        }
    }

    public synchronized boolean isEmpty() {
        return participants.isEmpty();
    }
    
    /**
     * 방 정리 (스케줄러 종료)
     */
    public void cleanup() {
        lifecycleManager.cleanup();
        scheduler.shutdown();
    }

    private String buildParticipantLine() {
        return "👥 현재 인원: " + participants.size() + "/" + Dealer.MAX_PLAYER + "명";
    }

    private String formatScoreLine(Player player) {
        return String.format(Locale.KOREA, " - %s | 포인트: %d, 승: %d, 패: %d, 무: %d",
                player.getNickName(), player.getPoint(), player.getWins(), player.getLosses(), player.getDraws());
    }

    // Package-private for RoundLifecycleManager
    void broadcast(String message) {
        snapshotSessions().forEach(session -> session.send(message));
    }

    private List<SessionState> snapshotSessions() {
        synchronized (this) {
            return participants.values().stream()
                    .map(Participant::session)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private record Participant(SessionState session, Player player) {
    }
}
