
import dao.DatabaseInitializer;
import dao.UserDao;

import java.sql.SQLException;

public class AdminSetup {

    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();

            UserDao userDao = new UserDao();

            userDao.createAdminAccount(
                    "admin",
                    "Admin123!",
                    "System Administrator"
            );

        } catch (SQLException e) {
            System.out.println("Setup failed: " + e.getMessage());
        }
    }
}
