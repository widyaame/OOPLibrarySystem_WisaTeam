package com.perpustakaan.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; // Untuk assertions seperti assertEquals

import java.time.LocalDate;

class TransactionTest {

    @Test
    void testCalculateFine_OnTimeReturn() {
        // Setup: Buku dipinjam dan dikembalikan tepat waktu atau lebih awal
        LocalDate borrowDate = LocalDate.of(2025, 5, 1);
        Transaction transaction = new Transaction("T001", "M001", "ISBN001", borrowDate);
        // DueDate akan otomatis menjadi 2025-05-08 (borrowDate + 7 hari)
        
        transaction.setActualReturnDate(LocalDate.of(2025, 5, 7)); // Dikembalikan sebelum jatuh tempo
        
        double expectedFine = 0.0;
        double actualFine = transaction.calculateFine(); // Panggil metode yang diuji
        
        assertEquals(expectedFine, actualFine, "Denda seharusnya 0.0 untuk pengembalian tepat waktu.");
        assertEquals(expectedFine, transaction.getFine(), "Atribut denda di transaksi seharusnya 0.0.");
    }

    @Test
    void testCalculateFine_LateReturn() {
        // Setup: Buku dipinjam dan dikembalikan terlambat
        LocalDate borrowDate = LocalDate.of(2025, 5, 1);
        Transaction transaction = new Transaction("T002", "M002", "ISBN002", borrowDate);
        // DueDate akan otomatis menjadi 2025-05-08
        
        transaction.setActualReturnDate(LocalDate.of(2025, 5, 10)); // Dikembalikan 2 hari terlambat
                                                                 // Asumsi denda 1000/hari
        
        // Asumsi denda 1000 per hari (sesuai implementasi di Transaction.calculateFine())
        double expectedFine = 2 * 1000.0; // 2 hari terlambat * 1000
        double actualFine = transaction.calculateFine(); // Panggil metode yang diuji

        assertEquals(expectedFine, actualFine, "Denda seharusnya 2000.0 untuk keterlambatan 2 hari.");
        assertEquals(expectedFine, transaction.getFine(), "Atribut denda di transaksi seharusnya 2000.0.");
    }

    @Test
    void testCalculateFine_ReturnedOnDueDate() {
        // Setup: Buku dikembalikan tepat pada tanggal jatuh tempo
        LocalDate borrowDate = LocalDate.of(2025, 5, 1);
        Transaction transaction = new Transaction("T003", "M003", "ISBN003", borrowDate);
        // DueDate akan otomatis menjadi 2025-05-08
        
        transaction.setActualReturnDate(LocalDate.of(2025, 5, 8)); // Dikembalikan pas jatuh tempo
        
        double expectedFine = 0.0;
        double actualFine = transaction.calculateFine();

        assertEquals(expectedFine, actualFine, "Denda seharusnya 0.0 jika dikembalikan tepat pada jatuh tempo.");
        assertEquals(expectedFine, transaction.getFine(), "Atribut denda di transaksi seharusnya 0.0.");
    }

    @Test
    void testCalculateFine_NotYetReturned() {
        // Setup: Buku belum dikembalikan
        LocalDate borrowDate = LocalDate.of(2025, 5, 1);
        Transaction transaction = new Transaction("T004", "M004", "ISBN004", borrowDate);
        // DueDate akan otomatis menjadi 2025-05-08
        // transaction.setActualReturnDate() tidak dipanggil
        
        double expectedFine = 0.0; // Denda belum dihitung jika belum ada tanggal kembali aktual
        double actualFine = transaction.calculateFine();

        assertEquals(expectedFine, actualFine, "Denda seharusnya 0.0 jika buku belum dikembalikan.");
        assertEquals(expectedFine, transaction.getFine(), "Atribut denda di transaksi seharusnya 0.0.");
    }
}