package com.campuscent;

import com.campuscent.utils.AuthenticationHelper;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private final String url;

    public DatabaseHelper(String filePath) {
        // SQLite database URL
        this.url = "jdbc:sqlite:" + filePath;
    }

    // Test database connection
    public void testConnection() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Initialize the database with required tables
    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // Create Users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                );
            """;
            stmt.execute(createUsersTable);

            // Create Goals table
            String createGoalsTable = """
                CREATE TABLE IF NOT EXISTS Goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    targetAmount REAL NOT NULL,
                    currentAmount REAL DEFAULT 0,
                    year INTEGER NOT NULL,
                    FOREIGN KEY (username) REFERENCES Users(username)
                );
            """;
            stmt.execute(createGoalsTable);

            // Create Transactions table
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS Transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    type TEXT NOT NULL,
                    category TEXT,
                    FOREIGN KEY (username) REFERENCES Users(username)
                );
            """;
            stmt.execute(createTransactionsTable);

            // Create Investments table
            String createInvestmentsTable = """
                CREATE TABLE IF NOT EXISTS Investments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    duration INTEGER NOT NULL,
                    rate REAL NOT NULL,
                    projectedReturn REAL NOT NULL,
                    FOREIGN KEY (username) REFERENCES Users(username)
                );
            """;
            stmt.execute(createInvestmentsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add a user to the database
    public void addUser(String username, String plainPassword) {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashedPassword = AuthenticationHelper.hashPassword(plainPassword); // Hash password
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Retrieve user by username
    public User getUserByUsername(String username) {
        String sql = "SELECT username, password FROM Users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String password = rs.getString("password");
                return new User(username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // User not found
    }

    // Retrieve all users
    public List<User> getUsers() {
        String sql = "SELECT * FROM Users";
        List<User> users = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                users.add(new User(username, password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    // Add a financial transaction (income or expense)
    public void addTransaction(FinancialEntry entry, String username) {
        String sql = "INSERT INTO Transactions (username, amount, date, type, category) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setDouble(2, entry.getAmount());
            pstmt.setString(3, entry.getDate().toString());
            pstmt.setString(4, entry instanceof Income ? "Income" : "Expense");

            if (entry instanceof Expense) {
                pstmt.setString(5, ((Expense) entry).getCategory().toString()); // Store Expense category
            } else if (entry instanceof Income) {
                pstmt.setString(5, ((Income) entry).getCategory().name()); // Store IncomeCategory
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve all transactions for a user
    public List<FinancialEntry> getTransactions(String username) {
        String sql = "SELECT * FROM Transactions WHERE username = ?";
        List<FinancialEntry> transactions = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                String type = rs.getString("type");
                String category = rs.getString("category");

                if ("Expense".equalsIgnoreCase(type)) {
                    transactions.add(new Expense(amount, date, Category.valueOf(category)));
                } else if ("Income".equalsIgnoreCase(type)) {
                    transactions.add(new Income(amount, date, IncomeCategory.valueOf(category)));
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error retrieving transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    // Check if a savings goal exists for the current year
    public boolean hasYearlyGoal(String username, int year) {
        String sql = "SELECT COUNT(*) AS count FROM Goals WHERE username = ? AND year= ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0; // Check if any goal exists for the current year
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to no goal
    }

    // Add a savings goal
    public void addGoal(String username, double targetAmount, double currentAmount, int year) {
        String sql = "INSERT INTO Goals (username, targetAmount, currentAmount, year) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setDouble(2, targetAmount);
            pstmt.setDouble(3, currentAmount);
            pstmt.setInt(4, year);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve all goals for a user
    public List<Goal> getGoals(String username) {
        String sql = "SELECT * FROM Goals WHERE username = ?";
        List<Goal> goals = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                double targetAmount = rs.getDouble("targetAmount");
                double currentAmount = rs.getDouble("currentAmount");
                int year = rs.getInt("year");
                goals.add(new Goal(targetAmount, year, username));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return goals;
    }

    public Goal getSavingsGoal(String username) {
        String sql = "SELECT targetAmount, currentAmount, year FROM Goals WHERE username = ? AND year = ?";
        int currentYear = LocalDate.now().getYear();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, currentYear);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double targetAmount = rs.getDouble("targetAmount");
                double currentAmount = rs.getDouble("currentAmount");
                Goal goal = new Goal(targetAmount, currentYear, username);
                goal.setCurrentAmount(currentAmount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // No savings goal found
    }


    // Update goal progress
    public void updateGoalProgress(String username, double targetAmount, double currentAmount) {
        String sql = "UPDATE Goals SET currentAmount = ? WHERE username = ? AND targetAmount = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, currentAmount);
            pstmt.setString(2, username);
            pstmt.setDouble(3, targetAmount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Log a financial transaction and associate it with a user
    public void logTransaction(User user, FinancialEntry transaction) {
        try {
            user.addTransaction(transaction);
            addTransaction(transaction, user.getUsername());
            System.out.println("Transaction logged successfully!");
        } catch (Exception e) {
            System.out.println("Error logging transaction: " + e.getMessage());
        }
    }

    public Goal getYearlyGoal(String username, int year) {
        String sql = "SELECT targetAmount, currentAmount, year FROM Goals WHERE username = ? AND year = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double targetAmount = rs.getDouble("targetAmount");
                double currentAmount = rs.getDouble("currentAmount");
                // Create the Goal object
                Goal goal = new Goal(targetAmount, year, username);
                goal.setCurrentAmount(currentAmount);

                return goal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No goal found
    }


    public void logInvestment(String username, double amount, LocalDate date, int duration, double rate, double projectedReturn) {
        String sql = "INSERT INTO Investments (username, amount, date, duration, rate, projectedReturn) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, date.toString());
            pstmt.setInt(4, duration);
            pstmt.setDouble(5, rate);
            pstmt.setDouble(6, projectedReturn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
