package org.example.server;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

    private static final String[] TEXTS = {
            "As fast as thou shalt wane, so fast thou growest\n" +
                    "In one of thine, from that which thou departest;",
            "And that fresh blood which youngly thou bestowest\n" +
                    "Thou mayst call thine when thou from youth convertest.",
            "Herein lives wisdom, beauty and increase:\n" +
                    "Without this, folly, age and cold decay:",
            "If all were minded so, the times should cease\n" +
                    "And threescore year would make the world away."
    };

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен на порту " + PORT);
            System.out.println("Ожидание подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(
                        clientSocket,
                        this,
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream())),
                        new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
                );

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    public Map<String, GameRoom> getRooms() {
        return rooms;
    }

    public Map<String, ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    public void addConnectedClient(String playerName, ClientHandler handler) {
        connectedClients.put(playerName, handler);
    }

    public void removeConnectedClient(String playerName) {
        connectedClients.remove(playerName);
    }

    public String getRandomText() {
        Random rand = new Random();
        return TEXTS[rand.nextInt(TEXTS.length)];
    }
}