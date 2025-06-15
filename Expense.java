package com.campuscent;
import java.time.LocalDate;
import java.util.Date;

public class Expense extends FinancialEntry {
    private Category category;

    public Expense(double amount, LocalDate date, Category category) {
        super(amount, date);
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public void displayEntryDetails() {
        System.out.println("Expense Details:");
        System.out.println("Amount: " + getAmount());
        System.out.println("Date: " + getDate());
        System.out.println("Category: " + category);
    }
}
