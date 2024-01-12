package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class UIController {
    String TITLE = "More or less, less is more";

    /* layout constants */
    int BUTTON_SIZE = 50;
    int PADDING = 30;

    /* components initialization */
    JFrame frame;
    private Button[][] buttons;
    public Label sumValueLabel;
    public Label movesLeftLabel;

    public UIController() {
        /* display of the game lobby frame */
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

        /* adding game title label and functional buttons in lobby */
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
        private final boolean isRandomLayout;
        private ButtonLobbyListener(boolean isRandomLayout) {
            this.isRandomLayout = isRandomLayout;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            /* processing click on the lobby buttons */
            if (isRandomLayout) {
                startNewGame(false, null);
            } else {
                importLayout();
            }
        }
    }

    class MenuListener implements ActionListener {
        private final int option;
        private final Game gameInstance;

        public MenuListener(Game gameInstance, int option) {
            this.option = option;
            this.gameInstance = gameInstance;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            /* processing menu options selection */
            switch (option){
                /* "restart" option */
                case 0:
                    int[] settings = {gameInstance.settings.getGameFieldSize(), gameInstance.settings.getTotalMoves(), gameInstance.settings.getTargetValue()};
                    startNewGame(true, settings);
                    break;
                /* "manual settings" option */
                case 1:
                    ManualSettings manualSettings = new ManualSettings(frame, gameInstance);
                    manualSettings.setVisible(true);
                    break;
                /* "game mode" -> "easy" option */
                case 2:
                    gameInstance.settings.setEasyMode();
                    break;
                /* "game mode" -> "medium" option */
                case 3:
                    gameInstance.settings.setMediumMode();
                    break;
                /* "game mode" -> "hard" option */
                case 4:
                    gameInstance.settings.setHardMode();
                    break;
                /* "layout" -> "export" option */
                case 5:
                    exportLayout(gameInstance);
                    break;
                /* "layout" -> "import" option */
                case 6:
                    importLayout();
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
            /* display of the manual settings modal */
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
                        /* processing the manual settings change */
                        int fields = Integer.parseInt(fieldSizeField.getText());
                        int moves = Integer.parseInt(movesField.getText());
                        int target = Integer.parseInt(targetValueField.getText());

                        gameInstance.settings.applySettings(fields, moves, target);

                        setVisible(false);
                    } catch (NumberFormatException ex) {
                        /* input validation */
                        JOptionPane.showMessageDialog(getParent(), "Invalid input. Please enter numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            setSize(300,300);
            setLocationRelativeTo(parent);
        }
    }

    public void importLayout() {
        /* waiting for user's file choice */
        JFileChooser fileChooser = new JFileChooser();
        int imp = fileChooser.showSaveDialog(frame);

        if (imp == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();

            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                frame.getContentPane().removeAll();
                frame.dispose();

                /*  first line of the export file contains manual settings.
                    setting them and restarting the game with new settings
                */
                String firstLine = reader.readLine();
                StringTokenizer firstLineTokens = new StringTokenizer(firstLine, "|");
                int fields = Integer.parseInt(firstLineTokens.nextToken().trim());
                int moves = Integer.parseInt(firstLineTokens.nextToken().trim());
                int target = Integer.parseInt(firstLineTokens.nextToken().trim());
                Game gameInstance = new Game(this, fields, moves, target);

                /* parsing of the rest of the file and rendering of the given game grid */
                buttons = new Button[fields][fields];
                for (int i = 0; i < fields; i++) {
                    String line = reader.readLine();
                    StringTokenizer lineTokens = new StringTokenizer(line, " ");
                    for (int j = 0; j < fields; j++) {
                        String value = lineTokens.nextToken().trim();
                        buttons[i][j] = addButtonOnField(j, i, value, gameInstance);
                    }
                }

                renderGameField(gameInstance, false);
                sumValueLabel.updateLabel(0);
                movesLeftLabel.updateLabel(moves);
            } catch (IOException | NumberFormatException e) {
                /* validation of the file */
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error reading the file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void exportLayout(Game gameInstance) {
        /* waiting for user's path choice */
        JFileChooser fileChooser = new JFileChooser();
        int exp = fileChooser.showSaveDialog(frame);

        if (exp == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath() + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                /* saving the current game grid in a txt file */
                writer.write(gameInstance.settings.getGameFieldSize() + " | " +  gameInstance.settings.getTotalMoves() + " | " + gameInstance.settings.getTargetValue());
                writer.newLine();
                for (int i = 0; i < gameInstance.settings.getGameFieldSize(); i++) {
                    for (int j = 0; j < gameInstance.settings.getGameFieldSize(); j++) {
                        writer.write(buttons[i][j].getText() + " ");
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                /* validation */
                e.printStackTrace();
            }
        }
    }

    public void startNewGame(boolean isRestart, int[] settings) {
        /* initialization of the new game.
           passing settings properties, in case they are given,
           else starting the game with default settings
         */
        Game gameInstance;
        if (settings != null) {
            gameInstance = new Game(this, settings[0], settings[1], settings[2]);
        } else {
            gameInstance = new Game(this);
        }
        /* if we restart the game - we get the previous button layout,
           else rendering of the random layout */
        if (isRestart) {
            renderGameField(gameInstance, false);
            sumValueLabel.updateLabel(0);
            movesLeftLabel.updateLabel(gameInstance.settings.getTotalMoves());
        } else {
            renderGameField(gameInstance, true);
        }
    }

    private void renderGameField(Game gameInstance, boolean isRandomLayout) {
        /* calculation of the game sizing.
           display of the game frame.
         */
        int frameWidth = gameInstance.settings.getGameFieldSize() * BUTTON_SIZE + PADDING * 2;
        int frameHeight = gameInstance.settings.getGameFieldSize() * BUTTON_SIZE +  PADDING * 4;

        frame.getContentPane().removeAll();
        frame.dispose();

        frame.setSize(frameWidth, frameHeight);
        frame.setLayout(null);
        frame.setVisible(true);

        /* display of the menu and game labels */
        createMenu(gameInstance);
        createLabel("Target value", gameInstance.settings.getTargetValue(), PADDING, PADDING);
        sumValueLabel = createLabel("SUM", 0, PADDING, frameHeight - PADDING - 20);
        movesLeftLabel = createLabel("Moves left", gameInstance.settings.getTotalMoves(),frameWidth - 130, PADDING);

        if (isRandomLayout) buttons = new Button[gameInstance.settings.getGameFieldSize()][gameInstance.settings.getGameFieldSize()];
        renderGrid(isRandomLayout, gameInstance);
    }

    private void createMenu(Game gameInstance) {
        /* display of menu with options */
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
        /* render of the random or given (previous or imported) layout */
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
        /* display of the button in the game field */
        Button button = new Button(value);
        int x = PADDING + (col * BUTTON_SIZE);
        int y = PADDING * 2 + (row * BUTTON_SIZE);
        button.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);
        button.addActionListener(new ButtonGridListener(button, gameInstance));
        frame.add(button);
        return button;
    }

    public void rerenderGridOnMove(Game gameInstance) {
        /* rerender of the grid after the user's move */
        Set<String> possibleMoves = new HashSet<>();

        /* current value - the number on the last clicked button */
        int current = gameInstance.getCurrentSelectedValue();
        /* previous value - the number on the pre-last clicked button */
        int previous = gameInstance.getPreviousSelectedValue();

        for (int i = 0; i < gameInstance.settings.getGameFieldSize(); i++) {
            for (int j = 0; j < gameInstance.settings.getGameFieldSize(); j++) {
                buttons[i][j].setEnabled(true);
                int col = i + 1; int row = j + 1;
                if (previous == 0) {
                    /* if it's the first move -
                       the next move available on buttons
                       in rows and columns divisible by current value
                    */
                    if (col % current == 0 || row % current == 0) {
                        possibleMoves.add(buttons[i][j].getText());
                    } else {
                        buttons[i][j].setEnabled(false);
                    }
                } else {
                    /* if it is not the first move -
                       the next move available on buttons
                       on the intersections of the
                       rows and columns divisible by current or previous value
                    */
                    if ((col % current == 0 || row % current == 0) && (col % previous == 0 || row % previous == 0) ) {
                        possibleMoves.add(buttons[i][j].getText());
                    } else {
                        buttons[i][j].setEnabled(false);
                    }
                }
            }
        }

        /* getting the biggest available value of the next move */
        int maxNextStep = 0;
        for (String element : possibleMoves) {
            maxNextStep = Integer.parseInt(element);
        }

        /* if there are no steps left - game is over */
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
            /* just added colors for available and unavailable buttons */
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
            /* going to the move method on button click and enabling clicked button */
            gameInstance.move(value);
            btn.setEnabled(false);
        }
    }

    class Label extends JLabel {
        /* extended JLabel in order to set the values and titles of the label separately */
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
        /* just label creation, nothing special */
        int width = 140; int height = 20;
        Label label = new Label(title, value);
        label.setBounds(x, y, width, height);
        frame.add(label);
        return label;
    }

    public int alert(String title, String message, int messageType) {
        /* alert that the user gets on win and lose */
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
    private final UIController UI;
    private int movesLeft;
    private int currentSum;
    private int previousSelectedValue;
    private int currentSelectedValue;

    public Game(UIController UI) {
        /* constructor with default settings */
        settings = new Settings();
        this.UI = UI;
        this.movesLeft = settings.totalMoves;
        this.currentSum = 0;
        this.previousSelectedValue = 0;
        this.currentSelectedValue = 0;
    }

    public Game(UIController UI, int gameFieldSize, int totalMoves, int targetValue) {
        /* constructor with given settings */
        settings = new Settings(gameFieldSize, totalMoves, targetValue);
        this.UI = UI;
        this.movesLeft = settings.totalMoves;
        this.currentSum = 0;
        this.previousSelectedValue = 0;
        this.currentSelectedValue = 0;
    }

    public void move(int value) {
        /* probably, the main method of the program.
           updating game variables after the user's choice,
           updating the labels with new variables
           checking if the user won or lost,
           re-rendering the grid if the game is not over yet
         */
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
        /* display of alert if the user won */
        int response = UI.alert("Win", "You won! Congratulations. New Game?", JOptionPane.PLAIN_MESSAGE);
        alertResponseReaction(response);
    }

    public void gameOver() {
        /* display of alert with deviation if the user lost */
        int deviation = settings.targetValue - currentSum;
        int response = UI.alert("Game Over", "No more moves left. Deviation: " + deviation + ". Restart?", JOptionPane.ERROR_MESSAGE);
        alertResponseReaction(response);
    }

    private void alertResponseReaction(int response) {
        if (response == 0) {
            /* "restart" option */
            int[] settings = {this.settings.getGameFieldSize(), this.settings.getTotalMoves(), this.settings.getTargetValue()};
            UI.startNewGame(true, settings);
        } else if (response == 1) {
            /* "new game" option */
            UI.startNewGame(false, null);
        } else {
            /* "exit" option */
            exitGame();
        }
    }

    private void exitGame() {
        System.exit(0);
    }

    /* some getters and setters of the Game below */
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

    class Settings {
        // game settings
        private final int gameFieldSize; // N*N is a field size
        private final int totalMoves;
        private final int targetValue;

        public Settings() {
            /* default settings */
            this.gameFieldSize = 10;
            this.totalMoves = 10;
            this.targetValue = 20;
        }

        public Settings(int gameFieldSize, int totalMoves, int targetValue) {
            /* given settings */
            this.gameFieldSize = gameFieldSize;
            this.totalMoves = totalMoves;
            this.targetValue = targetValue;
        }

        public void applySettings(int fields, int moves, int target) {
            /* restarting the game with new settings */
            int[] settings = {fields, moves, target};
            UI.startNewGame(false, settings);
        }

        /* game modes methods */
        public void setEasyMode() {
            applySettings(10, 15, 20);
        }

        public void setMediumMode() {
            applySettings(10, 12, 25);
        }

        public void setHardMode() {
            applySettings(10, 10, 50);
        }

        /* some setters and getters of the Settings */
        public int getGameFieldSize() {
            return gameFieldSize;
        }

        public int getTotalMoves() {
            return totalMoves;
        }

        public int getTargetValue() {
            return targetValue;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        /* initializing the game */
        UIController uiController = new UIController();
    }
}