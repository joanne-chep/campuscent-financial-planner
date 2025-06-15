package com.campuscent;
import java.time.LocalDate;

public class Income extends FinancialEntry {
    private IncomeCategory category;

    public Income(double amount, LocalDate date, IncomeCategory category) {
        super(amount, date);
        this.category = category;
    }


    public IncomeCategory getCategory() {return category;}

    // Implementing the displayEntryDetails method
    @Override
    public void displayEntryDetails() {
        System.out.println("Income Details:");
        System.out.println("Amount: " + getAmount());
        System.out.println("Date: " + getDate());
        System.out.println("Source: " + category);
    }
}
