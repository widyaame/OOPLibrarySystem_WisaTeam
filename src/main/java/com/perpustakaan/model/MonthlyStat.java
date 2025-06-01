package com.perpustakaan.model;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter; // Impor untuk formatter

public class MonthlyStat {
    private YearMonth monthYear;
    private int totalBorrows;
    private int totalReturns;
    private double totalFines;

    // Formatter untuk tampilan bulan dan tahun yang lebih baik
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");


    public MonthlyStat(YearMonth monthYear) {
        this.monthYear = monthYear;
        this.totalBorrows = 0;
        this.totalReturns = 0;
        this.totalFines = 0.0;
    }

    // Getter
    public YearMonth getMonthYear() {
        return monthYear;
    }

    // Metode getter baru untuk menampilkan string yang sudah diformat
    public String getMonthYearFormatted() {
        if (this.monthYear == null) {
            return "N/A";
        }
        return this.monthYear.format(MONTH_YEAR_FORMATTER);
    }

    public int getTotalBorrows() {
        return totalBorrows;
    }

    public int getTotalReturns() {
        return totalReturns;
    }

    public double getTotalFines() {
        return totalFines;
    }

    // Metode untuk menambah data (mutator)
    public void incrementBorrows() {
        this.totalBorrows++;
    }

    public void incrementReturns() {
        this.totalReturns++;
    }

    public void addFines(double fineAmount) {
        this.totalFines += fineAmount;
    }
}