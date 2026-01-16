package org.example.server;

import com.google.gson.Gson;
import org.example.model.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private BufferedReader in;
    private BufferedWriter out;
    private Gson gson = new Gson();

    private String playerName;
    private GameRoom currentRoom;
    private int progress = 0;
    private int errors = 0;
    private double speed = 0;
    private long startTime = 0;

    public ClientHandler(Socket socket, GameServer server, BufferedReader in, BufferedWriter out) {
        this.socket = socket;
        this.server = server;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            String messageJson;
            while ((messageJson = in.readLine()) != null) {
                System.out.println("Получено от " + (playerName != null ? playerName : "unknown") + ": " + messageJson);

                Message message = Message.fromJson(messageJson);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения от клиента: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CONNECT:
                handleConnect(message.getData());
                break;

            case PROGRESS:
                handleProgress(message.getData());
                break;

            default:
                System.out.println("Неизвестный тип сообщения: " + message.getType());
        }
    }

    private void handleConnect(String data) {
        ConnectData connectData = gson.fromJson(data, ConnectData.class);
        this.playerName = connectData.playerName;

        server.addConnectedClient(playerName, this);
        System.out.println("Игрок подключился: " + playerName);

        String roomId = findOrCreateRoom();

        if (roomId != null) {
            Message response = new Message(Message.Type.CONNECT,
                    "{\"status\":\"connected\", \"roomId\":\"" + roomId + "\"}");
            sendMessage(response);
        } else {
            Message error = new Message(Message.Type.ERROR,
                    "{\"message\":\"Не удалось создать или найти комнату\"}");
            sendMessage(error);
        }
    }

    private String findOrCreateRoom() {
        for (GameRoom room : server.getRooms().values()) {
            if (!room.isFull() && !room.isGameStarted()) {
                room.addPlayer(this);
                return room.getRoomId();
            }
        }

        String roomId = generateRoomId();
        String text = server.getRandomText();
        GameRoom newRoom = new GameRoom(roomId, text);
        newRoom.addPlayer(this);
        server.getRooms().put(roomId, newRoom);
        return roomId;
    }

    private String generateRoomId() {
        return "room_" + System.currentTimeMillis() % 10000;
    }

    private void handleProgress(String data) {
        if (currentRoom == null || !currentRoom.isGameStarted()) {
            return;
        }

        ProgressData progressData = gson.fromJson(data, ProgressData.class);
        this.progress = progressData.progress;
        this.errors = progressData.errors;

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        currentRoom.updatePlayerProgress(playerName, progress, errors);
    }

    public void sendMessage(Message message) {
        try {
            out.write(message.toJson());
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.err.println("Ошибка отправки сообщения игроку " + playerName + ": " + e.getMessage());
        }
    }

    private void disconnect() {
        System.out.println("Игрок отключился: " + playerName);

        if (playerName != null) {
            server.removeConnectedClient(playerName);
        }

        if (currentRoom != null) {
            currentRoom.removePlayer(this);
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии сокета: " + e.getMessage());
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
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

    public double calculateSpeed() {
        if (startTime == 0 || progress == 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - startTime) / 1000;

        if (elapsedSeconds == 0) {
            return 0;
        }

        return (progress / (double) elapsedSeconds) * 60;
    }

    private static class ConnectData {
        String playerName;
    }

    private static class ProgressData {
        int progress;
        int errors;
    }
}