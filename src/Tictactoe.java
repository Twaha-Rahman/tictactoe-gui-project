import java.util.Arrays;
import java.util.Random;

class Tictactoe {
    protected TictactoePlayer p1, p2;
    protected char[] cells;
    private boolean player1Turn;
    private int[] winCells;

    Db db = new Db();
    private int gameId = -1;

    Tictactoe(String[] names, String[] emails) {
        p1 = new TictactoePlayer(names[0], emails[0]);
        p2 = new TictactoePlayer(names[1], emails[1]);

        winCells = new int[3];
        for (int i = 0; i < 3; i++) {
            winCells[i] = -1;
        }

        try {
            gameId = db.getOngoingGameId(p1.name, p2.name);

            if (gameId != -1) {
                System.out.println("Restoring old ongoing game...");
                // there is an ongoing game between them; restore that game session
                OngoingGameInfo restoredGame = db.getOngoingGameInfo(String.valueOf(gameId));

                if (restoredGame.board_state.isEmpty()) {
                    System.err.println("ERROR: Failed to recover board state from database. Aborting due to fatal error!");
                    System.exit(1);
                }

                restoreCellsFromString(restoredGame.board_state);

                player1Turn = restoredGame.is_player1_turn;

            } else {
                // there's no ongoing game session between them; start a new game between them
                System.out.println("Starting new game...");

                Random random = new Random();
                if (random.nextInt(2) == 0) {
                    player1Turn = true;
                } else {
                    player1Turn = false;
                }

                cells = new char[9];
                Arrays.fill(cells, '.');

                String cellString = getStringifiedCells();
                gameId = db.addGameInfo(getGameStateInt(), names[0], names[1], cellString, player1Turn);
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.toString() + "\nAborting due to error.");

            System.exit(1);
        }
    }

    private String getStringifiedCells() {
        String cellString = "";

        for (int i = 0; i < cells.length; i++) {
            cellString += cells[i];
        }

        return cellString;
    }

    private void restoreCellsFromString(String boardStateString) {
        cells = new char[9];
        String[] boardStateChars = boardStateString.split("");

        for (int i = 0; i < boardStateChars.length; i++) {
            if (boardStateChars[i].equals(".")) {
                cells[i] = '.';
            }
            if (boardStateChars[i].equals("X")) {
                cells[i] = 'X';
            }
            if (boardStateChars[i].equals("O")) {
                cells[i] = 'O';
            }
        }
    }

    public int getGameStateInt() {
        int state = 0;

        GameState gs = check();
        if (gs == GameState.PLAYING) {
            state = 0;
        } else if (gs == GameState.P1WON) {
            state = 1;
        } else if (gs == GameState.P2WON) {
            state = 2;
        } else if (gs == GameState.TIE) {
            state = 3;
        }

        return state;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    public int[] getWinCells() {
        return winCells;
    }

    private void setWinCells(int a, int b, int c) {
        winCells[0] = a;
        winCells[1] = b;
        winCells[2] = c;
    }

    public GameState checkAndUpdateGameStatus() {
        GameState gs = check();

        if (gameId == -1) {
            System.err.println("ERROR: gameId has a value of -1. Aborting due to error!");
            System.exit(1);
        }
        int gsInt = getGameStateInt();

        try {
            db.updateGameStatus(gameId, gsInt);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.toString() + "\nAborting due to error!");
            System.exit(1);
        }

        return gs;
    }

    private GameState check() {
        // check for each row
        for (int i = 0; i < cells.length; i += 3) {
            if ((cells[i] == 'X') && (cells[i + 1] == 'X') && (cells[i + 2] == 'X')) {
                setWinCells(i, i + 1, i + 2);
                return GameState.P1WON;
            }

            if ((cells[i] == 'O') && (cells[i + 1] == 'O') && (cells[i + 2] == 'O')) {
                setWinCells(i, i + 1, i + 2);
                return GameState.P2WON;
            }
        }

        // check for each column
        for (int i = 0; i < 3; i++) {
            if ((cells[i] == 'X') && (cells[i + 3] == 'X') && (cells[i + 6] == 'X')) {
                setWinCells(i, i + 3, i + 6);
                return GameState.P1WON;
            }

            if ((cells[i] == 'O') && (cells[i + 3] == 'O') && (cells[i + 6] == 'O')) {
                setWinCells(i, i + 3, i + 6);
                return GameState.P2WON;
            }
        }

        // check diagonally
        if ((cells[0] == 'X') &&
                (cells[4] == 'X') &&
                (cells[8] == 'X')) {
            setWinCells(0, 4, 8);
            return GameState.P1WON;
        }
        if ((cells[0] == 'O') &&
                (cells[4] == 'O') &&
                (cells[8] == 'O')) {
            setWinCells(0, 4, 8);
            return GameState.P2WON;
        }
        if ((cells[2] == 'X') &&
                (cells[4] == 'X') &&
                (cells[6] == 'X')) {
            setWinCells(2, 4, 6);
            return GameState.P1WON;
        }
        if ((cells[2] == 'O') &&
                (cells[4] == 'O') &&
                (cells[6] == 'O')) {
            setWinCells(2, 4, 6);
            return GameState.P2WON;
        }

        // check whether the game is tied
        for (int i = 0; i < 9; i++) {
            if (cells[i] == '.') {
                return GameState.PLAYING;
            }
        }

        return GameState.TIE;
    }

    public boolean makeMove(int cellIdx) {
        if ((cellIdx < 0) || (cellIdx >= cells.length) || (cells[cellIdx] != '.')) {
            return false;
        }

        if (player1Turn) {
            cells[cellIdx] = 'X';
        } else {
            cells[cellIdx] = 'O';
        }
        player1Turn = !player1Turn;

        if (gameId != -1) {
            try {
                db.updateGameState(gameId, getStringifiedCells(), player1Turn);
            } catch (Exception e) {
                System.err.println("ERROR: " + e.toString() + "\nAborting due to critical error!");

                System.exit(1);
            }
        } else {
            System.err.println("ERROR: gameId has a value of -1. Aborting due to critical error!");

            System.exit(1);
        }

        return true;
    }
}
