package dev.xiyo.pokerhole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.xiyo.pokerhole.configuration.properties.TerminalGatewayProperties;

@SpringBootApplication
@EnableConfigurationProperties(TerminalGatewayProperties.class)
public class PokerHoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokerHoleApplication.class, args);
    }
}
