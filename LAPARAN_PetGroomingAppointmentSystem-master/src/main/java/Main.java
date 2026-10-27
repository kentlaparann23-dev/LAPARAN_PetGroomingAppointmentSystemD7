
import dao.DatabaseInitializer;
import dao.UserDao;
import ui.Menu;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
        } catch (SQLException e) {
            System.out.println(
                    "Could not initialize database: " + e.getMessage()
            );
            return;
        }

        UserDao userDao = new UserDao();

        try (Scanner scanner = new Scanner(System.in)) {
            new Menu(scanner, userDao).start();
        }
    }
}
