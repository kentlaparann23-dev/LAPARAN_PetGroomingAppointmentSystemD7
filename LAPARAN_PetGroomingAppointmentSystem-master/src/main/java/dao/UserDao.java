
package dao;

import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class UserDao {

    // Create the users table
    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL
                    CHECK (role IN (
                        'ADMINISTRATOR',
                        'RECEPTIONIST',
                        'GROOMER',
                        'CUSTOMER'
                    )),
                email TEXT,
                account_status TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (account_status IN (
                        'PENDING',
                        'APPROVED',
                        'REJECTED',
                        'INACTIVE'
                    )),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    // Create the administrator account
    public void createAdminAccount(
            String username,
            String password,
            String fullName
    ) throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO users
                (username, password_hash, full_name, role, account_status)
            VALUES (?, ?, ?, 'ADMINISTRATOR', 'APPROVED')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, hashPassword(password));
            statement.setString(3, fullName);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println(
                        "Administrator account created successfully."
                );
            } else {
                System.out.println(
                        "Admin username already exists."
                );
            }
        }
    }

    // Register a customer account
    public boolean register(
            String username,
            String password,
            String fullName,
            String email
    ) throws SQLException {

        String sql = """
            INSERT INTO users
                (username, password_hash, full_name, role, email,
                 account_status)
            VALUES (?, ?, ?, 'CUSTOMER', ?, 'PENDING')
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());
            statement.setString(2, hashPassword(password));
            statement.setString(3, fullName.trim());
            statement.setString(
                    4,
                    email == null || email.isBlank()
                            ? null
                            : email.trim()
            );

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                return false;
            }
            throw e;
        }
    }

    // Log in an existing user
    public User login(
            String username,
            String password
    ) throws SQLException {

        String sql = """
            SELECT user_id, username, full_name, role, account_status
            FROM users
            WHERE username = ? AND password_hash = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());
            statement.setString(2, hashPassword(password));

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                User user = mapUser(result);

                if (!"APPROVED".equals(user.status())) {
                    return null;
                }

                return user;
            }
        }
    }

    // Retrieve all users
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
            SELECT user_id, username, full_name, role, account_status
            FROM users
            ORDER BY user_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(mapUser(result));
            }
        }

        return users;
    }

    // Retrieve pending users
    public List<User> getPendingUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = """
            SELECT user_id, username, full_name, role, account_status
            FROM users
            WHERE account_status = 'PENDING'
            ORDER BY user_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(mapUser(result));
            }
        }

        return users;
    }

    // Approve a pending account
    public boolean approveUser(int userId) throws SQLException {
        String sql = """
            UPDATE users
            SET account_status = 'APPROVED'
            WHERE user_id = ?
              AND account_status = 'PENDING'
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    // Update a user's role and account status
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus
    ) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }

        if (newRole == null || newStatus == null) {
            throw new IllegalArgumentException(
                    "Role and status cannot be empty."
            );
        }

        newRole = newRole.trim().toUpperCase();
        newStatus = newStatus.trim().toUpperCase();

        List<String> validRoles = List.of(
                "ADMINISTRATOR",
                "RECEPTIONIST",
                "GROOMER",
                "CUSTOMER"
        );

        List<String> validStatuses = List.of(
                "APPROVED",
                "PENDING"
        );

        if (!validRoles.contains(newRole)) {
            throw new IllegalArgumentException(
                    "Invalid role. Use ADMINISTRATOR, RECEPTIONIST, "
                            + "GROOMER, or CUSTOMER."
            );
        }

        if (!validStatuses.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status. Use APPROVED or PENDING."
            );
        }

        String sql = """
            UPDATE users
            SET role = ?, account_status = ?
            WHERE user_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, newRole);
            statement.setString(2, newStatus);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;
        }
    }

    // Convert a database row into a User object
    private User mapUser(ResultSet result) throws SQLException {
        return new User(
                result.getInt("user_id"),
                result.getString("username"),
                result.getString("full_name"),
                result.getString("role"),
                result.getString("account_status")
        );
    }

    // Hash passwords using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Unable to hash password.", e
            );
        }
    }
}
