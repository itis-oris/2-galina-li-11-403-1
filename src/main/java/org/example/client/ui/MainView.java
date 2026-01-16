package org.example.client.ui;

import com.google.gson.Gson;
import org.example.client.GameClient;
import org.example.model.GameResult;
import org.example.model.PlayerProgress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainView extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JPanel connectionPanel;
    private JPanel waitingPanel;
    private JPanel gamePanel;
    private JPanel resultsPanel;

    private JTextField nameField;

    private JTextArea textArea;
    private JTextField inputField;
    private JLabel timerLabel;
    private JLabel progressLabel;
    private JLabel speedLabel;
    private JLabel errorsLabel;
    private DrawingPanel drawingPanel;

    private GameClient gameClient;
    private String currentText = "";
    private int currentProgress = 0;
    private int currentErrors = 0;
    private long gameStartTime = 0;
    private static final int GAME_TIME_SECONDS = 60;
    private Timer gameTimer;

    private Timer animationTimer;
    private int bgOffset = 0;

    private static final int SERVER_PORT = 8080;
    private static final String HOST = "localhost";

    private String opponentName = "Соперник";
    private int opponentProgress = 0;



    public MainView() {
        setTitle("Гонки на клавиатуре");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initPanels();
        showConnectionScreen();

        add(mainPanel);
    }


    private void initPanels() {
        connectionPanel = createConnectionPanel();
        waitingPanel = createWaitingPanel();
        gamePanel = createGamePanel();
        resultsPanel = createResultsPanel();

        mainPanel.add(connectionPanel, "CONNECTION");
        mainPanel.add(waitingPanel, "WAITING");
        mainPanel.add(gamePanel, "GAME");
        mainPanel.add(resultsPanel, "RESULTS");
    }


    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Гонки на клавиатуре", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        nameField = new JTextField();
        nameField.setBorder(BorderFactory.createTitledBorder("Ваше имя"));
        nameField.setMaximumSize(new Dimension(300, 55));

        JButton connectButton = new JButton("Подключиться");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setMaximumSize(new Dimension(300, 40));
        connectButton.addActionListener(e -> connectToServer());

        centerPanel.add(nameField);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(connectButton);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(centerPanel);
        panel.add(wrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createWaitingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel waitingLabel = new JLabel("Ожидание соперника...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        JLabel roomLabel = new JLabel("Комната: ", SwingConstants.CENTER);
        roomLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> {
            if (gameClient != null) {
                gameClient.disconnect();
            }
            showConnectionScreen();
        });

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        centerPanel.add(waitingLabel);
        centerPanel.add(roomLabel);
        centerPanel.add(progressBar);
        centerPanel.add(cancelButton);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(1, 4, 10, 10));

        timerLabel = new JLabel("Время: 0с");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        progressLabel = new JLabel("Прогресс: 0%");
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        speedLabel = new JLabel("Скорость: 0 зн/мин");
        speedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        speedLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        errorsLabel = new JLabel("Ошибки: 0");
        errorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorsLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        infoPanel.add(timerLabel);
        infoPanel.add(progressLabel);
        infoPanel.add(speedLabel);
        infoPanel.add(errorsLabel);

        textArea = new JTextArea(5, 50);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Текст для печати"));

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createTitledBorder("Начинайте печатать здесь..."));
        inputField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (gameStartTime == 0) {
                    gameStartTime = System.currentTimeMillis();
                    startGameTimer();
                }
                updateTypingProgress(inputField.getText());
            }
        });

        drawingPanel = new DrawingPanel();
        drawingPanel.setPreferredSize(new Dimension(780, 150));
        drawingPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(textScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.NORTH);
        bottom.add(drawingPanel, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel resultTitle = new JLabel("", SwingConstants.CENTER);
        resultTitle.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel winnerLabel = new JLabel("", SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JLabel statsLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton playAgainButton = new JButton("Играть снова");
        playAgainButton.addActionListener(e -> showConnectionScreen());

        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(playAgainButton);
        buttonPanel.add(exitButton);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 20, 20));
        centerPanel.add(resultTitle);
        centerPanel.add(winnerLabel);
        centerPanel.add(statsLabel);
        centerPanel.add(buttonPanel);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            if (currentText.isEmpty()) return;

            int totalChars = currentText.length();
            double myProgress = (double) currentProgress / totalChars;
            double opponentProgressValue = (double) opponentProgress / totalChars;

            int roadX = 20;
            int roadW = width - 40;

            int y1 = height / 2 - 40;
            int y2 = height / 2 + 10;

            g.setColor(new Color(230, 230, 230));
            g.fillRoundRect(roadX, y1, roadW, 25, 12, 12);
            g.fillRoundRect(roadX, y2, roadW, 25, 12, 12);

            g.setColor(Color.GRAY);
            g.drawRoundRect(roadX, y1, roadW, 25, 12, 12);
            g.drawRoundRect(roadX, y2, roadW, 25, 12, 12);


            g.setColor(Color.WHITE);
            for (int x = roadX - bgOffset; x < width; x += 40) {
                g.fillRect(x, y1 + 11, 20, 3);
                g.fillRect(x, y2 + 11, 20, 3);
            }

            int myX = roadX + (int) (roadW * myProgress) - 12;
            int opX = roadX + (int) (roadW * opponentProgressValue) - 12;

            drawCar(g, myX, y1 - 12, Color.BLUE);
            drawCar(g, opX, y2 - 12, Color.RED);
        }
    }
    public void showConnectionScreen() {
        cardLayout.show(mainPanel, "CONNECTION");
        stopGameTimer();
        stopAnimation();
    }

    public void showWaitingScreen() {
        cardLayout.show(mainPanel, "WAITING");
    }

    public void showGameScreen() {
        cardLayout.show(mainPanel, "GAME");
        inputField.requestFocusInWindow();
    }

    public void showResultsScreen() {
        cardLayout.show(mainPanel, "RESULTS");
        stopAnimation();
    }


    private void connectToServer() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showError("Ошибка", "Заполните все поля");
            return;
        }
        gameClient = new GameClient(this);
        gameClient.connect(HOST, SERVER_PORT, name);
        showWaitingScreen();
    }

    public void onConnectedToRoom(String roomId) {
        SwingUtilities.invokeLater(() -> {
            if (waitingPanel.getComponent(0) instanceof JPanel) {
                JPanel centerPanel = (JPanel) waitingPanel.getComponent(0);
                if (centerPanel.getComponent(1) instanceof JLabel) {
                    ((JLabel) centerPanel.getComponent(1)).setText("Комната: " + roomId);
                }
            }
        });
    }

    public void startGame(String text, String roomId) {
        currentText = text;
        currentProgress = 0;
        currentErrors = 0;
        gameStartTime = 0;

        SwingUtilities.invokeLater(() -> {
            textArea.setText(text);
            inputField.setText("");
            inputField.setEnabled(true);
            showGameScreen();
            startAnimation();
        });

        System.out.println("Текст для печати: " + text);
        System.out.println("Длина текста: " + text.length());
    }

    private void updateTypingProgress(String typedText) {
        if (currentText.isEmpty() || !inputField.isEnabled()) return;

        int correctChars = 0;
        int errors = 0;

        for (int i = 0; i < Math.min(typedText.length(), currentText.length()); i++) {
            if (typedText.charAt(i) == currentText.charAt(i)) {
                correctChars++;
            } else {
                errors++;
            }
        }

        currentProgress = correctChars;
        currentErrors = errors;

        updateGameUI();

        if (gameClient != null) {
            gameClient.sendProgress(currentProgress, currentErrors);
        }

        if (currentProgress >= currentText.length()) {
            inputField.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                showNotification("Вы напечатали весь текст!");
            });
        }
    }

    private void updateGameUI() {
        if (gameStartTime == 0) return;

        long currentTime = System.currentTimeMillis();
        final long elapsedSeconds = (currentTime - gameStartTime) / 1000;
        final long remainingSeconds = Math.max(0, GAME_TIME_SECONDS - elapsedSeconds);

        final double speed;
        if (elapsedSeconds > 0) {
            speed = (currentProgress / (double) elapsedSeconds) * 60;
        } else {
            speed = 0;
        }

        int totalChars = currentText.length();
        final int progressPercent = totalChars > 0 ? (currentProgress * 100) / totalChars : 0;
        final int currentErrorsFinal = currentErrors;

        SwingUtilities.invokeLater(() -> {
            timerLabel.setText("Осталось: " + remainingSeconds + "с");
            progressLabel.setText("Прогресс: " + progressPercent + "%");
            speedLabel.setText("Скорость: " + String.format("%.0f", speed) + " зн/мин");
            errorsLabel.setText("Ошибки: " + currentErrorsFinal);

            drawingPanel.repaint();

            if (remainingSeconds <= 0) {
                inputField.setEnabled(false);
                showNotification("Время вышло! Игра завершена.");
            }
        });
    }

    public void updateProgress(Object[] progresses) {
        Gson gson = new Gson();
        String json = gson.toJson(progresses);

        PlayerProgress[] playerProgresses = gson.fromJson(json, PlayerProgress[].class);

        String myPlayerName = gameClient != null ? gameClient.getPlayerName() : "";

        for (PlayerProgress progress : playerProgresses) {
            if (!progress.getPlayerName().equals(myPlayerName)) {
                opponentName = progress.getPlayerName();
                opponentProgress = progress.getProgress();
            }
        }
        drawingPanel.repaint();
    }

    private void startGameTimer() {
        stopGameTimer();
        final long[] remainingTime = {GAME_TIME_SECONDS};

        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingTime[0]--;

                if (remainingTime[0] <= 0) {
                    stopGameTimer();
                    endGameByTime();
                } else {
                    updateGameUI();
                }
            }
        });
        gameTimer.start();
    }

    private void endGameByTime() {
        SwingUtilities.invokeLater(() -> {
            showNotification("Время вышло!");

            if (gameClient != null) {
                gameClient.sendProgress(currentProgress, currentErrors);
            }
        });
    }

    private void stopGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
    }

    public void showGameResult(Object resultData) {
        stopGameTimer();

        if (gameClient == null || resultData == null) return;

        try {
            Gson gson = new Gson();

            String jsonData;
            if (resultData instanceof String) {
                jsonData = (String) resultData;
            } else {
                jsonData = gson.toJson(resultData);
            }

            GameResult result = gson.fromJson(jsonData, GameResult.class);

            SwingUtilities.invokeLater(() -> {
                String currentPlayerName = gameClient.getPlayerName();
                boolean isWinner = result.getWinner() != null && result.getWinner().equals(currentPlayerName);

                JPanel centerPanel = (JPanel) resultsPanel.getComponent(0);

                JLabel titleLabel = (JLabel) centerPanel.getComponent(0);
                titleLabel.setText(isWinner ? "Вы победили!" : "Вы проиграли");

                JLabel winnerLabel = (JLabel) centerPanel.getComponent(1);
                winnerLabel.setText("Победитель: " + result.getWinner());

                JLabel statsLabel = (JLabel) centerPanel.getComponent(2);
                statsLabel.setText(String.format("Скорость: %d зн/мин, Ошибки: %d",
                        result.getWinnerSpeed(), result.getWinnerErrors()));

                showResultsScreen();
            });

        } catch (Exception e) {
            System.err.println("Ошибка при обработке результатов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showNotification(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Уведомление",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void showError(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, title,
                    JOptionPane.ERROR_MESSAGE);
        });
    }

        private void drawCar(Graphics g, int x, int y, Color body) {
            g.setColor(body);
            g.fillRoundRect(x, y, 30, 16, 8, 8);

            g.setColor(Color.BLACK);
            g.fillOval(x + 4, y + 12, 8, 8);
            g.fillOval(x + 18, y + 12, 8, 8);

            g.setColor(new Color(255, 255, 255, 160));
            g.fillRoundRect(x + 18, y + 3, 8, 6, 4, 4);
        }

    private void startAnimation() {
        if (animationTimer != null) return;

        animationTimer = new Timer(40, e -> {
            bgOffset = (bgOffset + 4) % 40;
            drawingPanel.repaint();
        });
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainView frame = new MainView();
            frame.setVisible(true);
        });
    }
}
