package com.pokerhole.server.terminal;

import com.pokerhole.server.room.GameRoom;
import com.pokerhole.server.room.RoomRegistry;
import com.pokerhole.server.session.SessionState;
import com.pokerhole.server.support.InMemoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class TerminalCommandProcessorTest {

    private RoomRegistry roomRegistry;
    private TerminalCommandProcessor commandProcessor;

    @BeforeEach
    void setUp() {
        Locale.setDefault(Locale.KOREA);
        roomRegistry = new RoomRegistry();
        commandProcessor = new TerminalCommandProcessor(roomRegistry);
    }

    @Test
    void 방생성부터게임시작까지흐름() {
        InMemoryConnection hostConn = new InMemoryConnection();
        SessionState hostState = new SessionState(hostConn);
        commandProcessor.onConnect(hostState);
        assertEquals(2, hostConn.messages().size(), "접속 환영 메시지 2줄이 전송되어야 합니다.");

        commandProcessor.handle(hostState, "ROOM LIST");
        assertTrue(hostConn.messages().stream().anyMatch(msg -> msg.contains("방이 없습니다")),
                "방 목록이 비었음을 안내해야 합니다.");

        commandProcessor.handle(hostState, "ROOM CREATE alpha host");
        List<GameRoom> rooms = roomRegistry.listRooms().stream()
                .map(summary -> roomRegistry.findRoom(summary.id()).orElseThrow())
                .toList();
        assertEquals(1, rooms.size(), "방 생성 후 목록에 추가되어야 합니다.");

        GameRoom room = rooms.get(0);
        String roomId = room.id();
        assertTrue(hostState.isHost(), "방을 만든 세션이 방장이어야 합니다.");
        assertTrue(hostConn.messages().stream().anyMatch(msg -> msg.contains("방 \"alpha\"")),
                "방 입장 메시지가 전송되어야 합니다.");

        InMemoryConnection guestConn = new InMemoryConnection();
        SessionState guestState = new SessionState(guestConn);
        commandProcessor.onConnect(guestState);
        commandProcessor.handle(guestState, "ROOM JOIN " + roomId + " guest");

        assertTrue(guestConn.messages().stream().anyMatch(msg -> msg.contains("방 \"alpha\"")),
                "게스트도 방 입장 메시지를 받아야 합니다.");
        assertTrue(hostConn.messages().stream().anyMatch(msg -> msg.contains("guest 님이 방에 참가했습니다")),
                "방장은 참가자 입장을 브로드캐스트로 받아야 합니다.");

        assertEquals(2, room.summary().participants(), "두 명이 방에 있어야 합니다.");

        commandProcessor.handle(hostState, "START");
        assertTrue(hostConn.messages().stream().anyMatch(msg -> msg.contains("🎲 새로운 라운드를 시작합니다.")),
                "라운드 시작 안내가 브로드캐스트되어야 합니다.");
        assertTrue(guestConn.messages().stream().anyMatch(msg -> msg.contains("📊 현재 전적")),
                "라운드 결과 요약이 전달되어야 합니다.");

        long hostHandSize = StreamSupport.stream(hostState.player().orElseThrow().getHand().spliterator(), false).count();
        long guestHandSize = StreamSupport.stream(guestState.player().orElseThrow().getHand().spliterator(), false).count();
        assertEquals(0, hostHandSize, "라운드 종료 후 손패는 회수되어야 합니다.");
        assertEquals(0, guestHandSize, "라운드 종료 후 손패는 회수되어야 합니다.");

        assertTrue(guestConn.messages().stream().anyMatch(msg -> msg.contains("guest | 포인트")),
                "전적 출력에는 참가자 정보가 포함되어야 합니다.");
        assertTrue(guestConn.messages().stream().anyMatch(msg -> msg.contains("host | 포인트")),
                "전적 출력에는 방장 정보가 포함되어야 합니다.");

        commandProcessor.handle(guestState, "LEAVE");
        assertTrue(hostConn.messages().stream().anyMatch(msg -> msg.contains("guest 님이 방을 떠났습니다")),
                "퇴장 브로드캐스트가 전송되어야 합니다.");

        commandProcessor.handle(hostState, "LEAVE");
        assertTrue(roomRegistry.listRooms().isEmpty(), "모든 참가자가 떠나면 방이 제거되어야 합니다.");
    }

    @Test
    void 방장이아니면게임을시작할수없다() {
        InMemoryConnection hostConn = new InMemoryConnection();
        SessionState hostState = new SessionState(hostConn);
        commandProcessor.onConnect(hostState);
        commandProcessor.handle(hostState, "ROOM CREATE beta host");

        String roomId = roomRegistry.listRooms().get(0).id();
        InMemoryConnection guestConn = new InMemoryConnection();
        SessionState guestState = new SessionState(guestConn);
        commandProcessor.onConnect(guestState);
        commandProcessor.handle(guestState, "ROOM JOIN " + roomId + " guest");

        commandProcessor.handle(guestState, "START");
        assertTrue(guestConn.messages().stream().anyMatch(msg -> msg.contains("방장만 게임을 시작할 수 있습니다")),
                "방장이 아닌 참가자가 START 를 호출하면 오류가 안내되어야 합니다.");

        commandProcessor.handle(guestState, "LEAVE");
        commandProcessor.handle(hostState, "LEAVE");
    }

    @Test
    void 연결이끊기면참여중인방에서제거된다() {
        InMemoryConnection hostConn = new InMemoryConnection();
        SessionState hostState = new SessionState(hostConn);
        commandProcessor.onConnect(hostState);
        commandProcessor.handle(hostState, "ROOM CREATE gamma host");
        Optional<String> roomId = roomRegistry.listRooms().stream().map(summary -> summary.id()).findFirst();
        assertTrue(roomId.isPresent());

        commandProcessor.handleDisconnect(hostState);
        assertTrue(roomRegistry.listRooms().isEmpty(), "연결 종료 시 빈 방은 정리되어야 합니다.");
        assertTrue(hostState.currentRoom().isEmpty(), "세션 상태에서도 방 정보가 제거되어야 합니다.");
    }
}
