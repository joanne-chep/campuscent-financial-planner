package com.campuscent;

import java.time.LocalDate;

public class Goal implements Progressible {
    private double targetAmount;
    private double currentAmount;
    private String username;
    private int year;// Associate the goal with a user

    public Goal(double targetAmount, int year, String username) {
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.username = username;
        this.year = year;
    }

    public void updateProgress(DatabaseHelper dbHelper, double amount) {
        this.currentAmount += amount; // Adjust the progress by the specified amount

        // Ensure currentAmount does not exceed the target or fall below zero
        if (this.currentAmount < 0) {
            this.currentAmount = 0;
        } else if (this.currentAmount > targetAmount) {
            this.currentAmount = targetAmount;
        }

        // Update the goal in the database
        dbHelper.updateGoalProgress(username, targetAmount, currentAmount);

        // Check if the savings goal has been reached
        if (this.currentAmount >= targetAmount) {
            System.out.println("[CONGRATULATIONS] You have reached your savings goal of GHC" + String.format("%.2f", targetAmount) + "!");
        }
    }


    @Override
    public double getPercentage() {
        return (currentAmount / targetAmount) * 100;
    }

    @Override
    public double getRemaining() {
        return targetAmount - currentAmount;
    }

    public boolean isGoalReached() {
        return currentAmount >= targetAmount; // Check if the goal has been reached
    }

    public void printGoalReachedMessage() {
        if (isGoalReached()) {
            System.out.println("\n[CONGRATULATIONS] You have reached your savings goal of $" + String.format("%.2f", targetAmount) + "!");
        }
    }

    public int getYear() {return year;}

    public double getTargetAmount() {return targetAmount;}

    public double getCurrentAmount() {return currentAmount;}

    public void setCurrentAmount(double currentAmount) {this.currentAmount = currentAmount;}

    // Save the goal to the database
    public void saveGoal(DatabaseHelper dbHelper) {
        dbHelper.addGoal(username, targetAmount, currentAmount, year);
    }

    // Reduce the current amount saved
    public void reduceGoal(double amount) {
        if (amount > currentAmount) {
            System.out.println("Cannot reduce by more than the current savings.");
        } else {
            currentAmount -= amount;
        }
    }
}
