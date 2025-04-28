class TictactoePlayer extends  Player {
    TictactoePlayer(String n, String e) {
        if (n.isEmpty()) {
            System.err.println("ERROR: Trying to create a Tictactoe instance with an empty string as the name!");
            System.exit(1);
        }
        if (e.isEmpty() ) {
            System.err.println("ERROR: Trying to create a Tictactoe instance with an empty string as the email!");
            System.exit(1);
        }

        name = n;
        email = e;
    }
}
