package org.example.model;

public class PlayerProgress {
    private String playerName;
    private int progress;
    private int errors;
    private double speed;
    private int totalChars;

    public PlayerProgress() {}

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getTotalChars() {
        return totalChars;
    }

    public void setTotalChars(int totalChars) {
        this.totalChars = totalChars;
    }
}