package org.example.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.GameResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonStorage {
    private static final String FILE_NAME = "game_history.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveResult(GameResult result) {
        List<GameResult> results = loadAllResults();
        results.add(result);

        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(results, writer);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении: " + e.getMessage());
        }
    }

    public static List<GameResult> loadAllResults() {
        if (!Files.exists(Paths.get(FILE_NAME))) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(FILE_NAME)) {
            GameResult[] resultsArray = gson.fromJson(reader, GameResult[].class);
            if (resultsArray != null) {
                return new ArrayList<>(List.of(resultsArray));
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}