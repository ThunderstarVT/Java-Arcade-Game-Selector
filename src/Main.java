import ui.UI;

import java.io.IOException;

import database.Database;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            Database.Init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            new UI();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
