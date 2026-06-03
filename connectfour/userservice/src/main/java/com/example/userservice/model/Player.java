package com.example.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A player profile, persisted in User Service's own database (UserDB).
 * The id matches the JWT 'sub' (e.g. "user-alice") so other services can
 * look a player up using the identity carried in the token.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    private int wins;
    private int losses;

    protected Player() {
    }

    public Player(String id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.wins = 0;
        this.losses = 0;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setWins(int wins) { this.wins = wins; }
    public void setLosses(int losses) { this.losses = losses; }
}
