package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.LibraryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class BorrowBookController implements NeedsLibraryManager, NeedsUserContext {

    @FXML
    private Label userInfoLabel;
    @FXML
    private TextField filterField;
    @FXML
    private TableView<Book> bookTableView;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> quantityColumn;
    @FXML
    private Button borrowButton;
    // Tidak ada @FXML untuk refreshButton di kode Anda, tapi ada di FXML. Jika ada, tambahkan:
    // @FXML private Button refreshButton; 

    private LibraryManager libraryManager;
    private String currentLoggedInMemberId; // Lebih deskriptif untuk ID member yang login
    private String currentUserRole;       // Peran pengguna (MEMBER)
    private String currentUserDisplayName; // Nama tampilan pengguna

    private ObservableList<Book> masterBookData = FXCollections.observableArrayList(); // Daftar utama semua buku
    private FilteredList<Book> filteredBookData; // Untuk data yang sudah difilter
    private SortedList<Book> sortedBookData;     // Untuk data yang bisa diurutkan

    /**
     * Dipanggil oleh MainController untuk meneruskan instance LibraryManager.
     */
    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        // Data akan dimuat setelah user context juga di-set.
        attemptLoadData(); 
    }

    /**
     * Dipanggil oleh MainController untuk meneruskan konteks pengguna yang login.
     */
    @Override
    public void setUserContext(String userId, String userRole, String displayName) {
        this.currentLoggedInMemberId = userId;
        this.currentUserRole = userRole;
        this.currentUserDisplayName = displayName; // Simpan display name

        if (userInfoLabel != null) {
            userInfoLabel.setText("Pengguna: " + (this.currentUserDisplayName != null ? this.currentUserDisplayName : this.currentLoggedInMemberId) + 
                                  " (ID: " + this.currentLoggedInMemberId + " | Peran: " + this.currentUserRole + ")");
        }
        attemptLoadData();
    }

    /**
     * Mencoba memuat data buku jika LibraryManager dan UserContext sudah siap.
     */
    private void attemptLoadData() {
        if (this.libraryManager != null && this.currentLoggedInMemberId != null) {
            loadAndDisplayBooks();
        } else {
            if(this.libraryManager == null) System.err.println("Menunggu LibraryManager di BorrowBookController...");
            if(this.currentLoggedInMemberId == null) System.err.println("Menunggu UserContext di BorrowBookController...");
        }
    }

    @FXML
    private void initialize() {
        // 1. Setup CellValueFactory untuk kolom tabel
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // 2. Inisialisasi list untuk data tabel dan filter
        // masterBookData sudah diinisialisasi sebagai ObservableList
        filteredBookData = new FilteredList<>(masterBookData, p -> true); // Awalnya tampilkan semua

        // Listener untuk filterField agar filter diterapkan saat teks berubah
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyBookFilter(newValue);
        });
        
        // Bungkus FilteredList dengan SortedList agar tabel bisa di-sort
        sortedBookData = new SortedList<>(filteredBookData);
        // Hubungkan comparator SortedList dengan comparator TableView
        sortedBookData.comparatorProperty().bind(bookTableView.comparatorProperty());
        
        // Set item TableView ke SortedList
        bookTableView.setItems(sortedBookData);
        
        // Listener untuk seleksi item di tabel untuk mengatur status tombol Pinjam
        bookTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> manageBorrowButtonState(newSelection)
        );
        
        manageBorrowButtonState(null); // Awalnya tombol pinjam nonaktif
        System.out.println("BorrowBookController initialized.");
    }

    /**
     * Memuat data buku dari LibraryManager dan menampilkannya di tabel.
     */
    private void loadAndDisplayBooks() {
        if (libraryManager == null) {
            System.err.println("LibraryManager tidak tersedia di BorrowBookController. Tidak bisa memuat buku.");
            masterBookData.clear(); // Bersihkan data jika libraryManager tidak ada
            return;
        }
        masterBookData.clear();
        // Member bisa melihat semua buku yang tersedia (stok > 0 atau tidak, akan di-handle oleh manageBorrowButtonState)
        masterBookData.addAll(libraryManager.getAllBooks());
        
        // Filter mungkin sudah memiliki teks, jadi panggil applyBookFilter untuk memperbarui tampilan
        applyBookFilter(filterField.getText());

        // Status tombol pinjam juga perlu di-update setelah data baru dimuat dan seleksi mungkin berubah
        manageBorrowButtonState(bookTableView.getSelectionModel().getSelectedItem());

        System.out.println(masterBookData.size() + " buku dimuat untuk peminjaman.");
    }
    
    /**
     * Menerapkan filter pada daftar buku berdasarkan teks input.
     * @param filterText Teks untuk filter.
     */
    private void applyBookFilter(String filterText) {
        if (filteredBookData == null) return; // Guard clause jika filteredBookData belum siap

        filteredBookData.setPredicate(book -> {
            if (filterText == null || filterText.isEmpty() || filterText.isBlank()) {
                return true; // Tampilkan semua jika filter kosong
            }
            String lowerCaseFilter = filterText.toLowerCase();
            // Filter berdasarkan judul, pengarang, atau ISBN
            return (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerCaseFilter)) ||
                   (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowerCaseFilter)) ||
                   (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lowerCaseFilter));
        });
    }

    /**
     * Mengatur status aktif/nonaktif tombol Pinjam.
     * Tombol aktif jika ada buku yang dipilih dan kuantitasnya > 0.
     * @param selectedBook Buku yang sedang dipilih.
     */
    private void manageBorrowButtonState(Book selectedBook) {
        if (borrowButton != null) {
            boolean disable = (selectedBook == null || selectedBook.getQuantity() <= 0);
            borrowButton.setDisable(disable);
        }
    }

    /**
     * Menangani aksi ketika tombol "Pinjam Buku Terpilih" ditekan.
     */
    @FXML
    private void handleBorrowBookAction(ActionEvent event) {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();

        // Validasi awal
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Buku", "Tidak ada buku yang dipilih.", "Silakan pilih buku dari daftar untuk dipinjam.");
            return;
        }
        if (selectedBook.getQuantity() <= 0) {
             showAlert(Alert.AlertType.WARNING, "Stok Habis", "Stok buku '" + selectedBook.getTitle() + "' telah habis.", "Silakan pilih buku lain.");
            return;
        }
        if (currentLoggedInMemberId == null || currentLoggedInMemberId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Pengguna", "Informasi ID pengguna tidak ditemukan.", "Silakan login ulang atau hubungi administrator.");
            return;
        }

        System.out.println("Member '" + currentLoggedInMemberId + "' (Nama: " + currentUserDisplayName + ") mencoba meminjam buku '" + selectedBook.getIsbn() + ": " + selectedBook.getTitle() + "'");
        
        // Nonaktifkan tombol pinjam selama proses untuk mencegah klik ganda
        borrowButton.setDisable(true);

        boolean success = libraryManager.borrowBook(currentLoggedInMemberId, selectedBook.getIsbn());

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Peminjaman Berhasil", 
                      "Buku '" + selectedBook.getTitle() + "' berhasil dipinjam oleh " + currentUserDisplayName + ".",
                      "Harap kembalikan sebelum atau pada tanggal jatuh tempo.");
            loadAndDisplayBooks(); // Refresh daftar buku (untuk update kuantitas dan status tombol)
        } else {
            // Pesan error spesifik bisa jadi sudah dicetak oleh LibraryManager.
            // LibraryManager.borrowBook bisa mengembalikan false jika user sudah pinjam buku yg sama dan belum kembali.
            showAlert(Alert.AlertType.ERROR, "Peminjaman Gagal", 
                      "Gagal memproses peminjaman buku '" + selectedBook.getTitle() + "'.",
                      "Kemungkinan Anda sudah meminjam buku ini dan belum dikembalikan, stok baru saja habis, atau terjadi kesalahan lain.");
            // Aktifkan kembali tombol jika gagal, setelah data di-refresh dan status tombol di-update
            loadAndDisplayBooks(); // Refresh untuk memastikan status tombol benar
        }
        // Status tombol akan di-update oleh loadAndDisplayBooks -> manageBorrowButtonState
    }

    /**
     * Menangani aksi ketika tombol "Refresh Daftar" ditekan.
     */
    @FXML
    private void handleRefreshAction(ActionEvent event) {
        System.out.println("Tombol Refresh daftar buku diklik.");
        if(filterField != null) filterField.clear();
        loadAndDisplayBooks();
    }

    /**
     * Metode helper untuk menampilkan dialog Alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Mengatur owner agar alert muncul di atas window saat ini (jika window ada)
        if (bookTableView != null && bookTableView.getScene() != null && bookTableView.getScene().getWindow() != null) {
            alert.initOwner(bookTableView.getScene().getWindow());
        }
        alert.showAndWait();
    }
}