package com.perpustakaan.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // Untuk menghitung selisih hari

public class Transaction {
    private String transactionId; // ID unik untuk transaksi, contoh: T1001 [cite: 25]
    private String memberId;      // ID anggota yang meminjam, contoh: M001 [cite: 25]
    private String isbn;          // ISBN buku yang dipinjam, contoh: 978-602-73156-1-0 [cite: 25]
    private LocalDate borrowDate; // Tanggal buku dipinjam, contoh: 2025-01-15 [cite: 25]
    private LocalDate dueDate;    // Tanggal buku seharusnya dikembalikan (borrowDate + 7 hari) [cite: 19]
    private LocalDate actualReturnDate; // Tanggal buku benar-benar dikembalikan (bisa null jika belum kembali)
    private String status;        // Status peminjaman, contoh: "Borrowed", "Returned", "Overdue" [cite: 25]
    private double fine;          // Denda jika ada (akan dihitung saat pengembalian)

    /**
     * Constructor untuk membuat objek Transaction baru.
     * Tanggal jatuh tempo (dueDate) otomatis dihitung 7 hari dari tanggal peminjaman.
     * Status awal adalah "Borrowed".
     * Denda awal adalah 0.
     *
     * @param transactionId ID unik transaksi.
     * @param memberId      ID anggota yang meminjam.
     * @param isbn          ISBN buku yang dipinjam.
     * @param borrowDate    Tanggal buku dipinjam.
     */
    public Transaction(String transactionId, String memberId, String isbn, LocalDate borrowDate) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(7); // Default 7 hari untuk pengembalian [cite: 19]
        this.status = "Borrowed"; // Status awal saat transaksi dibuat [cite: 25]
        this.actualReturnDate = null; // Belum dikembalikan saat transaksi dibuat
        this.fine = 0.0; // Belum ada denda saat transaksi dibuat
    }

    // Getter dan Setter untuk semua atribut

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
        // Jika tanggal pinjam diubah, tanggal jatuh tempo juga harus dihitung ulang
        if (this.borrowDate != null) {
            this.dueDate = this.borrowDate.plusDays(7);
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    /**
     * Menghitung denda berdasarkan tanggal jatuh tempo dan tanggal pengembalian aktual.
     * Misalnya, denda Rp1000 per hari keterlambatan.
     * Metode ini bisa dipanggil saat admin memproses pengembalian buku.
     *
     * @return jumlah denda yang harus dibayar.
     */
    public double calculateFine() {
        if (actualReturnDate != null && actualReturnDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, actualReturnDate);
            // Asumsi denda adalah 1000 per hari. Anda bisa membuat ini lebih fleksibel.
            this.fine = overdueDays * 1000.0;
            return this.fine;
        }
        this.fine = 0.0;
        return 0.0;
    }

    /**
     * Representasi string dari objek Transaction.
     * Berguna untuk debugging atau logging.
     *
     * @return String yang merepresentasikan objek Transaction.
     */
    @Override
    public String toString() {
        return "Transaction{" +
               "transactionId='" + transactionId + '\'' +
               ", memberId='" + memberId + '\'' +
               ", isbn='" + isbn + '\'' +
               ", borrowDate=" + borrowDate +
               ", dueDate=" + dueDate +
               ", actualReturnDate=" + actualReturnDate +
               ", status='" + status + '\'' +
               ", fine=" + fine +
               '}';
    }
}