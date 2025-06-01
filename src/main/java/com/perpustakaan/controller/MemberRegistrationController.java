package com.perpustakaan.controller;

import com.perpustakaan.model.LibraryManager;
import com.perpustakaan.model.Member;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class MemberRegistrationController implements NeedsLibraryManager {

    @FXML
    private TextField idField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    // @FXML private Label formTitleLabel; // Jika judul form ingin diubah dari controller

    private LibraryManager libraryManager;
    private Stage dialogStage; // Opsional: jika dialog ini perlu menutup dirinya sendiri (biasanya ya)

    // Pola regex sederhana untuk validasi format email (sama seperti di MemberFormController)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public void setLibraryManager(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    // Metode ini bisa dipanggil oleh LoginController jika dialog ini perlu referensi ke Stage-nya sendiri
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


    @FXML
    private void initialize() {
        System.out.println("MemberRegistrationController initialized.");
    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        if (!isInputValid()) {
            return; // Jangan lanjutkan jika input tidak valid
        }

        String id = idField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String major = majorField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText(); // Password tidak di-trim

        // Cek duplikasi ID melalui LibraryManager
        if (libraryManager.findMemberById(id) != null) {
            showAlert(Alert.AlertType.ERROR, "Registrasi Gagal", "ID Anggota sudah terdaftar.", "Silakan gunakan ID Anggota yang lain.");
            idField.requestFocus();
            return;
        }

        // Cek duplikasi Email melalui LibraryManager
        if (libraryManager.findMemberByEmail(email) != null) {
            showAlert(Alert.AlertType.ERROR, "Registrasi Gagal", "Email sudah terdaftar.", "Silakan gunakan alamat email yang lain.");
            emailField.requestFocus();
            return;
        }

        // Buat objek Member baru
        Member newMember = new Member(id, fullName, (major.isEmpty() ? null : major), email, password);
        
        // Tambahkan member baru melalui LibraryManager
        boolean added = libraryManager.addMember(newMember);

        if (added) {
            showAlert(Alert.AlertType.INFORMATION, "Registrasi Berhasil", 
                      "Akun anggota baru untuk '" + fullName + "' berhasil dibuat.", 
                      "Anda sekarang bisa login menggunakan ID Anggota dan password Anda.");
            closeDialog(); // Tutup dialog registrasi setelah berhasil
        } else {
            // Ini seharusnya jarang terjadi jika pengecekan duplikasi di atas sudah benar,
            // tapi sebagai pengaman jika ada logika lain di libraryManager.addMember() yang bisa gagal.
            showAlert(Alert.AlertType.ERROR, "Registrasi Gagal", "Gagal membuat akun anggota baru.", "Silakan coba lagi atau hubungi administrator.");
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        // Dapatkan Stage dari salah satu tombol atau elemen UI di form ini
        if (idField != null && idField.getScene() != null && idField.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.close();
        } else if (this.dialogStage != null) { // Jika stage di-pass dari pemanggil
            this.dialogStage.close();
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (idField.getText() == null || idField.getText().trim().isEmpty()) {
            errorMessage += "ID Anggota tidak boleh kosong!\n";
        }
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            errorMessage += "Nama Lengkap tidak boleh kosong!\n";
        }
        // Jurusan bisa opsional
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "Email tidak boleh kosong!\n";
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            errorMessage += "Format email tidak valid! (Contoh: user@example.com)\n";
        }
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            errorMessage += "Password tidak boleh kosong!\n";
        } else if (passwordField.getText().length() < 6) { // Contoh validasi panjang password
            errorMessage += "Password minimal 6 karakter!\n";
        }
        if (confirmPasswordField.getText() == null || confirmPasswordField.getText().isEmpty()) {
            errorMessage += "Konfirmasi Password tidak boleh kosong!\n";
        } else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorMessage += "Password dan Konfirmasi Password tidak cocok!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Harap perbaiki field yang tidak valid:", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Mengatur owner agar alert muncul di atas window registrasi
        if (idField != null && idField.getScene() != null && idField.getScene().getWindow() != null) {
            alert.initOwner(idField.getScene().getWindow());
        } else if (this.dialogStage != null) {
             alert.initOwner(this.dialogStage);
        }
        alert.showAndWait();
    }
}