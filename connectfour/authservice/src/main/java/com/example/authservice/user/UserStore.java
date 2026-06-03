package com.example.authservice.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal credential store for the lab. Demo users (username / password):
 *   alice / password
 *   bob   / password
 */
@Component
public class UserStore {

    public record AuthUser(String id, String username, String passwordHash, List<String> roles) {}

    private final Map<String, AuthUser> usersByUsername = new ConcurrentHashMap<>();

    public UserStore(PasswordEncoder encoder) {
        register("alice", encoder.encode("password"), List.of("USER"));
        register("bob", encoder.encode("password"), List.of("USER"));
    }

    private void register(String username, String passwordHash, List<String> roles) {
        String id = "user-" + username;
        usersByUsername.put(username, new AuthUser(id, username, passwordHash, roles));
    }

    public Optional<AuthUser> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }
}
