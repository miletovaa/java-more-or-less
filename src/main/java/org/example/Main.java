package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class UIController {
    JFrame frame;
    private Button[][] buttons;
    String TITLE = "More or less, less is more";
    int BUTTON_SIZE = 50; // width and height of the buttons
    int PADDING = 30; // frame paddings

    public Label sumValueLabel;
    public Label movesLeftLabel;
    private ManualSettings manualSettings;

    public UIController() {
        frame = new JFrame(TITLE);
        int frameWidth = 300;
        int frameHeight = 300;

        frame.setSize(frameWidth, frameHeight);
        frame.setLayout(null);
        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                JOptionPane.showConfirmDialog(frame,"Are sure you want to close the application?");
            }
        });

        JLabel lobbyLabel = new JLabel(TITLE);
        lobbyLabel.setBounds(60, 50, 200, 40);
        frame.add(lobbyLabel);

        JButton btnStartGame = new JButton("Start new game");
        btnStartGame.setBounds(50, 100, 200, 40);
        btnStartGame.addActionListener(new ButtonLobbyListener(true));
        btnStartGame.setBackground(new Color(153, 253, 255));
        frame.add(btnStartGame);

        JButton btnImportGame = new JButton("Import game");
        btnImportGame.setBounds(50, 150, 200, 40);
        btnImportGame.addActionListener(new ButtonLobbyListener(false));
        btnImportGame.setBackground(new Color(153, 253, 255));
        frame.add(btnImportGame);
    }

    private class ButtonLobbyListener implements ActionListener {
        private boolean isRandomLayout;
        private ButtonLobbyListener(boolean isRandomLayout) {
            this.isRandomLayout = isRandomLayout;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isRandomLayout) {
                startNewGame(false, null);
            } else {
//              importGame();
            }
        }
    }

    class MenuListener implements ActionListener {
        private final int option;
        private Game gameInstance;

        public MenuListener(Game gameInstance, int option) {
            this.option = option;
            this.gameInstance = gameInstance;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (option){
                case 0:
                    int[] settings = {gameInstance.settings.getGameFieldSize(), gameInstance.settings.getTotalMoves(), gameInstance.settings.getTargetValue()};
                    startNewGame(true, settings);
                    break;
                case 1:
                    manualSettings = new ManualSettings(frame, gameInstance);
                    manualSettings.setVisible(true);
                    break;
                case 2:
                    gameInstance.settings.setEasyMode();
                    break;
                case 3:
                    gameInstance.settings.setMediumMode();
                    break;
                case 4:
                    gameInstance.settings.setHardMode();
                    break;
                case 5:
                    exportLayout(gameInstance);
                    break;
                case 6:
                    importLayout(gameInstance);
                    break;
            }
        }
    }

    class ManualSettings extends JDialog {
        JTextField fieldSizeField;
        JTextField movesField;
        JTextField targetValueField;
        JButton applyBtn;

        public ManualSettings(JFrame parent, Game gameInstance) {
            super(parent, "Manual Settings", true);
            setLayout(new FlowLayout());

            add(new JLabel("Field Size:"));
            fieldSizeField = new JTextField(Integer.toString(gameInstance.settings.getGameFieldSize()), 10);
            add(fieldSizeField);

            add(new JLabel("Number of Moves:"));
            movesField = new JTextField(Integer.toString(gameInstance.settings.getTotalMoves()), 10);
            add(movesField);

            add(new JLabel("Target Value:"));
            targetValueField = new JTextField(Integer.toString(gameInstance.settings.getTargetValue()), 10);
            add(targetValueField);

            add(new JLabel(""));
            applyBtn = new JButton("Apply");
            add(applyBtn);

            applyBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int fields = Integer.parseInt(fieldSizeField.getText());
                        int moves = Integer.parseInt(movesField.getText());
                        int target = Integer.parseInt(targetValueField.getText());

                        gameInstance.settings.applySettings(fields, moves, target);

                        setVisible(false);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(getParent(), "Invalid input. Please enter numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            setSize(300,300);
            setLocationRelativeTo(parent);
        }
    }

    public void importLayout(Game gameInstance) {
        JFileChooser fileChooser = new JFileChooser();
        int imp = fileChooser.showSaveDialog(frame);

        if (imp == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();

            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                frame.getContentPane().removeAll();
                frame.dispose();

                String firstLine = reader.readLine();
                StringTokenizer firstLineTokens = new StringTokenizer(firstLine, "|");
                int fields = Integer.parseInt(firstLineTokens.nextToken().trim());
                int moves = Integer.parseInt(firstLineTokens.nextToken().trim());
                int target = Integer.parseInt(firstLineTokens.nextToken().trim());
                gameInstance.settings.applySettings(fields, moves, target);

                for (int i = 0; i < fields; i++) {
                    String line = reader.readLine();
                    StringTokenizer lineTokens = new StringTokenizer(line, " ");
                    for (int j = 0; j < fields; j++) {
                        String value = lineTokens.nextToken().trim();
                        buttons[i][j] = addButtonOnField(j, i, value, gameInstance);
                    }
                }

                renderGrid(false, gameInstance);
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error reading the file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void exportLayout(Game gameInstance) {
        JFileChooser fileChooser = new JFileChooser();
        int exp = fileChooser.showSaveDialog(frame);

        if (exp == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath() + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                writer.write(gameInstance.settings.getGameFieldSize() + " | " +  gameInstance.settings.getTotalMoves() + " | " + gameInstance.settings.getTargetValue());
                writer.newLine();
                for (int i = 0; i < gameInstance.settings.getGameFieldSize(); i++) {
                    for (int j = 0; j < gameInstance.settings.getGameFieldSize(); j++) {
                        writer.write(buttons[i][j].getText() + " ");
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startNewGame(boolean isRestart, int[] settings) {
        Game gameInstance;
        if (settings != null) {
            gameInstance = new Game(this, settings[0], settings[1], settings[2]);
        } else {
            gameInstance = new Game(this);
        }
        renderGameField(gameInstance, true);
        if (isRestart) {
            sumValueLabel.updateLabel(0);
            movesLeftLabel.updateLabel(gameInstance.settings.getTotalMoves());
        }
    }

    private void renderGameField(Game gameInstance, boolean isRandomLayout) {
        int frameWidth = gameInstance.settings.getGameFieldSize() * BUTTON_SIZE + PADDING * 2;
        int frameHeight = gameInstance.settings.getGameFieldSize() * BUTTON_SIZE +  PADDING * 4;

        frame.getContentPane().removeAll();
        frame.dispose();

        frame.setSize(frameWidth, frameHeight);
        frame.setLayout(null);
        frame.setVisible(true);

        createMenu(gameInstance);
        createLabel("Target value", gameInstance.settings.getTargetValue(), PADDING, PADDING);
        sumValueLabel = createLabel("SUM", 0, PADDING, frameHeight - PADDING - 20);
        movesLeftLabel = createLabel("Moves left", gameInstance.settings.getTotalMoves(),frameWidth - 130, PADDING);

        if (isRandomLayout) buttons = new Button[gameInstance.settings.getGameFieldSize()][gameInstance.settings.getGameFieldSize()];
        renderGrid(isRandomLayout, gameInstance);
    }

    private void createMenu(Game gameInstance) {
        JMenuBar mb = new JMenuBar();

        JMenuItem restartItem = new JMenuItem("Restart");
        mb.add(restartItem);
        restartItem.addActionListener(new MenuListener(gameInstance, 0));

        JMenuItem settingsItem = new JMenuItem("Manual settings");
        mb.add(settingsItem);
        settingsItem.addActionListener(new MenuListener(gameInstance, 1));

        JMenu modeSubmenu = new JMenu("Difficulty");
        JMenuItem easyItem = new JMenuItem("Easy");
        easyItem.addActionListener(new MenuListener(gameInstance, 2));
        JMenuItem mediumItem = new JMenuItem("Medium");
        mediumItem.addActionListener(new MenuListener(gameInstance, 3));
        JMenuItem hardItem = new JMenuItem("Hard");
        hardItem.addActionListener(new MenuListener(gameInstance, 4));
        modeSubmenu.add(easyItem); modeSubmenu.add(mediumItem); modeSubmenu.add(hardItem);
        mb.add(modeSubmenu);

        JMenu layoutSubmenu = new JMenu("Layout");
        JMenuItem exportItem = new JMenuItem("Export");
        exportItem.addActionListener(new MenuListener(gameInstance, 5));
        layoutSubmenu.add(exportItem);
        JMenuItem importItem = new JMenuItem("Import");
        importItem.addActionListener(new MenuListener(gameInstance, 6));
        layoutSubmenu.add(importItem);
        mb.add(layoutSubmenu);

        frame.setJMenuBar(mb);
    }

    private void renderGrid(boolean isRandomLayout, Game gameInstance) {
        if (isRandomLayout) {
            Random random = new Random();
            for (int i = 0; i < gameInstance.settings.getGameFieldSize(); i++) {
                for (int j = 0; j < gameInstance.settings.getGameFieldSize(); j++) {
                    int randomDigit = random.nextInt(9) + 1;
                    buttons[i][j] = addButtonOnField(j, i, Integer.toString(randomDigit), gameInstance);
                }
            }
        } else {
            for (int i = 0; i < gameInstance.settings.getGameFieldSize(); i++) {
                for (int j = 0; j < gameInstance.settings.getGameFieldSize(); j++) {
                    addButtonOnField(j, i, buttons[i][j].getText(), gameInstance);
                }
            }
        }
    }

    private Button addButtonOnField(int col, int row, String value, Game gameInstance) {
        Button button = new Button(value);
        int x = PADDING + (col * BUTTON_SIZE);
        int y = PADDING * 2 + (row * BUTTON_SIZE);
        button.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);
        button.addActionListener(new ButtonGridListener(button, gameInstance));
        frame.add(button);
        return button;
    }

    public void rerenderGridOnMove(Game gameInstance) {
        Set<String> possibleMoves = new HashSet<>();

        int current = gameInstance.getCurrentSelectedValue();
        int previous = gameInstance.getPreviousSelectedValue();
        System.out.println("Current " + current);
        System.out.println("Previous " + previous);

        for (int i = 0; i < gameInstance.settings.getGameFieldSize(); i++) {
            for (int j = 0; j < gameInstance.settings.getGameFieldSize(); j++) {
                buttons[i][j].setEnabled(true);
                int col = i + 1; int row = j + 1;
                if (previous == 0) {
                    if (col % current == 0 || row % current == 0) {
                        possibleMoves.add(buttons[i][j].getText());
                    } else {
                        buttons[i][j].setEnabled(false);
                    }
                } else {
                    if ((col % current == 0 || row % current == 0) && (col % previous == 0 || row % previous == 0) ) {
                        possibleMoves.add(buttons[i][j].getText());
                        System.out.println("Button " + i + " " + j + " is available.");
                    } else {
                        buttons[i][j].setEnabled(false);
                    }
                }
            }
        }

        int maxNextStep = 0;
        for (String element : possibleMoves) {
            maxNextStep = Integer.parseInt(element);
        }

        if (
                maxNextStep == 0 ||
                        (maxNextStep + gameInstance.getCurrentSum() < gameInstance.settings.getTargetValue() && gameInstance.getMovesLeft() == 1)
        ) {
            gameInstance.gameOver();
        }

        gameInstance.setPreviousSelectedValue(current);
    }

    class Button extends JButton {
        private final Color activeBtn = new Color(98, 240, 150);
        private final Color inactiveBtn = new Color(209, 209, 209);

        public Button(String text) {
            super(text);
            this.setEnabled(true);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            setBackground(enabled ? activeBtn : inactiveBtn);
        }
    }

    class ButtonGridListener implements ActionListener {
        private final JButton btn;
        private final int value;
        private Game gameInstance;

        public ButtonGridListener(JButton btn, Game gameInstance) {
            this.btn = btn;
            this.value = Integer.parseInt(btn.getText());
            this.gameInstance = gameInstance;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            gameInstance.move(value);
            btn.setEnabled(false);
        }
    }

    class Label extends JLabel {
        String title;

        public Label(String text, int value) {
            super(text + ": " + value);
            this.title = text;
        }

        public void updateLabel(int value) {
            this.setText(title + ": " + value);
        }
    }

    private Label createLabel(String title, int value, int x, int y) {
        int width = 140; int height = 20;
        Label label = new Label(title, value);
        label.setBounds(x, y, width, height);
        frame.add(label);
        return label;
    }

    public int alert(String title, String message, int messageType) {
        Object[] options = { "Restart", "New game", "Exit" };
        return JOptionPane.showOptionDialog(
                frame,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                messageType,
                null,
                options,
                options[0]
        );
    }
}

class Game {
    public Settings settings;
    private UIController UI;
    private int movesLeft;
    private int currentSum;
    private int previousSelectedValue;
    private int currentSelectedValue;

    public Game(UIController UI) {
        settings = new Settings();
        this.UI = UI;
        this.movesLeft = settings.totalMoves;
        this.currentSum = 0;
        this.previousSelectedValue = 0;
        this.currentSelectedValue = 0;
        System.out.println("New Game created");
    }

    public Game(UIController UI, int gameFieldSize, int totalMoves, int targetValue) {
        settings = new Settings(gameFieldSize, totalMoves, targetValue);
        this.UI = UI;
        this.movesLeft = settings.totalMoves;
        this.currentSum = 0;
        this.previousSelectedValue = 0;
        this.currentSelectedValue = 0;
        System.out.println("New Game created");
    }

    public void move(int value) {
        previousSelectedValue = currentSelectedValue;
        currentSelectedValue = value;
        currentSum += value;
        movesLeft--;

        UI.sumValueLabel.updateLabel(currentSum);
        UI.movesLeftLabel.updateLabel(movesLeft);

        if (currentSum >= settings.targetValue) {
            win();
        } else {
            UI.rerenderGridOnMove(this);
            if (movesLeft == 0) gameOver();
        }
    }

    private void win() {
        int response = UI.alert("Win", "You won! Congratulations. New Game?", JOptionPane.PLAIN_MESSAGE);
        alertResponseReaction(response);
    }

    public void gameOver() {
        int deviation = settings.targetValue - currentSum;
        int response = UI.alert("Game Over", "No more moves left. Deviation: " + deviation + ". Restart?", JOptionPane.ERROR_MESSAGE);
        alertResponseReaction(response);
    }

    private void alertResponseReaction(int response) {
        if (response == 0) {
            int[] settings = {this.settings.getGameFieldSize(), this.settings.getTotalMoves(), this.settings.getTargetValue()};
            UI.startNewGame(true, settings);
        } else if (response == 1) {
            UI.startNewGame(false, null);
        } else {
            exitGame();
        }
    }

    private void exitGame() {
        System.exit(0);
    }

    public int getCurrentSum() {
        return currentSum;
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public int getPreviousSelectedValue() {
        return previousSelectedValue;
    }

    public void setPreviousSelectedValue(int previousSelectedValue) {
        this.previousSelectedValue = previousSelectedValue;
    }

    public int getCurrentSelectedValue() {
        return currentSelectedValue;
    }

//    public void setCurrentSelectedValue(int currentSelectedValue) {
//        this.currentSelectedValue = currentSelectedValue;
//    }

    class Settings {
        // Game settings
        private int gameFieldSize; // N*N is a field size
        private int totalMoves;
        private int targetValue;

        public Settings() {
            this.gameFieldSize = 10;
            this.totalMoves = 10;
            this.targetValue = 20;
        }

        public Settings(int gameFieldSize, int totalMoves, int targetValue) {
            this.gameFieldSize = gameFieldSize;
            this.totalMoves = totalMoves;
            this.targetValue = targetValue;
        }

        public void applySettings(int fields, int moves, int target) {
            int[] settings = {fields, moves, target};
            UI.startNewGame(false, settings);
        }

        public void setEasyMode() {
            applySettings(10, 15, 20);
        }

        public void setMediumMode() {
            applySettings(10, 12, 25);
        }

        public void setHardMode() {
            applySettings(10, 10, 30);
        }

        public int getGameFieldSize() {
            return gameFieldSize;
        }

//        public void setGameFieldSize(int newGameFieldSize) {
//            this.gameFieldSize = newGameFieldSize;
//        }

        public int getTotalMoves() {
            return totalMoves;
        }

//        public void setTotalMoves(int totalMoves) {
//            this.totalMoves = totalMoves;
//        }

        public int getTargetValue() {
            return targetValue;
        }

//        public void setTargetValue(int targetValue) {
//            this.targetValue = targetValue;
//        }
    }
}

public class Main {
    public static void main(String[] args) {
        UIController uiController = new UIController();
    }
}