package com.example.bff.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Protected proxy to User Service. Reaching any method here means the JWT was
 * already validated by the security filter. We forward the same Bearer token
 * downstream so User Service can also validate it (defense in depth / zero-trust).
 */
@RestController
@RequestMapping("/api/players")
public class UserProxyController {

    private final RestClient userClient;

    public UserProxyController(RestClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body,
                                    @RequestHeader("Authorization") String auth) {
        return forwardPost("/api/players", body, auth);
    }

    @GetMapping
    public ResponseEntity<?> all(@RequestHeader("Authorization") String auth) {
        Object body = userClient.get().uri("/api/players")
                .header("Authorization", auth)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable String id,
                                  @RequestHeader("Authorization") String auth) {
        Object body = userClient.get().uri("/api/players/{id}", id)
                .header("Authorization", auth)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<?> forwardPost(String uri, Map<String, Object> body, String auth) {
        Object response = userClient.post().uri(uri)
                .header("Authorization", auth)
                .body(body)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(response);
    }
}
