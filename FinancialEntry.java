package com.campuscent;
import java.time.LocalDate;

public abstract class FinancialEntry {
    private double amount;
    private LocalDate date;

    public FinancialEntry(double amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    
    // Abstract method for displaying details
    public abstract void displayEntryDetails();
}