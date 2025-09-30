package dev.xiyo.pokerhole.server;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupMessageListener {

    private static final String STARTUP_MESSAGE = "서버가 실행되엇습니다. !";

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println(STARTUP_MESSAGE);
    }
}
