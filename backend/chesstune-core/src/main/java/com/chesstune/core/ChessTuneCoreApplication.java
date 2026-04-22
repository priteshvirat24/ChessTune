package com.chesstune.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ChessTune Core — REST API Entry Point.
 * Handles Auth, Users, Tournaments, Store, and Admin operations.
 */
@SpringBootApplication
public class ChessTuneCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessTuneCoreApplication.class, args);
    }
}
