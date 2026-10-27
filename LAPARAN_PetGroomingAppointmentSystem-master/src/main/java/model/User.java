package model;

public record User(
        int id,
        String username,
        String fullName,
        String role,
        String status
) {
}
