package com.example.userservice.rest;

import com.example.userservice.model.Player;
import com.example.userservice.repository.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST CRUD for player profiles. Reached through the BFF, which has already
 * validated the JWT. CRUD = Create, Read, Update, Delete.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerRepository repository;

    public PlayerController(PlayerRepository repository) {
        this.repository = repository;
    }

    public record CreatePlayerRequest(String id, String username, String displayName) {}
    public record UpdatePlayerRequest(String displayName) {}

    @PostMapping
    public ResponseEntity<Player> create(@RequestBody CreatePlayerRequest req) {
        if (repository.existsById(req.id())) {
            return ResponseEntity.status(409).build();
        }
        Player saved = repository.save(new Player(req.id(), req.username(), req.displayName()));
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public List<Player> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> byId(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> update(@PathVariable String id, @RequestBody UpdatePlayerRequest req) {
        return repository.findById(id)
                .map(player -> {
                    player.setDisplayName(req.displayName());
                    return ResponseEntity.ok(repository.save(player));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
