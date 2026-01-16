package org.example.client;

import com.google.gson.Gson;
import org.example.client.ui.MainView;
import org.example.model.Message;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class GameClient {
    private MainView mainView;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private ClientThread clientThread;
    private Gson gson = new Gson();

    private String playerName;
    private boolean connected = false;

    public GameClient(MainView mainView) {
        this.mainView = mainView;
    }

    public void connect(String serverHost, int serverPort, String playerName) {
        this.playerName = playerName;

        try {
            socket = new Socket(serverHost, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            connected = true;

            clientThread = new ClientThread();
            new Thread(clientThread).start();

            sendConnectMessage();

            SwingUtilities.invokeLater(() -> {
                mainView.showNotification("Подключено к серверу");
            });

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                mainView.showError("Ошибка подключения", "Не удалось подключиться к серверу");
            });
        }
    }

    private void sendConnectMessage() {
        String connectData = "{\"playerName\":\"" + playerName + "\"}";
        Message message = new Message(Message.Type.CONNECT, connectData);
        sendMessage(message);
    }

    public void sendProgress(int progress, int errors) {
        if (!connected) return;

        String progressData = "{\"progress\":" + progress + ",\"errors\":" + errors + "}";
        Message message = new Message(Message.Type.PROGRESS, progressData);
        sendMessage(message);
    }

    public void sendMessage(Message message) {
        if (!connected || out == null) return;

        try {
            out.write(message.toJson());
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            mainView.showNotification("Отключено от сервера");
        });
    }

    public boolean isConnected() {
        return connected;
    }

    public String getPlayerName() {
        return playerName;
    }

    private class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                String messageJson;
                while (connected && (messageJson = in.readLine()) != null) {
                    System.out.println("Получено от сервера: " + messageJson);

                    Message message = Message.fromJson(messageJson);
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("Ошибка чтения от сервера: " + e.getMessage());
                    disconnect();
                }
            }
        }

        private void handleServerMessage(Message message) {
            SwingUtilities.invokeLater(() -> {
                switch (message.getType()) {
                    case CONNECT:
                        handleConnectResponse(message.getData());
                        break;

                    case START_GAME:
                        handleStartGame(message.getData());
                        break;

                    case PROGRESS:
                        handleProgressUpdate(message.getData());
                        break;

                    case GAME_END:
                        handleGameEnd(message.getData());
                        break;

                    case ERROR:
                        handleError(message.getData());
                        break;
                }
            });
        }

        private void handleConnectResponse(String data) {
            ConnectResponse response = gson.fromJson(data, ConnectResponse.class);
            mainView.onConnectedToRoom(response.roomId);
        }

        private void handleStartGame(String data) {
            System.out.println("Получены данные старта: " + data);

            StartGameData gameData = gson.fromJson(data, StartGameData.class);
            mainView.startGame(gameData.text, gameData.roomId);
        }

        private void handleProgressUpdate(String data) {
            PlayerProgress[] progresses = gson.fromJson(data, PlayerProgress[].class);
            mainView.updateProgress(progresses);
        }

        private void handleGameEnd(String data) {
            GameResultData result = gson.fromJson(data, GameResultData.class);
            mainView.showGameResult(result);
        }

        private void handleError(String data) {
            ErrorData error = gson.fromJson(data, ErrorData.class);
            mainView.showError("Ошибка сервера", error.message);
        }
    }

    private static class ConnectResponse {
        String status;
        String roomId;
    }

    private static class StartGameData {
        String text;
        String roomId;
    }

    private static class PlayerProgress {
        String playerName;
        int progress;
        int errors;
        double speed;
        int totalChars;
    }

    private static class GameResultData {
        String date;
        String winner;
        String[] players;
        int winnerSpeed;
        int winnerErrors;
    }

    private static class ErrorData {
        String message;
    }
}