package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class KelolaBukuController {

    // Tabel dan kolom-kolomnya
    @FXML
    private TableView<Book> bookTableView;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> categoryColumn;
    @FXML
    private TableColumn<Book, String> statusColumn;

    // Form fields
    @FXML
    private TextField isbnField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField categoryField;
    @FXML
    private ComboBox<String> statusComboBox;

    // Tombol-tombol aksi
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;

    private BookDAO bookDAO;
    private ObservableList<Book> bookList;

    public KelolaBukuController() {
        bookDAO = new BookDAO();
    }

    @FXML
    public void initialize() {
        // 1. Setup kolom tabel untuk menampilkan data dari properti objek Book.
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // 2. Setup kolom status dengan CellFactory khusus untuk membuat badge berwarna.
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(_ -> new TableCell<Book, String>() {
            private final Label statusLabel = new Label();
            {
                // Inisialisasi awal untuk label di dalam sel
                statusLabel.getStyleClass().add("status-badge");
                setGraphic(statusLabel);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    // Jangan tampilkan apa-apa jika barisnya kosong
                    setGraphic(null);
                } else {
                    // Set teks dan gaya CSS berdasarkan nilai status
                    statusLabel.setText(status);
                    statusLabel.getStyleClass().removeAll("status-tersedia", "status-dipinjam");

                    if (status.equalsIgnoreCase("Tersedia")) {
                        statusLabel.getStyleClass().add("status-tersedia");
                    } else {
                        statusLabel.getStyleClass().add("status-dipinjam");
                    }
                    setGraphic(statusLabel);
                }
            }
        });

        // 3. Isi pilihan untuk ComboBox status di form.
        statusComboBox.setItems(FXCollections.observableArrayList("Tersedia", "Dipinjam"));

        // 4. Muat data buku dari file CSV ke dalam tabel.
        loadBookData();

        // 5. Tambahkan listener untuk mendeteksi saat pengguna mengklik baris di tabel,
        // lalu tampilkan detailnya di form.
        bookTableView.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> showBookDetails(newValue));
    }

    private void loadBookData() {
        bookList = FXCollections.observableArrayList(bookDAO.getAllBooks());
        bookTableView.setItems(bookList);
    }

    private void showBookDetails(Book book) {
        if (book != null) {
            // Isi form dengan data dari buku yang dipilih
            isbnField.setText(book.getIsbn());
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            categoryField.setText(book.getCategory());
            statusComboBox.setValue(book.getStatus());
            isbnField.setEditable(false); // ISBN tidak boleh diubah
        } else {
            // Kosongkan form jika tidak ada buku yang dipilih
            clearForm();
        }
    }

    @FXML
    private void handleAddButton() {
        Book newBook = new Book(
                isbnField.getText(),
                titleField.getText(),
                authorField.getText(),
                categoryField.getText(),
                statusComboBox.getValue());
        bookDAO.addBook(newBook);
        loadBookData(); // Muat ulang data untuk menampilkan buku baru
        clearForm();
    }

    @FXML
    private void handleUpdateButton() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            Book updatedBook = new Book(
                    isbnField.getText(), // ISBN tidak berubah
                    titleField.getText(),
                    authorField.getText(),
                    categoryField.getText(),
                    statusComboBox.getValue());
            bookDAO.updateBook(updatedBook);
            loadBookData();
            clearForm();
        }
    }

    @FXML
    private void handleDeleteButton() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            // Tampilkan dialog konfirmasi sebelum menghapus
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Hapus");
            alert.setHeaderText("Hapus Buku: " + selectedBook.getTitle());
            alert.setContentText("Apakah Anda yakin ingin menghapus buku ini?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                bookDAO.deleteBook(selectedBook.getIsbn());
                loadBookData();
                clearForm();
            }
        }
    }

    @FXML
    private void handleClearButton() {
        clearForm();
    }

    private void clearForm() {
        bookTableView.getSelectionModel().clearSelection();
        isbnField.clear();
        titleField.clear();
        authorField.clear();
        categoryField.clear();
        statusComboBox.setValue(null);
        isbnField.setEditable(true);
    }
}