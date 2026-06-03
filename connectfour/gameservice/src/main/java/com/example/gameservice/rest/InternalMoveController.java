package com.example.gameservice.rest;

import com.example.gameservice.game.GameService;
import com.example.gameservice.model.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal endpoint the Bot Service calls (REST) to post its reply move as
 * player 2. Not exposed through the BFF. In Kubernetes this is only reachable
 * cluster-internally via the gameservice Service DNS name.
 */
@RestController
@RequestMapping("/internal/games")
public class InternalMoveController {

    private final GameService gameService;

    public InternalMoveController(GameService gameService) {
        this.gameService = gameService;
    }

    public record BotMoveRequest(int column) {}

    @PostMapping("/{id}/bot-moves")
    public ResponseEntity<?> botMove(@PathVariable String id, @RequestBody BotMoveRequest req) {
        try {
            Game game = gameService.playMove(id, 2, req.column());
            return ResponseEntity.ok(Map.of(
                    "gameId", game.getId(),
                    "board", game.getBoard(),
                    "status", game.getStatus().toString()));
        } catch (GameService.MoveException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
