package com.example.bff.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Protected proxy to Game Service. Forwards the validated Bearer token so the
 * human's identity (JWT 'sub') travels all the way to Game Service.
 */
@RestController
@RequestMapping("/api/games")
public class GameProxyController {

    private final RestClient gameClient;

    public GameProxyController(RestClient gameClient) {
        this.gameClient = gameClient;
    }

    @PostMapping
    public ResponseEntity<?> newGame(@RequestHeader("Authorization") String auth) {
        Object body = gameClient.post().uri("/api/games")
                .header("Authorization", auth)
                .retrieve().body(Object.class);
        return ResponseEntity.status(201).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id,
                                 @RequestHeader("Authorization") String auth) {
        Object body = gameClient.get().uri("/api/games/{id}", id)
                .header("Authorization", auth)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<?> move(@PathVariable String id,
                                  @RequestBody Map<String, Object> body,
                                  @RequestHeader("Authorization") String auth) {
        Object response = gameClient.post().uri("/api/games/{id}/moves", id)
                .header("Authorization", auth)
                .body(body)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(response);
    }
}
