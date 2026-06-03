package com.example.botservice.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Posts the bot's reply move back to Game Service's internal endpoint over REST.
 * The base URL is configurable so it can point at localhost locally and at the
 * cluster-internal DNS name (http://gameservice:8083) in Kubernetes.
 */
@Component
public class GameClient {

    private final RestClient restClient;

    public GameClient(@Value("${gameservice.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void postBotMove(String gameId, int column) {
        restClient.post()
                .uri("/internal/games/{id}/bot-moves", gameId)
                .body(Map.of("column", column))
                .retrieve()
                .toBodilessEntity();
    }
}
