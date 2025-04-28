import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Ui implements ActionListener {
    Tictactoe ttt;

    JFrame frame = null;
    JPanel title_panel = null;
    JPanel button_panel = null;
    JLabel textfield = null;

    String[] usernameList;
    String[] emailList;

    JButton[] buttons = new JButton[9];

    boolean player1_turn;

    Ui(String[] usernames, String[] emails) {
        usernameList = usernames;
        emailList = emails;

        startGame(usernameList, emailList);
    }

    private void startGame(String[] names, String[] emails) {
        ttt = new Tictactoe(names, emails);
        setUpUi();
        colorCellsIfNeeded();

        if (ttt.isPlayer1Turn()) {
            setUiMessage(ttt.p1.name + "'s turn (X)");
        } else {
            setUiMessage(ttt.p2.name + "'s turn (O)");
        }
    }

    private void colorCellsIfNeeded() {
        for (int i = 0; i < ttt.cells.length; i++) {
            if (ttt.cells[i] == 'X') {
                buttons[i].setForeground(Color.ORANGE);
                buttons[i].setText("X");
            } else if (ttt.cells[i] == 'O') {
                buttons[i].setForeground(Color.BLUE);
                buttons[i].setText("O");
            }
        }
    }

    private void setUiMessage(String str) {
        textfield.setText(str);
    }

    private void setUpUi() {
        JFrame oldFrame = frame;

        frame = new JFrame();
        title_panel = new JPanel();
        button_panel = new JPanel();
        textfield = new JLabel();

        if (oldFrame != null) {
            oldFrame.dispose();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.getContentPane().setBackground(new Color(50, 50, 50));
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);

        textfield.setBackground(new Color(25, 25, 25));
        textfield.setForeground(new Color(25, 255, 0));
        textfield.setFont(new Font("noto", Font.BOLD, 75));
        textfield.setHorizontalAlignment(JLabel.CENTER);
        textfield.setOpaque(true);

        title_panel.setLayout(new BorderLayout());
        title_panel.setBounds(0, 0, 800, 100);
        button_panel.setLayout(new GridLayout(3, 3));
        button_panel.setBackground(new Color(150, 150, 150));

        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton();
            button_panel.add(buttons[i]);
            buttons[i].setFont(new Font("MV Boli", Font.BOLD, 120));
            buttons[i].setFocusable(false);
            buttons[i].addActionListener(this);
        }

        title_panel.add(textfield);
        frame.add(title_panel, BorderLayout.NORTH);
        frame.add(button_panel);
    }

    private int askPlayAgain() {
        int result = JOptionPane.showConfirmDialog(frame, "Do you want to play again?", "Play again?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < 9; i++) {
            if (e.getSource() == buttons[i]) {

                if (ttt.makeMove(i)) {

                    // we print the opposite of the person who's turn it is now because
                    // `ttt.makeMove()` call has toggled the moveUser
                    if (ttt.isPlayer1Turn()) {
                        buttons[i].setForeground(Color.BLUE);
                        buttons[i].setText("O");
                    } else {
                        buttons[i].setForeground(Color.ORANGE);
                        buttons[i].setText("X");
                    }

                    if (ttt.isPlayer1Turn()) {
                        textfield.setText(ttt.p1.name + "'s turn (X)");
                    } else {
                        textfield.setText(ttt.p2.name + "'s turn (O)");
                    }

                    int playAgain = -1;
                    GameState gs = ttt.checkAndUpdateGameStatus();

                    if (gs == GameState.P1WON) {
                        pWins('X', ttt.getWinCells());
                        playAgain = askPlayAgain();
                    }
                    if (gs == GameState.P2WON) {
                        pWins('O', ttt.getWinCells());
                        playAgain = askPlayAgain();
                    }
                    if (gs == GameState.TIE) {
                        setUiMessage("Game Tied!");

                        for (int j = 0; j < 9; j++) {
                            buttons[j].setBackground(Color.gray);
                            buttons[j].setEnabled(false);
                        }

                        playAgain = askPlayAgain();
                    }

                    if (playAgain == JOptionPane.YES_OPTION) {
                        startGame(usernameList, emailList);
                    }
                    if (playAgain == JOptionPane.NO_OPTION) {
                        System.exit(0);
                    }
                }
            }
        }
    }

    public void pWins(char p, int[] cell) {
        buttons[cell[0]].setBackground(Color.GREEN);
        buttons[cell[1]].setBackground(Color.GREEN);
        buttons[cell[2]].setBackground(Color.GREEN);

        for (int i = 0; i < 9; i++) {
            buttons[i].setEnabled(false);
        }

        if (p == 'X') {
            textfield.setText(ttt.p1.name + " won!");
        } else {
            textfield.setText(ttt.p2.name + " won!");
        }
    }
}
