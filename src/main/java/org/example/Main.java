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
    private final int i; // column
    private final int j; // row

    public ButtonClickListener(String value, int i, int j) {
        this.value = value;
        this.i = i;
        this.j = j;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO: process button click
        System.out.print("clicked button " + value + " (" + i + ";" + j + ")\n");
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
    private final JFrame frame;
    private final Button[][] buttons;

    // Game settings
    private static final int N = 10; // N*N is a field size
    private static final int totalMoves = 10;

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

        // coordinates
        int x = PADDING; int y = PADDING * 2;

        int frameWidth = N * BUTTON_SIZE + PADDING * 2;
        int frameHeight = N * BUTTON_SIZE +  PADDING * 4;

        // add target value label
        int targetValue = 100;
        Label targetValueLabel = UIConstruct.createLabel(frame, "Target value", targetValue, PADDING, PADDING);

        int sumOnBtn = 0; // current sum of numbers on the buttons

        // add buttons on the field
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int randomDigit = random.nextInt(9) + 1;
                buttons[i][j] = new Button(new JButton(Integer.toString(randomDigit)));
                buttons[i][j].j.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);
                buttons[i][j].j.addActionListener(new ButtonClickListener(buttons[i][j].value, i, j));
                frame.add(buttons[i][j].j);
                x += BUTTON_SIZE;
            }
            y += BUTTON_SIZE;
            x = PADDING;
        }

        // add summary of the button values label
        Label sumValueLabel = UIConstruct.createLabel(frame, "SUM", sumOnBtn, PADDING, frameHeight - PADDING - 20);

        // add number of moves left label
        Label movesLeftLabel = UIConstruct.createLabel(frame, "Moves left", totalMoves, frameWidth - 130, PADDING);

        frame.setSize(frameWidth, frameHeight);
        frame.setLayout(null);
        frame.setVisible(true);
    }
}

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
    }
}