package com.campuscent;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.time.format.DateTimeParseException;

public class App {
    private static DatabaseHelper dbHelper;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // Initialize the database
        dbHelper = new DatabaseHelper("campuscent.db");
        dbHelper.initializeDatabase();

        // Start the application
        User user = welcomeAndAuthenticate();

        if (user != null) {
            System.out.println("Login successful!");

            // Load user data
            loadUserData(user);

            // Main menu
            mainMenu(user);
        } else {
            System.out.println("Authentication failed. Exiting program.");
        }

        scanner.close();
    }

    private static User welcomeAndAuthenticate() {
        while (true) {
            System.out.println("\nWelcome to CampusCent!");
            System.out.println("Are you already registered?");
            System.out.println("1. Yes, I want to log in.");
            System.out.println("2. No, I want to register.");
            System.out.println("3. Exit Program.");

            int choice = getValidChoice(1, 2, 3);

            switch (choice) {
                case 1 -> {
                    return loginUser(); // Redirect to login
                }
                case 2 -> {
                    return registerUser(); // Redirect to registration
                }
                case 3 -> {
                    System.out.println("Exiting program. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid input. Please try again.");
            }
        }
    }


    private static User loginUser() {
        while (true) {
            System.out.println("\n[LOGIN] Please enter your credentials or type 'back' to return to the previous menu.");

            String username = getValidString("Username: ", input -> !input.trim().isEmpty());
            if (username.equalsIgnoreCase("back")) return null; // Go back to the welcome menu

            String password = getValidString("Password: ", input -> !input.trim().isEmpty());
            if (password.equalsIgnoreCase("back")) return null;

            // Validate login credentials
            LoginController loginController = new LoginController(dbHelper);
            String sessionId = loginController.login(username, password);

            if (sessionId != null && SessionManager.isSessionValid(sessionId)) {
                System.out.println("\nLogin successful! Welcome, " + username + "!");
                return dbHelper.getUserByUsername(username);
            } else {
                System.out.println("\n[ERROR] Login failed. Username or password is incorrect.");
                System.out.println("1. Try Again");
                System.out.println("2. Register for a new account.");
                System.out.println("3. Back to Main Menu");

                int choice = getValidChoice(1, 2, 3);

                switch (choice) {
                    case 1 -> System.out.println("Please try logging in again.");
                    case 2 -> {
                        return registerUser(); // Redirect to registration
                    }
                    case 3 -> {
                        return null; // Back to the welcome menu
                    }
                }
            }
        }
    }


    private static User registerUser() {
        while (true) {
            System.out.println("\n[REGISTER] Please enter your desired credentials or type 'back' to return to the previous menu.");

            String username = getValidString("Enter your username (3-20 characters, alphanumeric + underscores): ", App::isValidUsername);
            if (username.equalsIgnoreCase("back")) return null; // Go back to the welcome menu

            String password = getValidString("Enter your password (at least 8 characters with letters and numbers): ", App::isValidPassword);
            if (password.equalsIgnoreCase("back")) return null;

            try {
                // Attempt to add the user to the database
                User user = new User(username, password);
                dbHelper.addUser(user.getUsername(), user.getHashedPassword());

                System.out.println("\nRegistration successful! You can now log in.");
                return loginUser(); // Redirect to login after successful registration

            } catch (Exception e) {
                if (e.getMessage().contains("UNIQUE constraint failed: Users.username")) {
                    // Handle duplicate username error
                    System.out.println("\n[ERROR] This username is already taken.");
                    System.out.println("1. Log in with this username.");
                    System.out.println("2. Try registering with a different username.");
                    System.out.println("3. Back to Main Menu");

                    int choice = getValidChoice(1, 2, 3);

                    switch (choice) {
                        case 1 -> {
                            return loginUser(); // Redirect to login
                        }
                        case 2 -> {
                            System.out.println("Please try registering with a different username.");
                        }
                        case 3 -> {
                            return null; // Back to the welcome menu
                        }
                    }
                } else {
                    // Handle other unexpected errors
                    System.out.println("\n[ERROR] An unexpected error occurred during registration: " + e.getMessage());
                    return null; // Exit registration in case of a critical error
                }
            }
        }
    }



    private static void loadUserData(User user) {
        System.out.println("Loading your data...");
        user.loadTransactions(dbHelper);

        TransactionSummary summary = new TransactionSummary(dbHelper);
        System.out.println("User Summary:");
        System.out.println(" - Total Income: GHC" + String.format("%.2f", summary.getTotalIncome(user)));
        System.out.println(" - Total Expenses: GHC" + String.format("%.2f", summary.getTotalExpenses(user)));

        System.out.println("\nIncome Breakdown by Category:");
        summary.displayIncomeByCategory(user.getUsername());

        System.out.println("\nExpense Breakdown by Category:");
        summary.displayExpensesByCategory(user.getUsername());

        List<Goal> userGoals = dbHelper.getGoals(user.getUsername());
        if (!userGoals.isEmpty()) {
            System.out.println("\nActive Savings Goals:");
            for (Goal goal : userGoals) {
                System.out.println(" - Target: GHC" + goal.getTargetAmount() +
                        " | Current: GHC" + goal.getCurrentAmount() +
                        " | Remaining: GHC" + goal.getRemaining() +
                        " | Year: " + goal.getYear());
            }
        } else {
            System.out.println("\nNo active savings goals.");
        }
        System.out.println("Data loaded successfully!");
    }

    private static void mainMenu(User user) {
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Log Income or Expense");
            System.out.println("2. View Transactions Summary");
            System.out.println("3. Set Financial Goals");
            System.out.println("4. Exit");

            int choice = getValidChoice(1,2,3,4);

            switch (choice) {
                case 1 -> logTransactions(user);
                case 2 -> viewTransactionSummary(user);
                case 3 -> setFinancialGoals(user);
                case 4 -> {
                    System.out.println("Exiting program. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void logTransactions(User user) {
        System.out.println("Would you like to log an income or an expense?");
        System.out.println("1. Income");
        System.out.println("2. Expense");
        System.out.println("3. Back to Main Menu");
        int choice = getValidChoice(1, 2, 3);

        if (choice == 1) {
            logIncome(user);
        } else if (choice == 2){
            logExpense(user);
        } else if (choice == 3) {
            System.out.println("Returning to the main menu...");
            return;
        }
    }

    private static void logIncome(User user) {
        System.out.println("\nEnter your monthly income amount, or type 0 to go back to the main menu:");

        double incomeAmount = getValidDouble("Enter your monthly income amount: ");

        // Check if the user wants to go back
        if (incomeAmount == 0) {
            System.out.println("Returning to the main menu...");
            return;
        }

        // Prompt the user to select an income category
        IncomeCategory category = chooseIncomeCategory();

        // Create an Income object
        Income income = new Income(incomeAmount, LocalDate.now(), category);

        // Log the income transaction in the database
        dbHelper.logTransaction(user, income);

        // Allocation amounts
        double newSpendingAllocation = incomeAmount * 0.70;   // 70% for spending
        double savingAndInvestmentAllocation = incomeAmount * 0.30; // 30% for savings/investment

        // Update DynamicLimit for spending
        DynamicLimit dynamicLimit = user.getDynamicLimit();

        // If there was a previous spending allocation, add to it
        double updatedSpendingAllocation = dynamicLimit.getTotalLimit() + newSpendingAllocation;

        // Recalculate 1 limit based on the updated spending allocation
        dynamicLimit.updateLimit(updatedSpendingAllocation);

        System.out.println("\nYour income has been allocated as follows:");
        System.out.println(" - New Spending: GHC" + String.format("%.2f", newSpendingAllocation) + " (70%)");
        System.out.println(" - Total Spending Allocation (Updated): GHC" + String.format("%.2f", dynamicLimit.getTotalLimit()));
        System.out.println(" - Savings/Investment: GHC" + String.format("%.2f", savingAndInvestmentAllocation) + " (30%)");

        // Ask the user how to allocate the 30% for savings/investment
        System.out.println("\nHow would you like to allocate the savings/investment portion?");
        System.out.println("1. Allocate all to savings.");
        System.out.println("2. Split into 70% savings and 30% investment.");
        System.out.println("3. Back to Main Menu");
        int allocationChoice = getValidChoice(1, 2, 3);

        if (allocationChoice == 1) {
            // Allocate all to savings
            allocateToSavings(user, savingAndInvestmentAllocation);
        } else if (allocationChoice == 2) {
            // Split into savings and investment
            InvestmentManager.splitAndAllocateSavings(savingAndInvestmentAllocation, user, dbHelper);
        } else if (allocationChoice == 3) {
            System.out.println("Returning to the main menu...");
            return;
        }
    }




    private static void logExpense(User user) {
        System.out.println("\nEnter your expense amount:");

        double expenseAmount = getValidDouble("Enter your expense amount: ");




        Category expenseCategory = chooseExpenseCategory();

        Expense expense = new Expense(expenseAmount, LocalDate.now(), expenseCategory);
        dbHelper.logTransaction(user, expense);

        // Update DynamicLimit with the new expense
        DynamicLimit dynamicLimit = user.getDynamicLimit();
        dynamicLimit.spend(expenseAmount); // Process the expense

        System.out.println("Expense logged successfully!");

        // Debug the updated limits
        dynamicLimit.debugLimit();

        // Check if the user has exceeded their monthly spending allocation
        if (dynamicLimit.hasExceededLimit()) {
            System.out.println("\n[ALERT] Your monthly spending allocation has been exhausted.");
            double overspentAmount = dynamicLimit.getSpent() - dynamicLimit.getTotalLimit();
            System.out.println("You have overspent by GHC" + String.format("%.2f", overspentAmount) + ".");

            System.out.println("Would you like to withdraw from your savings to cover this amount? (yes/no)");
            String response = getValidString("Enter 'yes' or 'no': ", input -> input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("no"));

            if (response.equalsIgnoreCase("yes")) {
                // Fetch the active savings goal
                Goal savingsGoal = dbHelper.getSavingsGoal(user.getUsername());

                if (savingsGoal == null) {
                    // No active savings goal found
                    System.out.println("[NOTICE] You do not have an active savings goal. Please set a goal before using savings for expenses.");
                    return;
                }

                double remainingSavings = savingsGoal.getRemaining();

                if (remainingSavings >= overspentAmount) {
                    // Withdraw from savings
                    savingsGoal.updateProgress(dbHelper, -overspentAmount);
                    System.out.println("[SUCCESS] GHC" + String.format("%.2f", overspentAmount) + " has been withdrawn from your savings.");
                    System.out.println("Remaining Savings: GHC" + String.format("%.2f", savingsGoal.getRemaining()));

                    // Reset the overspent amount in the dynamic limit
                    dynamicLimit.adjustForSavings(overspentAmount);
                } else {
                    // Insufficient savings
                    System.out.println("[NOTICE] Your savings are insufficient to cover the overspent amount.");
                    System.out.println(" - Savings Available: GHC" + String.format("%.2f", remainingSavings));
                    System.out.println(" - Overspent Amount: GHC" + String.format("%.2f", overspentAmount));
                    System.out.println("Please plan your expenses carefully to avoid overspending.");
                }
            } else {
                System.out.println("Please plan your expenses carefully to avoid overspending.");
            }
        }

    }



    private static Category chooseExpenseCategory() {
        System.out.println("Select an expense category:");
        System.out.println("1. FOOD");
        System.out.println("2. TRANSPORTATION");
        System.out.println("3. HOUSING");
        System.out.println("4. UTILITIES");
        System.out.println("5. ENTERTAINMENT");
        System.out.println("6. OTHER");
        System.out.println("7. Back to Main Menu");

        int choice = getValidChoice(1, 2,3,4,5,6,7);

        return switch (choice) {
            case 1 -> Category.FOOD;
            case 2 -> Category.TRANSPORTATION;
            case 3 -> Category.HOUSING;
            case 4 -> Category.UTILITIES;
            case 5 -> Category.ENTERTAINMENT;
            case 6 -> Category.OTHER;
            case 7 -> null; // Return null to indicate "Back to Main Menu"
            default -> throw new IllegalStateException("Unexpected value: " + choice);
        };
    }

    private static void viewTransactionSummary(User user) {
        TransactionSummary summary = new TransactionSummary(dbHelper);
        System.out.println("Total Income: GHC" + String.format("%.2f", summary.getTotalIncome(user)));
        System.out.println("Total Expenses: GHC" + String.format("%.2f", summary.getTotalExpenses(user)));

        System.out.println("\nIncome Breakdown by Category:");
        summary.displayIncomeByCategory(user.getUsername());

        System.out.println("\nExpense Breakdown by Category:");
        summary.displayExpensesByCategory(user.getUsername());
    }

    private static void setFinancialGoals(User user) {
        int currentYear = LocalDate.now().getYear();

        // Check if the user already has a goal for the current year
        if (dbHelper.hasYearlyGoal(user.getUsername(), currentYear)) {
            System.out.println("You already have a savings goal set for this year.");
            return;
        }

        double savingsGoalAmount = getValidDouble("Enter your yearly savings goal amount: ");

        Goal savingsGoal = new Goal(savingsGoalAmount, currentYear, user.getUsername());
        dbHelper.addGoal(user.getUsername(), savingsGoal.getTargetAmount(), 0, currentYear);
        System.out.println("Savings goal set successfully.");
    }

    private static void allocateToSavings(User user, double savingsAllocation) {
        System.out.println("\nAllocating $" + String.format("%.2f", savingsAllocation) + " to your savings.");

        // Fetch the user's single savings goal for the current year
        int currentYear = LocalDate.now().getYear();
        Goal savingsGoal = dbHelper.getYearlyGoal(user.getUsername(), currentYear);

        if (savingsGoal == null) {
            System.out.println("[NOTICE] You don't have an active savings goal for this year.");
            System.out.println("Consider setting a financial goal from the main menu!");
            return;
        }

        // Update the progress for the savings goal
        savingsGoal.updateProgress(dbHelper, savingsAllocation);

        System.out.println("Successfully added $" + String.format("%.2f", savingsAllocation) + " to your savings goal.");
        System.out.println("Current Savings: $" + String.format("%.2f", savingsGoal.getCurrentAmount()) +
                " | Target: $" + String.format("%.2f", savingsGoal.getTargetAmount()));

        // Check if the goal is reached
        if (savingsGoal.isGoalReached()) {
            savingsGoal.printGoalReachedMessage();
        }
    }



    @FunctionalInterface
    public interface ValidationFunction {
        boolean validate(String input);
    }

    private static int getValidChoice(int... validOptions) {
        while (true) {
            System.out.print("Enter your choice: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                for (int valid : validOptions) {
                    if (choice == valid) return choice;
                }
                System.out.println("Invalid choice. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static double getValidDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) return value;
                System.out.println("Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static String getValidString(String prompt, ValidationFunction validator) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("back")) return "back";
            if (validator.validate(input)) return input;
            System.out.println("Invalid input. Try again.");
        }
    }

    private static boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,20}$");

    }

    private static boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$");
    }

    private static void adjustSavingsForOverspend(User user, double overspentAmount) {
        List<Goal> userGoals = user.getGoals();

        if (userGoals.isEmpty()) {
            System.out.println("[NOTICE] You don't have any savings goals set. You cannot withdraw from savings.");
            return;
        }

        Goal savingsGoal = userGoals.get(0); // Assume a single savings goal for simplicity

        if (savingsGoal.getRemaining() < overspentAmount) {
            System.out.println("[NOTICE] Your savings are insufficient to cover the overspent amount.");
            System.out.println(" - Savings Available: GHC" + String.format("%.2f", savingsGoal.getRemaining()));
            System.out.println(" - Overspent Amount: GHC" + String.format("%.2f", overspentAmount));
        } else {
            savingsGoal.updateProgress(dbHelper, -overspentAmount); // Withdraw from savings
            System.out.println("You have successfully withdrawn GHC" + String.format("%.2f", overspentAmount) + " from your savings.");
            System.out.println("Remaining Savings: GHC" + String.format("%.2f", savingsGoal.getRemaining()));
        }
    }

    private static IncomeCategory chooseIncomeCategory() {
        System.out.println("Select an income category:");
        System.out.println("1. SALARY");
        System.out.println("2. FREELANCE");
        System.out.println("3. RENTAL");
        System.out.println("4. INVESTMENT");
        System.out.println("5. OTHER");
        System.out.println("6. Back to Main Menu");

        int choice = getValidChoice(1, 2,3,4,5,6);

        return switch (choice) {
            case 1 -> IncomeCategory.SALARY;
            case 2 -> IncomeCategory.FREELANCE;
            case 3 -> IncomeCategory.RENTAL;
            case 4 -> IncomeCategory.INVESTMENT;
            case 5 -> IncomeCategory.OTHER;
            case 6 -> null;
            default -> throw new IllegalStateException("Unexpected value: " + choice);
        };
    }


}
