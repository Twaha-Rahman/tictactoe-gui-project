import java.sql.*;
import java.util.*;

// To run: java -cp mysql-connector.jar Db.java
//
// SQL for the `user_info` table:
// CREATE TABLE `tictactoe`.`user_info` (`id` INT(255) NOT NULL AUTO_INCREMENT , `name` VARCHAR(1024) NOT NULL , `email` VARCHAR(1024) NOT NULL , `password` VARCHAR(1024) NOT NULL , PRIMARY KEY (`id`), UNIQUE `email` (`email`(1024)), UNIQUE `name` (`name`(1024))) ENGINE = InnoDB;

// SQL for the `games_info` table:
// CREATE TABLE `tictactoe`.`games_info` (`id` INT(255) NOT NULL AUTO_INCREMENT , `is_ongoing` BOOLEAN NOT NULL , `player1` VARCHAR(1024) NOT NULL , `player2` VARCHAR(1024) NOT NULL , `board_state` VARCHAR(1024) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;
//
// SQL to add the `is_player1_turn` column after creating the table:
// ALTER TABLE `games_info` ADD `is_player1_turn` BOOLEAN NOT NULL AFTER `board_state`;
//
// SQL to modify the `is_ongoing` column to `game_status` column after creating the table:
// ALTER TABLE `games_info` CHANGE `is_ongoing` `game_status` INT(255) NOT NULL;

class OngoingGameInfo {
    public String board_state;
    public boolean is_player1_turn;
}

class UserCredentials {
    public String name;
    public String email;
    public String password;
}

class Db {
    public static Connection con;

    Db() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tictactoe", "root", "");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to connect to DB!\nError message: " + e.toString());
            System.exit(1);
        }
    }

    public void addUser(String username, String email, String password) throws SQLException {
        String query = "INSERT INTO user_info (name, email, password) VALUES (?,?,?)";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, username);
        ps.setString(2, email);
        ps.setString(3, password);

        int opStat = ps.executeUpdate();

        if (opStat == 1) {
            System.out.println("Successfully added the user!");
        } else {
            System.out.println("Failed to add the user!");
        }
    }

    public Vector<UserCredentials> getAllUserCredentials() throws SQLException {
        String query = "SELECT `name`, `email`, `password` FROM `user_info`";

        PreparedStatement ps = con.prepareStatement(query);

        ResultSet res = ps.executeQuery();

        Vector<UserCredentials> userInfos = new Vector<>();

        while (res.next()) {
            UserCredentials usr = new UserCredentials();
            usr.name = res.getString("name");
            usr.email = res.getString("email");
            usr.password = res.getString("password");

            userInfos.add(usr);
        }

        return userInfos;
    }

    public int addGameInfo(int gameStatus, String name1, String name2, String cellsStringified,
                           boolean isPlayer1Turn) throws SQLException {

        String query = "INSERT INTO games_info (game_status, player1, player2, board_state, is_player1_turn) VALUES (?,?,?,?,?)";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, String.valueOf(gameStatus));
        ps.setString(2, name1);
        ps.setString(3, name2);
        ps.setString(4, cellsStringified);
        ps.setString(5, (isPlayer1Turn) ? "1" : "0");

        int opStat = ps.executeUpdate();

        if (opStat == 1) {
            System.out.println("Successfully added the game state!");
        } else {
            System.out.println("Failed to add the game state!");
        }

        query = "SELECT MAX(id) FROM `games_info`";
        ps = con.prepareStatement(query);

        ResultSet res = ps.executeQuery();
        int id = -1;
        if (res.next()) {
            id = res.getInt("MAX(id)");
        }

        return id;
    }

    public void updateGameState(int gameId, String cellsStringified,
                                boolean isPlayer1Turn) throws SQLException {

        String query = "UPDATE games_info SET `board_state`=?, `is_player1_turn`=?  WHERE id=?";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, cellsStringified);
        ps.setString(2, (isPlayer1Turn) ? "1" : "0");
        ps.setString(3, String.valueOf(gameId));

        int opStat = ps.executeUpdate();

        if (opStat == 1) {
            System.out.println("Successfully updated the game state!");
        } else {
            System.out.println("Failed to update the game state!");
        }
    }

    public void updateGameStatus(int gameId, int gameStatus) throws SQLException {

        String query = "UPDATE games_info SET `game_status`=?  WHERE id=?";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, String.valueOf(gameStatus));
        ps.setString(2, String.valueOf(gameId));

        int opStat = ps.executeUpdate();

        if (opStat == 1) {
            System.out.println("Successfully updated the game status!");
        } else {
            System.out.println("Failed to update the game status!");
        }
    }

    public int getOngoingGameId(String player1, String player2) throws SQLException {
        String query = "SELECT `id`,`player1`,`player2`,`game_status` FROM `games_info`";

        PreparedStatement ps = con.prepareStatement(query);

        ResultSet res = ps.executeQuery();

        int searchedId = -1;
        while (res.next()) {
            int gs = res.getInt("game_status");
            if (gs != 0) {
                continue;
            }

            int id = res.getInt("id");

            String n1 = res.getString("player1");
            String n2 = res.getString("player2");

            if (((n1.equals(player1)) && (n2.equals(player2))) || ((n2.equals(player1)) && (n1.equals(player2)))) {
                searchedId = id;
            }
        }

        return searchedId;
    }

    public OngoingGameInfo getOngoingGameInfo(String gameId) throws SQLException {
        String query = "SELECT `board_state`,`is_player1_turn` FROM `games_info` where `id`=?";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, String.valueOf(gameId));
        ResultSet res = ps.executeQuery();

        OngoingGameInfo ogi = new OngoingGameInfo();
        ogi.board_state = "";

        if (res.next()) {
            ogi.board_state = res.getString("board_state");
            ogi.is_player1_turn = res.getBoolean("is_player1_turn");
        }

        return ogi;
    }

}
