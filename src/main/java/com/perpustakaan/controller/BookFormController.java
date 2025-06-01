package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.LibraryManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BookFormController implements NeedsLibraryManager {

    @FXML
    private Label formTitleLabel; // Untuk mengubah judul form (Tambah/Edit)
    @FXML
    private TextField isbnField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField quantityField;
    @FXML
    private Button simpanButton; // Bisa digunakan untuk mengubah teks tombol jika perlu

    private LibraryManager libraryManager;
    private Stage dialogStage; // Stage dari dialog ini, untuk menutupnya
    private Book bookToEdit;   // Buku yang akan diedit (null jika mode tambah)
    private boolean isEditMode = false;

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    /**
     * Dipanggil oleh controller yang membuka dialog ini untuk memberikan Stage.
     * @param dialogStage Stage dari dialog ini.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Dipanggil jika ini adalah form untuk mengedit buku.
     * Mengisi field dengan data buku yang ada dan mengubah judul form.
     * @param book Buku yang akan diedit.
     */
    public void setBookToEdit(Book book) {
        this.bookToEdit = book;
        this.isEditMode = true;

        formTitleLabel.setText("Edit Data Buku");
        // Isi field dengan data dari bookToEdit
        isbnField.setText(book.getIsbn());
        isbnField.setDisable(true); // ISBN biasanya tidak boleh diubah saat edit
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        quantityField.setText(String.valueOf(book.getQuantity()));
        // simpanButton.setText("Update"); // Opsional: Ubah teks tombol simpan
    }

    @FXML
    private void initialize() {
        // Bisa ditambahkan listener untuk validasi input secara real-time jika diinginkan
    }

    @FXML
    private void handleSimpanAction(ActionEvent event) {
        if (!isInputValid()) {
            return; // Jangan lanjutkan jika input tidak valid
        }

        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        int quantity = Integer.parseInt(quantityField.getText().trim()); // Validasi sudah memastikan ini angka

        if (isEditMode) {
            // Mode Edit
            if (bookToEdit == null) { // Seharusnya tidak terjadi jika alur benar
                showAlert(Alert.AlertType.ERROR, "Kesalahan Edit", "Objek buku untuk diedit tidak ditemukan.", "Silakan coba lagi.");
                return;
            }
            // ISBN tidak diubah, jadi kita bisa langsung set atribut lain
            bookToEdit.setTitle(title);
            bookToEdit.setAuthor(author);
            bookToEdit.setQuantity(quantity);
            
            boolean updated = libraryManager.updateBook(bookToEdit);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Update Berhasil", "Data buku berhasil diperbarui.", null);
                dialogStage.close(); // Tutup dialog setelah berhasil
            } else {
                // Kemungkinan buku tidak ditemukan lagi oleh LibraryManager (jarang terjadi)
                showAlert(Alert.AlertType.ERROR, "Update Gagal", "Gagal memperbarui data buku.", "Buku mungkin tidak ditemukan atau terjadi kesalahan lain.");
            }
        } else {
            // Mode Tambah
            // Cek duplikasi ISBN sebelum menambah (LibraryManager juga melakukan ini, tapi baik untuk validasi awal)
            if (libraryManager.findBookByIsbn(isbn) != null) {
                showAlert(Alert.AlertType.ERROR, "Tambah Gagal", "ISBN sudah ada.", "Buku dengan ISBN '" + isbn + "' sudah terdaftar dalam sistem.");
                isbnField.requestFocus();
                return;
            }
            Book newBook = new Book(isbn, title, author, quantity);
            boolean added = libraryManager.addBook(newBook);
            if (added) {
                showAlert(Alert.AlertType.INFORMATION, "Tambah Berhasil", "Buku baru berhasil ditambahkan.", null);
                dialogStage.close(); // Tutup dialog setelah berhasil
            } else {
                // Ini bisa terjadi jika ada race condition atau validasi LibraryManager gagal karena alasan lain
                showAlert(Alert.AlertType.ERROR, "Tambah Gagal", "Gagal menambahkan buku baru.", "Mungkin ISBN sudah ada atau terjadi kesalahan lain.");
            }
        }
    }

    @FXML
    private void handleBatalAction(ActionEvent event) {
        if (dialogStage != null) {
            dialogStage.close(); // Tutup dialog
        }
    }

    /**
     * Memvalidasi input pengguna di form.
     * @return true jika semua input valid, false sebaliknya.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (isbnField.getText() == null || isbnField.getText().trim().isEmpty()) {
            errorMessage += "ISBN tidak boleh kosong!\n";
        }
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage += "Judul buku tidak boleh kosong!\n";
        }
        if (authorField.getText() == null || authorField.getText().trim().isEmpty()) {
            errorMessage += "Nama pengarang tidak boleh kosong!\n";
        }
        if (quantityField.getText() == null || quantityField.getText().trim().isEmpty()) {
            errorMessage += "Kuantitas tidak boleh kosong!\n";
        } else {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity < 0) {
                    errorMessage += "Kuantitas tidak boleh negatif!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Kuantitas harus berupa angka yang valid!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Tampilkan pesan error
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Harap perbaiki field yang tidak valid:", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Pastikan dialog ini juga modal terhadap parentnya (dialogStage)
        if (dialogStage != null && dialogStage.getOwner() != null) {
             alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }
}