package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

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
        if (option == 0) {
            Game.restartGame();
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
        JMenuBar mb=new JMenuBar();
        JMenuItem restartItem = new JMenuItem("Restart");
        mb.add(restartItem);
        restartItem.addActionListener(new MenuListener(0));
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
    private static JFrame frame;
    private static Button[][] buttons;
    public static int movesLeft;
    public static int currentSum;
    public static int Previous;
    public static int Current;

    private static Label sumValueLabel;
    private static Label movesLeftLabel;

    // Game settings
    private static final int N = 10; // N*N is a field size
    private static final int totalMoves = 10;
    private static final int targetValue = 20;

    // UI settings
    private static final int BUTTON_SIZE = 50; // width and height of the buttons
    private static final int PADDING = 30; // frame paddings

    public Game() {
        String TITLE = "More or less, less is more";
        frame = new JFrame(TITLE);
        buttons = new Button[N][N];
        Previous = 0;
    }

    public void initialize() {
        Random random = new Random();

        movesLeft = totalMoves;
        currentSum = 0;

        // coordinates
        int x = PADDING; int y = PADDING * 2;

        int frameWidth = N * BUTTON_SIZE + PADDING * 2;
        int frameHeight = N * BUTTON_SIZE +  PADDING * 4;

        UIConstruct.createMenu(frame);

        // add target value label
        UIConstruct.createLabel(frame, "Target value", targetValue, PADDING, PADDING);

        // add buttons on the field
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int randomDigit = random.nextInt(9) + 1;
                buttons[i][j] = new Button(Integer.toString(randomDigit));
                buttons[i][j].setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);
                buttons[i][j].addActionListener(new ButtonClickListener(buttons[i][j]));
                frame.add(buttons[i][j]);
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

    public static void fieldValidation() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                buttons[i][j].setEnabled(true);
                int col = i + 1; int row = j + 1;
                if (Previous == 0) {
                    buttons[i][j].setEnabled(col % Current == 0 || row % Current == 0);
                } else {
                    buttons[i][j].setEnabled((col % Current == 0 && col % Previous == 0) || (row % Current == 0 && row % Previous == 0));
                }
            }
        }
        Previous = Current;
    }

    public static void move() {
        sumValueLabel.value = currentSum;
        movesLeftLabel.value = movesLeft;

        sumValueLabel.updateLabel();
        movesLeftLabel.updateLabel();

        if (currentSum >= targetValue) win();
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
        int response = UIConstruct.alert(frame, "Game Over", "No more moves left. Restart?", JOptionPane.ERROR_MESSAGE);
        if (response == 0) {
            restartGame();
        } else {
            exitGame();
        }
    }

    public static void restartGame() {
        frame.getContentPane().removeAll();
        frame.dispose();

        Game game = new Game();
        game.initialize();
    }

    private static void exitGame() {
        System.exit(0);
    }
}

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
    }
}