package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.LibraryManager;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.Transaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell; // <-- Tambahkan impor ini
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal; // <-- Tambahkan impor ini

public class BorrowedBooksReportController implements NeedsLibraryManager {

    @FXML
    private TableView<Transaction> reportTableView;
    @FXML
    private TableColumn<Transaction, String> transactionIdColumn;
    @FXML
    private TableColumn<Transaction, String> bookTitleColumn;
    @FXML
    private TableColumn<Transaction, String> memberNameColumn;
    @FXML
    private TableColumn<Transaction, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<Transaction, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Transaction, String> statusColumn;

    @FXML
    private Button refreshButton;

    private LibraryManager libraryManager;
    private ObservableList<Transaction> reportData = FXCollections.observableArrayList();
    
    // Definisikan formatter tanggal yang diinginkan
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy"); // Contoh: 31 Mei 2025

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        loadReportData();
    }

    @FXML
    private void initialize() {
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Gunakan CellFactory kustom untuk format tanggal
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowDateColumn.setCellFactory(column -> TableCellHelper.getFormattedDateCell(dateFormatter)); // <--- AKTIFKAN INI

        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(column -> TableCellHelper.getFormattedDateCell(dateFormatter)); // <--- AKTIFKAN INI

        bookTitleColumn.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            Book book = libraryManager.findBookByIsbn(transaction.getIsbn());
            return new SimpleStringProperty(book != null ? book.getTitle() : "Buku Tidak Ditemukan");
        });

        memberNameColumn.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            Member member = libraryManager.findMemberById(transaction.getMemberId());
            return new SimpleStringProperty(member != null ? member.getFullName() : "Anggota Tidak Ditemukan");
        });
        
        reportTableView.setItems(reportData);
        System.out.println("BorrowedBooksReportController initialized.");
    }

    private void loadReportData() {
        if (libraryManager == null) {
            System.err.println("LibraryManager belum di-set di BorrowedBooksReportController.");
            reportData.clear();
            return;
        }
        reportData.clear();
        reportData.addAll(libraryManager.getBorrowedBooksReport());
        reportTableView.refresh();
        System.out.println(reportData.size() + " data buku dipinjam dimuat ke laporan.");
    }

    @FXML
    private void handleRefreshReportAction(ActionEvent event) {
        System.out.println("Tombol Refresh Laporan diklik.");
        loadReportData();
    }

    // --- Implementasi TableCellHelper sebagai static nested class ---
    static class TableCellHelper {
        /**
         * Membuat TableCell yang memformat objek Temporal (seperti LocalDate) menggunakan DateTimeFormatter.
         * @param formatter DateTimeFormatter yang akan digunakan.
         * @return TableCell yang sudah diformat.
         */
        public static <S, T extends Temporal> TableCell<S, T> getFormattedDateCell(DateTimeFormatter formatter) {
            return new TableCell<>() { // Anonymous inner class
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null); // Kosongkan cell jika item null atau baris kosong
                        setGraphic(null);
                    } else {
                        setText(formatter.format(item)); // Format item (tanggal) dan tampilkan sebagai teks
                    }
                }
            };
        }
    }
}