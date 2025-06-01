package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.LibraryManager;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.Transaction; // Pastikan model Transaction diimpor
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.time.LocalDate; // Untuk menampilkan tanggal
import java.time.format.DateTimeFormatter; // Untuk format tanggal

public class ReturnBookController implements NeedsLibraryManager {

    @FXML
    private TextField transactionIdField;
    @FXML
    private Button searchButton;
    @FXML
    private Separator detailsSeparator;
    @FXML
    private GridPane transactionDetailsPane;
    @FXML
    private Label bookTitleLabel;
    @FXML
    private Label memberNameLabel;
    @FXML
    private Label borrowDateLabel;
    @FXML
    private Label dueDateLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label fineLabel;
    @FXML
    private Button processReturnButton;

    private LibraryManager libraryManager;
    private Transaction currentTransaction; // Untuk menyimpan transaksi yang sedang diproses
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");


    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    @FXML
    private void initialize() {
        // Sembunyikan detail dan tombol proses pada awalnya
        hideTransactionDetails();
        System.out.println("ReturnBookController initialized.");
    }

    @FXML
    private void handleSearchTransactionAction(ActionEvent event) {
        String transactionId = transactionIdField.getText().trim();
        if (transactionId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Kosong", "ID Transaksi tidak boleh kosong.", "Silakan masukkan ID transaksi yang valid.");
            return;
        }

        currentTransaction = libraryManager.findTransactionById(transactionId);

        if (currentTransaction == null) {
            showAlert(Alert.AlertType.ERROR, "Transaksi Tidak Ditemukan", "Transaksi dengan ID '" + transactionId + "' tidak ditemukan.", "Pastikan ID transaksi sudah benar.");
            hideTransactionDetails();
        } else if ("Returned".equalsIgnoreCase(currentTransaction.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Info Transaksi", "Buku untuk transaksi ID '" + transactionId + "' sudah dikembalikan sebelumnya.", 
                      "Tanggal Kembali: " + (currentTransaction.getActualReturnDate() != null ? currentTransaction.getActualReturnDate().format(dateFormatter) : "-"));
            displayTransactionDetails(currentTransaction, true); // Tampilkan detail tapi tombol proses tetap nonaktif
        } else {
            // Transaksi ditemukan dan belum dikembalikan (Borrowed atau Overdue)
            displayTransactionDetails(currentTransaction, false);
        }
    }

    @FXML
    private void handleProcessReturnAction(ActionEvent event) {
        if (currentTransaction == null) {
            showAlert(Alert.AlertType.ERROR, "Proses Gagal", "Tidak ada transaksi yang dipilih untuk diproses.", "Silakan cari transaksi terlebih dahulu.");
            return;
        }

        boolean success = libraryManager.returnBook(currentTransaction.getTransactionId());
        if (success) {
            // Dapatkan nilai denda yang sudah dihitung oleh returnBook()
            double fineAmount = currentTransaction.getFine(); // Asumsi fine sudah di-update di currentTransaction oleh LibraryManager
            
            showAlert(Alert.AlertType.INFORMATION, "Pengembalian Berhasil", 
                      "Buku '" + bookTitleLabel.getText() + "' berhasil dikembalikan." +
                      (fineAmount > 0 ? "\nTotal Denda: Rp" + String.format("%,.0f", fineAmount) : "\nTidak ada denda."), null);
            
            // Reset tampilan
            transactionIdField.clear();
            hideTransactionDetails();
            currentTransaction = null; // Bersihkan transaksi saat ini
        } else {
            showAlert(Alert.AlertType.ERROR, "Pengembalian Gagal", "Gagal memproses pengembalian buku.", "Silakan coba lagi atau hubungi administrator.");
        }
    }

    private void displayTransactionDetails(Transaction transaction, boolean isAlreadyReturned) {
        Book book = libraryManager.findBookByIsbn(transaction.getIsbn());
        Member member = libraryManager.findMemberById(transaction.getMemberId());

        bookTitleLabel.setText(book != null ? book.getTitle() : "Buku Tidak Ditemukan");
        memberNameLabel.setText(member != null ? member.getFullName() : "Anggota Tidak Ditemukan");
        borrowDateLabel.setText(transaction.getBorrowDate().format(dateFormatter));
        dueDateLabel.setText(transaction.getDueDate().format(dateFormatter));
        statusLabel.setText(transaction.getStatus());

        // Hitung denda (ini akan dihitung ulang di LibraryManager saat returnBook dipanggil,
        // tapi kita bisa tampilkan estimasi di sini)
        // Pastikan calculateFine() di Transaction.java bisa dipanggil tanpa mengubah status permanen
        // atau cukup tampilkan denda yang sudah ada jika statusnya Returned.
        double fineAmount = 0;
        if(isAlreadyReturned) {
            fineAmount = transaction.getFine(); // Ambil denda yang sudah tercatat
        } else {
             // Untuk transaksi yang belum dikembalikan, simulasikan perhitungan denda jika dikembalikan hari ini
            Transaction tempTransactionForFine = new Transaction(
                transaction.getTransactionId(), 
                transaction.getMemberId(), 
                transaction.getIsbn(), 
                transaction.getBorrowDate()
            );
            tempTransactionForFine.setDueDate(transaction.getDueDate()); // Pastikan due date sama
            tempTransactionForFine.setActualReturnDate(LocalDate.now()); // Asumsikan kembali hari ini untuk preview denda
            fineAmount = tempTransactionForFine.calculateFine();
        }
        fineLabel.setText(String.format("%,.0f", fineAmount));


        detailsSeparator.setVisible(true);
        transactionDetailsPane.setVisible(true);
        processReturnButton.setVisible(true);
        processReturnButton.setDisable(isAlreadyReturned || book == null || member == null); // Nonaktifkan jika sudah kembali atau data tidak lengkap
    }

    private void hideTransactionDetails() {
        detailsSeparator.setVisible(false);
        transactionDetailsPane.setVisible(false);
        bookTitleLabel.setText("-");
        memberNameLabel.setText("-");
        borrowDateLabel.setText("-");
        dueDateLabel.setText("-");
        statusLabel.setText("-");
        fineLabel.setText("0.0");
        processReturnButton.setVisible(false);
        processReturnButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}