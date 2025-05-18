package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbManager {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private Connection connection;

    public DbManager(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("✅ Database connection established.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Failed to connect to the database.");
        }
    }

    public void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) UNIQUE,
                    email VARCHAR(100) UNIQUE
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("🛠️ Table checked/created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUser(String name, String email) {
        String checkSql = "SELECT 1 FROM users WHERE name = ? OR email = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, name);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("⚠️ User with same name or email already exists.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String insertSql = "INSERT INTO users(name, email) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();
            System.out.println("✅ User added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(int id, String name, String email) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setInt(3, id);
            int rows = stmt.executeUpdate();
            if (rows > 0)
                System.out.println("✅ User updated.");
            else
                System.out.println("⚠️ User not found.");
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // duplicate key
                System.out.println("⚠️ Duplicate name or email.");
            } else {
                e.printStackTrace();
            }
        }
    }

    public void deleteUser(String identifier) {
        String sql = """
                DELETE FROM users
                WHERE id::text = ? OR name = ? OR email = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, identifier);
            int rows = stmt.executeUpdate();
            if (rows > 0)
                System.out.println("✅ User(s) deleted.");
            else
                System.out.println("⚠️ No user found for deletion.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT id, name, email FROM users ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<String> searchUsers(String keyword) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT id, name, email FROM users WHERE name ILIKE ? OR email ILIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
