package com.example.gameservice.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A single Connect Four game, persisted in Game Service's own database (GameDB).
 * The board is stored as a CSV string of 42 cells (see ConnectFour engine).
 */
@Entity
@Table(name = "games")
public class Game {

    @Id
    private String id;

    @Column(name = "player_one")
    private String playerOne;     // human, the JWT 'sub'

    @Column(name = "player_two")
    private String playerTwo;     // the bot

    @Lob
    @Column(name = "board", length = 200)
    private String board;         // CSV, e.g. "0,0,1,0,..."

    @Column(name = "next_player")
    private int nextPlayer;       // 1 or 2

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "winner")
    private Integer winner;       // null, 1, 2, or 0 for draw

    @Column(name = "created_at")
    private Instant createdAt;

    public enum Status { IN_PROGRESS, WON, DRAW }

    protected Game() {
    }

    public Game(String id, String playerOne, String playerTwo, String board) {
        this.id = id;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.board = board;
        this.nextPlayer = 1;
        this.status = Status.IN_PROGRESS;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getPlayerOne() { return playerOne; }
    public String getPlayerTwo() { return playerTwo; }
    public String getBoard() { return board; }
    public int getNextPlayer() { return nextPlayer; }
    public Status getStatus() { return status; }
    public Integer getWinner() { return winner; }
    public Instant getCreatedAt() { return createdAt; }

    public void setBoard(String board) { this.board = board; }
    public void setNextPlayer(int nextPlayer) { this.nextPlayer = nextPlayer; }
    public void setStatus(Status status) { this.status = status; }
    public void setWinner(Integer winner) { this.winner = winner; }
}
