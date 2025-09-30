package com.pokerhole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.pokerhole.server.terminal.TerminalGatewayProperties;

@SpringBootApplication
@EnableConfigurationProperties(TerminalGatewayProperties.class)
public class PokerHoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokerHoleApplication.class, args);
    }
}
