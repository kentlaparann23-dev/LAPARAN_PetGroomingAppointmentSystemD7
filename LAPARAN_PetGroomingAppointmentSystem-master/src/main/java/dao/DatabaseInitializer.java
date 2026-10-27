
package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {
        UserDao userDao = new UserDao();
        userDao.createTable();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            System.out.println("Database initialized successfully.");
        }
    }
}
