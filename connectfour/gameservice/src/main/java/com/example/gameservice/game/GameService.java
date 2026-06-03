package com.example.gameservice.game;

import com.example.gameservice.model.Game;
import com.example.gameservice.outbox.OutboxEvent;
import com.example.gameservice.repository.GameRepository;
import com.example.gameservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Core game orchestration. The key method is playMove: it updates the board AND
 * writes the move-played outbox event inside ONE @Transactional method, which is
 * the whole point of the Transactional Outbox pattern.
 */
@Service
public class GameService {

    public static final String BOT_ID = "bot";

    private final GameRepository gameRepository;
    private final OutboxRepository outboxRepository;

    public GameService(GameRepository gameRepository, OutboxRepository outboxRepository) {
        this.gameRepository = gameRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public Game newGame(String humanPlayerId) {
        String id = UUID.randomUUID().toString();
        int[][] empty = new int[ConnectFour.ROWS][ConnectFour.COLS];
        Game game = new Game(id, humanPlayerId, BOT_ID, ConnectFour.serialize(empty));
        return gameRepository.save(game);
    }

    public Optional<Game> find(String gameId) {
        return gameRepository.findById(gameId);
    }

    public static class MoveException extends RuntimeException {
        public MoveException(String message) { super(message); }
    }

    /**
     * Apply a move for {@code player} in {@code column}, update game status,
     * and emit a move-played event - all in one transaction.
     */
    @Transactional
    public Game playMove(String gameId, int player, int column) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new MoveException("Game not found"));

        if (game.getStatus() != Game.Status.IN_PROGRESS) {
            throw new MoveException("Game is already finished");
        }
        if (game.getNextPlayer() != player) {
            throw new MoveException("Not player " + player + "'s turn");
        }

        int[][] board = ConnectFour.parse(game.getBoard());
        if (!ConnectFour.canDrop(board, column)) {
            throw new MoveException("Column " + column + " is full or invalid");
        }

        int row = ConnectFour.drop(board, column, player);
        game.setBoard(ConnectFour.serialize(board));

        if (ConnectFour.hasWon(board, player)) {
            game.setStatus(Game.Status.WON);
            game.setWinner(player);
        } else if (ConnectFour.isFull(board)) {
            game.setStatus(Game.Status.DRAW);
            game.setWinner(0);
        } else {
            game.setNextPlayer(player == 1 ? 2 : 1);
        }

        gameRepository.save(game);

        // Same transaction: write the outbox event. If the app crashes after
        // this commit, the relay still finds and publishes it later.
        String payload = buildMovePlayedJson(game, player, column, row);
        outboxRepository.save(new OutboxEvent(game.getId(), "move-played", payload));

        return game;
    }

    private String buildMovePlayedJson(Game game, int player, int column, int row) {
        return "{"
                + "\"gameId\":\"" + game.getId() + "\","
                + "\"player\":" + player + ","
                + "\"column\":" + column + ","
                + "\"row\":" + row + ","
                + "\"board\":\"" + game.getBoard() + "\","
                + "\"nextPlayer\":" + game.getNextPlayer() + ","
                + "\"status\":\"" + game.getStatus() + "\""
                + "}";
    }
}
