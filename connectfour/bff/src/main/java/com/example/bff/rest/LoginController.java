package com.example.bff.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Public login route. Forwards credentials to Auth Service and returns the JWT.
 * The client stores the token and sends it as a Bearer header on later calls.
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private final RestClient authClient;

    public LoginController(RestClient authClient) {
        this.authClient = authClient;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            Object response = authClient.post()
                    .uri("/auth/login")
                    .body(credentials)
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Login failed"));
        }
    }
}
