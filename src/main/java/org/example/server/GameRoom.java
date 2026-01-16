package org.example.server;

import com.google.gson.Gson;
import org.example.model.GameResult;
import org.example.model.Message;
import org.example.storage.JsonStorage;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private String roomId;
    private String textToType;
    private List<ClientHandler> players = new ArrayList<>();
    private boolean gameStarted = false;
    private boolean gameEnded = false;
    private long startTime;
    private Gson gson = new Gson();

    public GameRoom(String roomId, String textToType) {
        this.roomId = roomId;
        this.textToType = textToType;
        System.out.println("Создана комната " + roomId + " с текстом: " + textToType);
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTextToType() {
        return textToType;
    }

    public boolean addPlayer(ClientHandler player) {
        if (players.size() >= 2) {
            return false;
        }
        players.add(player);
        player.setCurrentRoom(this);
        System.out.println("Игрок " + player.getPlayerName() + " добавлен в комнату " + roomId);

        if (players.size() == 2 && !gameStarted) {
            startGame();
        }
        return true;
    }

    public void removePlayer(ClientHandler player) {
        players.remove(player);
        System.out.println("Игрок " + player.getPlayerName() + " покинул комнату " + roomId);
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void startGame() {
        gameStarted = true;
        startTime = System.currentTimeMillis();
        System.out.println("Игра началась в комнате " + roomId);
        System.out.println("Текст для печати: " + textToType);


        Message startMessage = new Message(Message.Type.START_GAME,
                "{\"text\":\"" + textToType + "\", \"roomId\":\"" + roomId + "\"}");

        for (ClientHandler player : players) {
            System.out.println("Отправляем текст игроку: " + player.getPlayerName());
            player.sendMessage(startMessage);
        }
    }

    public synchronized void updatePlayerProgress(String playerName, int progress, int errors) {
        if (gameEnded) return;

        for (ClientHandler player : players) {
            if (player.getPlayerName().equals(playerName)) {
                player.setProgress(progress);
                player.setErrors(errors);
                player.setSpeed(player.calculateSpeed());
                break;
            }
        }

        broadcastProgress();
        checkGameEnd();
    }

    private void broadcastProgress() {
        for (ClientHandler player : players) {
            List<PlayerProgress> progressList = new ArrayList<>();
            for (ClientHandler p : players) {
                progressList.add(new PlayerProgress(
                        p.getPlayerName(),
                        p.getProgress(),
                        p.getErrors(),
                        p.getSpeed(),
                        textToType.length()
                ));
            }

            String progressJson = gson.toJson(progressList);
            Message progressMessage = new Message(Message.Type.PROGRESS, progressJson);
            player.sendMessage(progressMessage);
        }
    }

    private static class PlayerProgress {
        String playerName;
        int progress;
        int errors;
        double speed;
        int totalChars;

        public PlayerProgress(String playerName, int progress, int errors, double speed, int totalChars) {
            this.playerName = playerName;
            this.progress = progress;
            this.errors = errors;
            this.speed = speed;
            this.totalChars = totalChars;
        }
    }

    private void checkGameEnd() {
        for (ClientHandler player : players) {
            if (player.getProgress() >= textToType.length()) {
                endGame(player);
                return;
            }
        }
    }

    private void endGame(ClientHandler winner) {
        if (gameEnded) return;
        gameEnded = true;

        long endTime = System.currentTimeMillis();
        double gameDuration = (endTime - startTime) / 1000.0;

        System.out.println("Игра окончена в комнате " + roomId);
        System.out.println("Победитель: " + winner.getPlayerName());
        System.out.println("Время игры: " + gameDuration + " секунд");

        String[] playerNames = players.stream()
                .map(ClientHandler::getPlayerName)
                .toArray(String[]::new);

        GameResult result = new GameResult(
                winner.getPlayerName(),
                playerNames,
                (int) winner.getSpeed(),
                winner.getErrors()
        );

        JsonStorage.saveResult(result);

        String resultJson = gson.toJson(result);
        Message endMessage = new Message(Message.Type.GAME_END, resultJson);

        for (ClientHandler player : players) {
            player.sendMessage(endMessage);
        }
    }

    public boolean isFull() {
        return players.size() >= 2;
    }
}