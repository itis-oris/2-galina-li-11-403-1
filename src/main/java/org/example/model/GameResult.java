package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameResult {
    private String date;
    private String winner;
    private String[] players;
    private int winnerSpeed;
    private int winnerErrors;

    public GameResult() {}

    public GameResult(String winner, String[] players, int winnerSpeed, int winnerErrors) {
        this.date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.winner = winner;
        this.players = players;
        this.winnerSpeed = winnerSpeed;
        this.winnerErrors = winnerErrors;
    }

    public String getDate() { return date; }
    public String getWinner() { return winner; }
    public String[] getPlayers() { return players; }
    public int getWinnerSpeed() { return winnerSpeed; }
    public int getWinnerErrors() { return winnerErrors; }
}