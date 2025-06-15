package com.campuscent;

import java.time.LocalDate;
import java.util.Scanner;

public class InvestmentManager {

    // Get rate for treasury bill period
    public static double getRateForPeriod(int days) {
        return switch (days) {
            case 91 -> 26.8293;  // 91-day rate
            case 182 -> 27.7876; // 182-day rate
            case 364 -> 29.2178; // 364-day rate
            default -> throw new IllegalArgumentException("Invalid period. Use 91, 182, or 364 days.");
        };
    }



    // Prompt user and handle the split between savings and investment
    public static void splitAndAllocateSavings(double incomeAmount, User user, DatabaseHelper dbHelper) {
        double savingsAllocation = incomeAmount * 0.70;
        double investmentAllocation = incomeAmount * 0.30;

        System.out.println("Allocation:");
        System.out.println(" - Savings: GHC" + String.format("%.2f", savingsAllocation) + " (70%)");
        System.out.println(" - Investment: GHC" + String.format("%.2f", investmentAllocation) + " (30%)");

        // Prompt for investment details
        System.out.println("\nYou chose to allocate 30% for investments.");
        System.out.println("Choose the treasury bill period for your investment:");
        System.out.println("1. 91 days");
        System.out.println("2. 182 days");
        System.out.println("3. 364 days");

        int choice = getValidChoice(new Scanner(System.in), 1, 3);
        int investmentDays = switch (choice) {
            case 1 -> 91;
            case 2 -> 182;
            case 3 -> 364;
            default -> throw new IllegalStateException("Unexpected value: " + choice);
        };

        double rate = getRateForPeriod(investmentDays);
        double projectedReturn = calculateInvestmentReturn(investmentAllocation, rate, investmentDays);

        System.out.println("\nInvestment Breakdown:");
        System.out.println(" - Principal: GHC" + String.format("%.2f", investmentAllocation));
        System.out.println(" - Treasury Bill Duration: " + investmentDays + " days");
        System.out.println(" - Rate: " + String.format("%.2f", rate) + "%");
        System.out.println(" - Projected Return: GHC" + String.format("%.2f", projectedReturn));

        // Log the investment into the database
        dbHelper.logInvestment(
                user.getUsername(),
                investmentAllocation,
                LocalDate.now(),
                investmentDays,
                rate,
                projectedReturn
        );

        System.out.println("\nYour savings and investment have been successfully allocated!");
    }

    // Calculate returns for the specific treasury bill duration
    public static double calculateInvestmentReturn(double principal, double rate, int days) {
        double annualRate = rate / 100.0; // Convert percentage to decimal
        double timePeriod = days / 365.0; // Convert days to fraction of a year
        return principal * (1 + (annualRate * timePeriod)); // Simple interest calculation
    }

    // Utility method for validating user input for choices
    private static int getValidChoice(Scanner scanner, int min, int max) {
        while (true) {
            System.out.print("Enter a number between " + min + " and " + max + ": ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }


    // Example savings goal
}
