package com.campuscent;

import java.time.YearMonth;
import java.time.LocalDate;

public class DynamicLimit {
    private double totalLimit; // Total spending allocation for the month
    private int totalDays; // Total number of days in the current month
    private int currentDay; // Current day in the month (1-indexed)
    private double dailyLimit; // The dynamic daily limit
    private double spent; // Total amount spent so far
    private double carryover; // Carryover amount from previous days
    private LocalDate currentDate;
    private double dailySpent;

    public DynamicLimit() {
        this.totalLimit = 0;
        this.totalDays = 0;
        this.currentDay = 1; // Start at day 1
        this.dailyLimit = 0;
        this.spent = 0;
        this.carryover = 0;
        this.currentDate = null;
        this.dailySpent = 0;
    }

    // Initialize the dynamic limit with monthly spending allocation
    public void initializeLimit(double monthlySpendingAllocation) {
        YearMonth currentMonth = YearMonth.now(); // Get the current year and month
        this.totalLimit = monthlySpendingAllocation; // Set the total spending allocation
        this.totalDays = currentMonth.lengthOfMonth(); // Get total days in the current month
        this.dailyLimit = monthlySpendingAllocation / totalDays; // Calculate the initial daily limit
        this.spent = 0; // Reset spent amount
        this.carryover = 0; // Reset carryover amount
        this.currentDay = LocalDate.now().getDayOfMonth(); // Start at day 1
        this.currentDate = LocalDate.now();

    }

    // Handle spending for a given day
    public void spend(double amount) {
        // Check if the real-world date has advanced
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            moveToNextDay(today);
        }
        // Update total spent for the month
        dailySpent+= amount;
        spent += amount;

        // Adjust the daily limit based on carryover
        double effectiveDailyLimit = dailyLimit + carryover;

        if (dailySpent > effectiveDailyLimit) {
            System.out.println("[NOTICE] You have overspent by GHC" + String.format("%.2f", dailySpent - effectiveDailyLimit) + " today!");
        } else {
            System.out.println("[NOTICE] You have underspent today. Carryover will be applied at the end of the day.");
        }

        // Check if the total spending allocation has been exceeded
        if (spent > totalLimit) {
            double overspentAmount = spent - totalLimit;
            System.out.println("[ALERT] You have exceeded your monthly spending limit by GHC" + String.format("%.2f", overspentAmount) + "!");
        }
    }

    public void adjustForSavings(double amount) {
        // Reduce the spent amount by the withdrawn savings
        spent -= amount;

        // Ensure spent does not go negative (edge case safety)
        if (spent < 0) {
            spent = 0;
        }
    }


    // Move to the next day
    private void moveToNextDay(LocalDate today) {
        if (!today.equals(currentDate)) {
            endDay(); // End the current day
            currentDate = today; // Update the date
        }
    }

    public void endDay() {
        // Calculate carryover
        double effectiveDailyLimit = dailyLimit + carryover;
        if (dailySpent > effectiveDailyLimit) {
            carryover = -(dailySpent - effectiveDailyLimit); // Negative carryover reduces tomorrow's limit
            System.out.println("[NOTICE] You overspent today by GHC" + String.format("%.2f", -carryover) + ". Tomorrow's limit will be reduced.");
        } else {
            carryover = effectiveDailyLimit - dailySpent; // Positive carryover increases tomorrow's limit
            System.out.println("[NOTICE] You underspent today by GHC" + String.format("%.2f", carryover) + ". Tomorrow's limit will increase.");
        }

        // Reset daily spent for the new day
        dailySpent = 0;

        // Move to the next day
        if (currentDay < totalDays) {
            currentDay++;
        } else {
            System.out.println("[NOTICE] You have reached the end of the month.");
        }
    }



    // Get the current daily limit (adjusted with carryover)
    public double getDailyLimit() {
        return dailyLimit + carryover- dailySpent;
    }

    // Debugging Output
    public void debugLimit() {
        System.out.println("Dynamic Limit Debug:");
        System.out.println(" - Total Limit (Monthly): GHC" + totalLimit);
        System.out.println(" - Total Spent (Monthly): GHC" + spent);
        System.out.println(" - Daily Spent (Today): GHC" + dailySpent);
        System.out.println(" - Current Day: " + currentDay);
        System.out.println(" - Daily Limit (Base): GHC" + dailyLimit);
        System.out.println(" - Carryover: GHC" + carryover);
        System.out.println(" - Effective Daily Limit (Today): GHC" + getDailyLimit());
        System.out.println(" - Remaining Monthly Limit: GHC" + (totalLimit - spent));
    }


    // Check if the user has exceeded the total spending limit
    public boolean hasExceededLimit() {
        return spent > totalLimit;
    }

    // Getters for testing
    public double getTotalLimit() {
        return totalLimit;
    }

    public void updateLimit(double updatedSpendingAllocation) {
        this.totalLimit = updatedSpendingAllocation; // Update the total spending allocation

        // Calculate the remaining days in the current month
        LocalDate today = LocalDate.now();
        int remainingDays = totalDays - today.getDayOfMonth() + 1;

        // Recalculate the daily limit based on the remaining days
        this.dailyLimit = totalLimit / remainingDays;

        System.out.println("[UPDATE] Dynamic limit recalculated.");
        System.out.println(" - Updated Total Spending Allocation: GHC" + totalLimit);
        System.out.println(" - Remaining Days in Month: " + remainingDays);
        System.out.println(" - New Daily Limit: GHC" + dailyLimit);
    }


    public int getTotalDays() {
        return totalDays;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public double getSpent() {
        return spent;
    }

    public double getCarryover() {
        return carryover;
    }
}
