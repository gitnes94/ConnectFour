package com.example.gameservice.rest;

import com.example.gameservice.game.GameService;
import com.example.gameservice.grpc.UserGrpcClient;
import com.example.gameservice.model.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API reached through the BFF. The human's identity comes from the
 * validated JWT (the 'sub' claim). Player 1 = human, Player 2 = bot.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final UserGrpcClient userGrpcClient;

    public GameController(GameService gameService, UserGrpcClient userGrpcClient) {
        this.gameService = gameService;
        this.userGrpcClient = userGrpcClient;
    }

    public record MoveRequest(int column) {}

    @PostMapping
    public ResponseEntity<?> newGame(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Game game = gameService.newGame(userId);
        // gRPC call to User Service to enrich the response with a display name
        String displayName = userGrpcClient.resolveDisplayName(userId);
        return ResponseEntity.status(201).body(view(game, displayName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return gameService.find(id)
                .map(g -> ResponseEntity.ok(view(g, userGrpcClient.resolveDisplayName(g.getPlayerOne()))))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Human move: always player 1. */
    @PostMapping("/{id}/moves")
    public ResponseEntity<?> move(@PathVariable String id,
                                  @RequestBody MoveRequest req,
                                  @AuthenticationPrincipal Jwt jwt) {
        try {
            Game game = gameService.playMove(id, 1, req.column());
            return ResponseEntity.ok(view(game, userGrpcClient.resolveDisplayName(game.getPlayerOne())));
        } catch (GameService.MoveException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private Map<String, Object> view(Game game, String displayName) {
        return Map.of(
                "gameId", game.getId(),
                "playerOneName", displayName,
                "board", game.getBoard(),
                "nextPlayer", game.getNextPlayer(),
                "status", game.getStatus().toString(),
                "winner", game.getWinner() == null ? "" : game.getWinner());
    }
}
