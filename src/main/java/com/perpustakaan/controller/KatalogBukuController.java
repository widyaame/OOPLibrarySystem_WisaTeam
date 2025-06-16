package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class KatalogBukuController {

    @FXML private TableView<Book> bookTableView;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TextField searchField;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private ChoiceBox<Integer> durasiChoiceBox;
    @FXML private Label dueDateLabel;
    @FXML private Button borrowButton;

    private BookDAO bookDAO;
    private TransactionDAO transactionDAO;
    private ObservableList<Book> availableBooksList;
    private String currentMemberId;

    public KatalogBukuController() {
        bookDAO = new BookDAO();
        transactionDAO = new TransactionDAO();
    }

    public void initData(String memberId) {
        this.currentMemberId = memberId;
    }

    @FXML
    public void initialize() {
        // Setup tabel
        setupTableColumns();
        loadAvailableBooks();
        setupSearchFilter();

        // Setup ChoiceBox durasi
        durasiChoiceBox.setItems(FXCollections.observableArrayList(3, 7, 14, 30));
        durasiChoiceBox.setValue(7); // Nilai default 7 hari

        // Listener untuk mengubah tanggal jatuh tempo secara dinamis
        durasiChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (_, _, _) -> updateDueDateLabel());

        // Listener untuk menampilkan detail buku saat dipilih
        bookTableView.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> showBookDetails(newValue));
    }

    private void setupTableColumns() {
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
    }

    private void loadAvailableBooks() {
        availableBooksList = FXCollections.observableArrayList(
                bookDAO.getAllBooks().stream()
                        .filter(book -> "Tersedia".equalsIgnoreCase(book.getStatus()))
                        .collect(Collectors.toList())
        );
        bookTableView.setItems(availableBooksList);
    }

    private void setupSearchFilter() {
        FilteredList<Book> filteredData = new FilteredList<>(availableBooksList, _ -> true);
        searchField.textProperty().addListener((_, _, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return book.getTitle().toLowerCase().contains(lowerCaseFilter) || 
                       book.getAuthor().toLowerCase().contains(lowerCaseFilter);
            });
        });
        bookTableView.setItems(filteredData);
    }

    private void showBookDetails(Book book) {
        if (book != null) {
            titleLabel.setText(book.getTitle());
            authorLabel.setText(book.getAuthor());
            borrowButton.setDisable(false);
            updateDueDateLabel(); // Update tanggal saat buku baru dipilih
        } else {
            titleLabel.setText("-");
            authorLabel.setText("-");
            borrowButton.setDisable(true);
            dueDateLabel.setText("-");
        }
    }

    private void updateDueDateLabel() {
        if (durasiChoiceBox.getValue() != null) {
            LocalDate dueDate = LocalDate.now().plusDays(durasiChoiceBox.getValue());
            // Format tanggal agar lebih mudah dibaca (misal: 15 Juni 2025)
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.of("id", "ID"));
            dueDateLabel.setText(dueDate.format(formatter));
        }
    }

    @FXML
    private void handleBorrowButton() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        Integer durasi = durasiChoiceBox.getValue();
        if (selectedBook == null || durasi == null) return;

        // Buat transaksi baru dengan durasi yang dipilih
        Transaction newTransaction = new Transaction(
                UUID.randomUUID().toString(),
                currentMemberId,
                selectedBook.getIsbn(),
                LocalDate.now(),
                LocalDate.now().plusDays(durasi), // Jatuh tempo sesuai pilihan
                null,
                Transaction.Status.DIPINJAM
        );
        transactionDAO.addTransaction(newTransaction);

        selectedBook.setStatus("Dipinjam");
        bookDAO.updateBook(selectedBook);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Peminjaman Berhasil");
        alert.setHeaderText(null);
        alert.setContentText("Anda berhasil meminjam buku: " + selectedBook.getTitle() + "\nHarap kembalikan sebelum: " + dueDateLabel.getText());
        alert.showAndWait();

        // Muat ulang data dan reset form
        loadAvailableBooks();
        showBookDetails(null);
    }
}