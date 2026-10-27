
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final User currentAdmin;

    public AdminMenu(
            Scanner scanner,
            UserDao userDao,
            User currentAdmin
    ) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.currentAdmin = currentAdmin;
    }

    public void start() {
        while (true) {
            System.out.println("\n===== ADMINISTRATOR MENU =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Logout");

            int choice = readIntInput("Choose an option: ");

            try {
                switch (choice) {
                    case 0 -> System.out.println("Cancelled.");
                    case 1 -> displayUsers(
                            userDao.getAllUsers(), "ALL SYSTEM USERS");
                    case 2 -> displayUsers(
                            userDao.getPendingUsers(), "PENDING REGISTRATIONS");
                    case 3 -> approveUser();
                    case 4 -> updateUser();
                    case 5 -> {
                        System.out.println("Logged out.");
                        return;
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void approveUser() throws SQLException {
        List<User> pendingUsers = userDao.getPendingUsers();
        displayUsers(pendingUsers, "PENDING REGISTRATIONS");

        if (pendingUsers.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }

        int userId = readIntInput(
                "Enter User ID to approve (0 to cancel): ");

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.id() == userId);

        if (!exists) {
            System.out.println("User ID not found in pending registrations.");
            return;
        }

        if (userDao.approveUser(userId)) {
            System.out.println(
                    "SUCCESS: User ID " + userId + " has been approved!");
            System.out.println("Account status: APPROVED");
        } else {
            System.out.println("Approval failed.");
        }
    }

    private void updateUser() throws SQLException {
        List<User> users = userDao.getAllUsers();
        displayUsers(users, "UPDATE USER");

        if (users.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        int targetUserId = readIntInput(
                "Enter target User ID (0 to cancel): ");

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User target = users.stream()
                .filter(user -> user.id() == targetUserId)
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.out.println("User ID not found.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println(
                    "WARNING: Modifying the currently active admin account "
                            + "is restricted.");
            return;
        }

        String role = readValidRole();
        if (role == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String status = readValidStatus();
        if (status == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(targetUserId, role, status)) {
            System.out.println(
                    "SUCCESS: User ID " + targetUserId + " updated!");
            System.out.println("New role: " + role);
            System.out.println("New status: " + status);
        } else {
            System.out.println("Update failed. No user was changed.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= 0) {
                    return value;
                }
                System.out.println("Enter zero or a positive number.");
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a whole number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println(
                    "Valid roles: ADMINISTRATOR, RECEPTIONIST, GROOMER, CUSTOMER");
            System.out.print("Enter new role (0 to cancel): ");

            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0")) {
                return null;
            }

            if (List.of(
                    "ADMINISTRATOR", "RECEPTIONIST", "GROOMER", "CUSTOMER"
            ).contains(role)) {
                return role;
            }

            System.out.println("Invalid role. Please try again.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("Valid statuses: APPROVED, PENDING");
            System.out.print("Enter new status (0 to cancel): ");

            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0")) {
                return null;
            }

            if (List.of("APPROVED", "PENDING").contains(status)) {
                return status;
            }

            System.out.println("Invalid status. Please try again.");
        }
    }

    private void displayUsers(List<User> users, String title) {
        System.out.println("\n===== " + title + " =====");
        System.out.printf(
                "%-8s %-20s %-18s %-12s%n",
                "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(
                "------------------------------------------------------------");

        if (users.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (User user : users) {
            System.out.printf(
                    "%-8d %-20s %-18s %-12s%n",
                    user.id(),
                    user.username(),
                    user.role(),
                    user.status());
        }
    }
}
