
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Menu {

    private final UserDao userDao;
    private final Scanner scanner;

    public Menu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== PET GROOMING APPOINTMENT SYSTEM ===");
            System.out.println("1. Register Customer Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> register();
                    case "2" -> login();
                    case "3" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void register() throws SQLException {
        System.out.println("\n=== CUSTOMER REGISTRATION ===");

        System.out.print("Full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Email (optional): ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (fullName.isBlank() || username.isBlank()) {
            System.out.println("Full name and username are required.");
            return;
        }

        if (password.length() < 8) {
            System.out.println("Password must contain at least 8 characters.");
            return;
        }

        if (userDao.register(username, password, fullName, email)) {
            System.out.println("Registration successful!");
            System.out.println("Role: CUSTOMER");
            System.out.println("Account status: PENDING");
            System.out.println("Your account is awaiting approval.");
        } else {
            System.out.println("That username is already taken.");
        }
    }

    private void login() throws SQLException {
        System.out.println("\n=== LOGIN ===");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Incorrect username or password.");
            return;
        }

        if (!"APPROVED".equalsIgnoreCase(user.status())) {
            System.out.println(
                    "Login denied. Account status: " + user.status()
            );

            if ("PENDING".equalsIgnoreCase(user.status())) {
                System.out.println(
                        "Your account is awaiting administrator approval."
                );
            } else if ("REJECTED".equalsIgnoreCase(user.status())) {
                System.out.println(
                        "Your registration was rejected. Contact the administrator."
                );
            } else if ("INACTIVE".equalsIgnoreCase(user.status())) {
                System.out.println(
                        "Your account is inactive. Contact the administrator."
                );
            }

            return;
        }

        System.out.println(
                "Login successful. Welcome, " + user.fullName() + "!"
        );
        System.out.println("Role: " + user.role());

        if ("ADMINISTRATOR".equalsIgnoreCase(user.role())) {
            new AdminMenu(scanner, userDao, user).start();
        } else {
            System.out.println(
                    "You are logged in, but your role-specific menu "
                            + "has not been connected yet."
            );
        }
    }
}
