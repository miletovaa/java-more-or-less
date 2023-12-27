package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

class Button {
    boolean isActive;
    JButton j;
    String value;

    public Button(JButton button) {
        this(true, button);
    }
    public Button(boolean isActive, JButton j) {
        this.isActive = isActive;
        this.j = j;
        this.value = j.getText();
    }
}

class ButtonClickListener implements ActionListener {
    private final String value;

    public ButtonClickListener(String value) {
        this.value = value;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Game.movesLeft -= 1;
        Game.currentSum += Integer.parseInt(value);
        Game.move();
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
    public static Label createLabel(JFrame frame, String title, int value, int x, int y) {
        int width = 140; int height = 20;
        Label label = new Label(new JLabel(), title, value);
        label.j.setBounds(x, y, width, height);
        frame.add(label.j);
        return label;
    }
}

class Game {
    private static JFrame frame;
    private final Button[][] buttons;
    public static int movesLeft;
    public static int currentSum;

    private static Label sumValueLabel;
    private static Label movesLeftLabel;

    // Game settings
    private static final int N = 10; // N*N is a field size
    private static final int totalMoves = 3;
    private static final int targetValue = 100;

    // UI settings
    private static final int BUTTON_SIZE = 50; // width and height of the buttons
    private static final int PADDING = 30; // frame paddings

    public Game() {
        String TITLE = "More or less, less is more";
        frame = new JFrame(TITLE);
        buttons = new Button[N][N];
    }

    public void initialize() {
        Random random = new Random();

        movesLeft = totalMoves;
        currentSum = 0;

        // coordinates
        int x = PADDING; int y = PADDING * 2;

        int frameWidth = N * BUTTON_SIZE + PADDING * 2;
        int frameHeight = N * BUTTON_SIZE +  PADDING * 4;

        // add target value label
        UIConstruct.createLabel(frame, "Target value", targetValue, PADDING, PADDING);

        // add buttons on the field
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int randomDigit = random.nextInt(9) + 1;
                buttons[i][j] = new Button(new JButton(Integer.toString(randomDigit)));
                buttons[i][j].j.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);
                buttons[i][j].j.addActionListener(new ButtonClickListener(buttons[i][j].value));
                frame.add(buttons[i][j].j);
                x += BUTTON_SIZE;
            }
            y += BUTTON_SIZE;
            x = PADDING;
        }

        // add summary of the button values label
        sumValueLabel = UIConstruct.createLabel(frame, "SUM", currentSum, PADDING, frameHeight - PADDING - 20);

        // add number of moves left label
        movesLeftLabel = UIConstruct.createLabel(frame, "Moves left", totalMoves, frameWidth - 130, PADDING);

        frame.setSize(frameWidth, frameHeight);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    public static void move() {
        sumValueLabel.value = currentSum;
        movesLeftLabel.value = movesLeft;

        sumValueLabel.updateLabel();
        movesLeftLabel.updateLabel();

        // TODO: add game logic

        if (movesLeft == 0) gameOver();
    }

    private static void gameOver() {
        JOptionPane.showMessageDialog(frame, "Game Over! No more moves left.", "Game Over", JOptionPane.ERROR_MESSAGE);
        // TODO: restart game and exit
    }
}

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
    }
}