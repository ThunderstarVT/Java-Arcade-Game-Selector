package database;

import Util.Reference;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:data/database.db";

    private static final String createTableStatement = "CREATE TABLE IF NOT EXISTS gameList (id INTEGER PRIMARY KEY, name TEXT NOT NULL, path TEXT NOT NULL UNIQUE, images TEXT)";
    private static final String addEntryStatement = "INSERT INTO gameList (name, path, images) VALUES (?, ?, ?)";
    private static final String getEntriesStatement = "SELECT * FROM gameList ORDER BY name";

    public static void Init() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        Statement stmt = conn.createStatement();

        stmt.execute(createTableStatement);

        stmt.close();
        conn.close();
    }

    public static boolean AddGame(String name, File path, List<File> images) {
        String pathStr = path.getAbsolutePath();
        String imagesStr = Reference.gson.toJson(images.stream().map(File::getAbsolutePath).toList());

        try {
            Connection conn = DriverManager.getConnection(URL);
            PreparedStatement stmt = conn.prepareStatement(addEntryStatement);

            stmt.setString(1, name);
            stmt.setString(2, pathStr);
            stmt.setString(3, imagesStr);

            stmt.execute();

            stmt.close();
            conn.close();
        } catch (SQLException e){
            return false;
        }

        return true;
    }

    public static boolean AddGame(String name, File path) {
        return AddGame(name, path, List.of());
    }


    public static List<Reference.Game> GetAllGames() {
        List<Reference.Game> toReturn = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection(URL);
            Statement stmt = conn.createStatement();

            ResultSet results = stmt.executeQuery(getEntriesStatement);

            while (results.next()) {
                String name = results.getString("name");
                String path = results.getString("path");
                List<String> images = Reference.gson.fromJson(results.getString("images"), List.class);

                toReturn.add(new Reference.Game(name, new File(path),
                        images.stream().map(File::new).toList()));
            }

            results.close();

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return toReturn;
    }
}
