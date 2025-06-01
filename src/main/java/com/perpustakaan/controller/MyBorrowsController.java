package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.LibraryManager;
import com.perpustakaan.model.Transaction; // Model utama untuk tabel ini
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell; // Untuk kustomisasi cell
import java.text.NumberFormat; // Untuk format mata uang
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal; // Untuk helper tanggal
import java.util.Locale;

public class MyBorrowsController implements NeedsLibraryManager, NeedsUserContext {

    @FXML
    private Label userInfoLabel;
    @FXML
    private TableView<Transaction> borrowsTableView;
    @FXML
    private TableColumn<Transaction, String> bookTitleColumn;
    @FXML
    private TableColumn<Transaction, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<Transaction, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Transaction, LocalDate> returnDateColumn; // Kolom untuk ActualReturnDate
    @FXML
    private TableColumn<Transaction, String> statusColumn;
    @FXML
    private TableColumn<Transaction, Double> fineColumn;

    private LibraryManager libraryManager;
    private String currentMemberId;
    private String currentMemberDisplayName;

    private ObservableList<Transaction> memberTransactionsData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        attemptLoadData();
    }

    @Override
    public void setUserContext(String userId, String userRole, String displayName) {
        this.currentMemberId = userId;
        this.currentMemberDisplayName = displayName; // Simpan display name

        if (userInfoLabel != null) {
            userInfoLabel.setText("Riwayat Peminjaman untuk: " + (this.currentMemberDisplayName != null ? this.currentMemberDisplayName : this.currentMemberId));
        }
        attemptLoadData();
    }
    
    private void attemptLoadData() {
        if (this.libraryManager != null && this.currentMemberId != null) {
            loadMemberTransactions();
        }
    }

    @FXML
    private void initialize() {
        // Mengatur CellValueFactory
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowDateColumn.setCellFactory(column -> TableCellHelper.getFormattedDateCell(dateFormatter));

        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(column -> TableCellHelper.getFormattedDateCell(dateFormatter));

        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("actualReturnDate")); // Hubungkan ke actualReturnDate
        returnDateColumn.setCellFactory(column -> TableCellHelper.getFormattedDateCell(dateFormatter)); // Format juga

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<>("fine"));
        fineColumn.setCellFactory(column -> TableCellHelper.getFormattedCurrencyCell()); // Format sebagai Rupiah

        bookTitleColumn.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            Book book = libraryManager.findBookByIsbn(transaction.getIsbn());
            return new SimpleStringProperty(book != null ? book.getTitle() : "N/A - Buku tidak ditemukan");
        });

        borrowsTableView.setItems(memberTransactionsData);
        System.out.println("MyBorrowsController initialized.");
    }

    private void loadMemberTransactions() {
        if (libraryManager == null || currentMemberId == null) {
            System.err.println("LibraryManager atau ID Member belum siap di MyBorrowsController.");
            memberTransactionsData.clear();
            return;
        }
        memberTransactionsData.clear();
        memberTransactionsData.addAll(libraryManager.getTransactionsByMemberId(currentMemberId));
        borrowsTableView.refresh();
        System.out.println(memberTransactionsData.size() + " transaksi ditemukan untuk member " + currentMemberId);
    }

    @FXML
    private void handleRefreshAction(ActionEvent event) {
        System.out.println("Tombol Refresh Riwayat diklik.");
        loadMemberTransactions();
    }

    // --- Implementasi TableCellHelper sebagai static nested class (sama seperti di controller lain) ---
    static class TableCellHelper {
        public static <S, T extends Temporal> TableCell<S, T> getFormattedDateCell(DateTimeFormatter formatter) {
            return new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : formatter.format(item));
                }
            };
        }

        public static <S> TableCell<S, Double> getFormattedCurrencyCell() {
            return new TableCell<>() {
                private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : currencyFormat.format(item));
                }
            };
        }
    }
    // --- Akhir Implementasi TableCellHelper ---
}
