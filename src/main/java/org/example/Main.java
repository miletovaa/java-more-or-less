package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;
import java.util.StringTokenizer;

class Button extends JButton {
    private static final Color activeBtn = new Color(98, 240, 150);
    private static final Color inactiveBtn = new Color(209, 209, 209);

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

class ButtonClickListener implements ActionListener {
    private final JButton btn;
    private final int value;

    public ButtonClickListener(JButton btn) {
        this.btn = btn;
        this.value = Integer.parseInt(btn.getText());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Game.movesLeft -= 1;
        Game.currentSum += value;
        Game.Current = value;
        Game.move();
        btn.setEnabled(false);
    }
}

class MenuListener implements ActionListener {
    private final int option;

    public MenuListener(int option) {
        this.option = option;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (option){
            case 0:
                Game.restartGame();
                break;
            case 1:
                Game.Settings.manualSettings = new Game.Settings.ManualSettings(Game.frame);
                Game.Settings.manualSettings.setVisible(true);
                break;
            case 2:
                Game.Settings.setEasyMode();
                break;
            case 3:
                Game.Settings.setMediumMode();
                break;
            case 4:
                Game.Settings.setHardMode();
                break;
            case 5:
                Game.exportLayout();
                break;
            case 6:
                Game.importLayout();
                break;
        }
    }
}

class Label {
    JLabel j;
    String title;
    int value;

    public Label(JLabel j, String title, int value) {
        this.j = j;
        this.title = title;
        this.value = value;
        updateLabel();
    }

    public void updateLabel() {
        this.j.setText(title + ": " + value);
    }
}

class UIConstruct {
    public static void createMenu(JFrame frame) {
        JMenuBar mb = new JMenuBar();

        JMenuItem restartItem = new JMenuItem("Restart");
        mb.add(restartItem);
        restartItem.addActionListener(new MenuListener(0));

        JMenuItem settingsItem = new JMenuItem("Manual settings");
        mb.add(settingsItem);
        settingsItem.addActionListener(new MenuListener(1));

        JMenu modeSubmenu = new JMenu("Difficulty");
        JMenuItem easyItem = new JMenuItem("Easy");
        easyItem.addActionListener(new MenuListener(2));
        JMenuItem mediumItem = new JMenuItem("Medium");
        mediumItem.addActionListener(new MenuListener(3));
        JMenuItem hardItem = new JMenuItem("Hard");
        hardItem.addActionListener(new MenuListener(4));
        modeSubmenu.add(easyItem); modeSubmenu.add(mediumItem); modeSubmenu.add(hardItem);
        mb.add(modeSubmenu);

        JMenu layoutSubmenu = new JMenu("Layout");
        JMenuItem exportItem = new JMenuItem("Export");
        exportItem.addActionListener(new MenuListener(5));
        layoutSubmenu.add(exportItem);
        JMenuItem importItem = new JMenuItem("Import");
        importItem.addActionListener(new MenuListener(6));
        layoutSubmenu.add(importItem);
        mb.add(layoutSubmenu);

        frame.setJMenuBar(mb);
    }

    public static int alert(JFrame frame, String title, String message, int messageType) {
        Object[] options = { "Restart", "Exit" };
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

    public static Label createLabel(JFrame frame, String title, int value, int x, int y) {
        int width = 140; int height = 20;
        Label label = new Label(new JLabel(), title, value);
        label.j.setBounds(x, y, width, height);
        frame.add(label.j);
        return label;
    }
}

class Game {
    static JFrame frame;
    private static Button[][] buttons;
    public static int movesLeft;
    public static int currentSum;
    public static int Previous;
    public static int Current;

    private static Label sumValueLabel;
    private static Label movesLeftLabel;

    // UI settings
    private static final int BUTTON_SIZE = 50; // width and height of the buttons
    private static final int PADDING = 30; // frame paddings

    public Game() {
        String TITLE = "More or less, less is more";
        frame = new JFrame(TITLE);
        buttons = new Button[Settings.N][Settings.N];
    }

    public static void initialize(boolean setRandomly) {
        Previous = 0;
        currentSum = 0;
        movesLeft = Settings.totalMoves;

        int frameWidth = Settings.N * BUTTON_SIZE + PADDING * 2;
        int frameHeight = Settings.N * BUTTON_SIZE +  PADDING * 4;

        UIConstruct.createMenu(frame);
        UIConstruct.createLabel(frame, "Target value", Settings.targetValue, PADDING, PADDING);
        sumValueLabel = UIConstruct.createLabel(frame, "SUM", currentSum, PADDING, frameHeight - PADDING - 20);
        movesLeftLabel = UIConstruct.createLabel(frame, "Moves left", Settings.totalMoves, frameWidth - 130, PADDING);

        // add buttons on the field
        if (setRandomly) {
            initializeButtonsRandomly();
        }

        frame.setSize(frameWidth, frameHeight);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    public static void initializeButtonsRandomly() {
        Random random = new Random();
        for (int i = 0; i < Settings.N; i++) {
            for (int j = 0; j < Settings.N; j++) {
                int randomDigit = random.nextInt(9) + 1;
                addButtonOnField(j, i, Integer.toString(randomDigit));
            }
        }
    }

    private static void addButtonOnField(int col, int row, String value) {
        buttons[row][col] = new Button(value);
        int x = PADDING + (col * BUTTON_SIZE);
        int y = PADDING * 2 + (row * BUTTON_SIZE);
        buttons[row][col].setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);
        buttons[row][col].addActionListener(new ButtonClickListener(buttons[row][col]));
        frame.add(buttons[row][col]);
    }

    public static void fieldValidation() {
        Set<String> possibleMoves = new HashSet<>();

        for (int i = 0; i < Settings.N; i++) {
            for (int j = 0; j < Settings.N; j++) {
                buttons[i][j].setEnabled(true);
                int col = i + 1; int row = j + 1;
                if (Previous == 0) {
                    if (col % Current == 0 || row % Current == 0) {
                        possibleMoves.add(buttons[i][j].getText());
                    } else {
                        buttons[i][j].setEnabled(false);
                    }
                } else {
                    if ((col % Current == 0 && col % Previous == 0) || (row % Current == 0 && row % Previous == 0)) {
                        possibleMoves.add(buttons[i][j].getText());
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
            (maxNextStep + currentSum < Settings.targetValue && movesLeft == 1)
        ) {
            gameOver();
        }

        Previous = Current;
    }

    public static void move() {
        sumValueLabel.value = currentSum;
        movesLeftLabel.value = movesLeft;

        sumValueLabel.updateLabel();
        movesLeftLabel.updateLabel();

        if (currentSum >= Settings.targetValue) win();
        else {
            if (movesLeft == 0) gameOver();
            fieldValidation();
        }
    }

    private static void win() {
        int response = UIConstruct.alert(frame, "Win", "You won! Congratulations. Restart?", JOptionPane.PLAIN_MESSAGE);
        if (response == 0) {
            restartGame();
        } else {
            exitGame();
        }
    }

    private static void gameOver() {
        int deviation = Settings.targetValue - currentSum;
        int response = UIConstruct.alert(frame, "Game Over", "No more moves left. Deviation: " + deviation + ". Restart?", JOptionPane.ERROR_MESSAGE);
        if (response == 0) {
            restartGame();
        } else {
            exitGame();
        }
    }

    public static void restartGame() {
        frame.getContentPane().removeAll();
        frame.dispose();

        initialize(true);
    }

    private static void exitGame() {
        System.exit(0);
    }
    static class Settings {
        // Game settings
        private static int N = 10; // N*N is a field size
        private static int totalMoves = 10;
        private static int targetValue = 20;
        static ManualSettings manualSettings;
        static class ManualSettings extends JDialog {
            JTextField fieldSizeField;
            JTextField movesField;
            JTextField targetValueField;
            JButton applyBtn;

            public ManualSettings(JFrame parent) {
                super(parent, "Manual Settings", true);
                setLayout(new FlowLayout());

                add(new JLabel("Field Size:"));
                fieldSizeField = new JTextField(Integer.toString(N), 10);
                add(fieldSizeField);

                add(new JLabel("Number of Moves:"));
                movesField = new JTextField(Integer.toString(totalMoves), 10);
                add(movesField);

                add(new JLabel("Target Value:"));
                targetValueField = new JTextField(Integer.toString(targetValue), 10);
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

                            applySettings(fields, moves, target);

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

        public static void setEasyMode() {
            applySettings(10, 15, 20);
        }

        public static void setMediumMode() {
            applySettings(10, 12, 25);
        }

        public static void setHardMode() {
            applySettings(10, 10, 30);
        }

        private static void applySettings(int fields, int moves, int target) {
            N = fields;
            totalMoves = moves;
            targetValue = target;

            restartGame();
        }
    }

    public static void exportLayout() {
        JFileChooser fileChooser = new JFileChooser();
        int exp = fileChooser.showSaveDialog(frame);

        if (exp == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath() + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                writer.write(Settings.N + " | " +  Settings.totalMoves + " | " + Settings.targetValue);
                writer.newLine();
                for (int i = 0; i < Settings.N; i++) {
                    for (int j = 0; j < Settings.N; j++) {
                        writer.write(buttons[i][j].getText() + " ");
                    }
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void importLayout() {
        JFileChooser fileChooser = new JFileChooser();
        int imp = fileChooser.showSaveDialog(frame);

        if (imp == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();

            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                frame.getContentPane().removeAll();
                frame.dispose();

                initialize(false);

                String firstLine = reader.readLine();
                StringTokenizer firstLineTokens = new StringTokenizer(firstLine, "|");
                Settings.N = Integer.parseInt(firstLineTokens.nextToken().trim());
                Settings.totalMoves = Integer.parseInt(firstLineTokens.nextToken().trim());
                Settings.targetValue = Integer.parseInt(firstLineTokens.nextToken().trim());

                for (int i = 0; i < Settings.N; i++) {
                    String line = reader.readLine();
                    StringTokenizer lineTokens = new StringTokenizer(line, " ");
                    for (int j = 0; j < Settings.N; j++) {
                        String value = lineTokens.nextToken().trim();
                        addButtonOnField(j, i, value);
                    }
                    System.out.println();
                }

            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error reading the file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        new Game();
        Game.initialize(true);
    }
}